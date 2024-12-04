package com.dytech.edge.cache;

import com.dytech.common.net.Proxy;
import com.dytech.common.net.Wget;
import com.dytech.common.net.WgetConnectionHandler;
import com.dytech.devlib.PropBagEx;
import com.tle.common.Pair;
import com.tle.core.remoting.ToolsService;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Service extends Thread {
  private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
  private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
  private static final int DEFAULT_HTTP_PORT = 80;
  private static final String RESOURCES_XML = "/cache.xml";
  private static final String CONFIG_XML = "/config.xml";

  private static final String[] SEEK_FOR_HEADERS =
      new String[] {
        "Cache-Control", "X-Cache", "X-Cache-Lookup",
      };
  private static final Map<String, List<String>> SEND_HEADERS = new HashMap<String, List<String>>();

  private final long sleep;
  private final String username;
  private final String password;
  private final String host;
  private String context;
  private final int port;
  private final URL serviceEndpoint;

  @SuppressWarnings("nls")
  public Service() throws NumberFormatException, MalformedURLException {
    PropBagEx config = new PropBagEx(getClass().getResourceAsStream(CONFIG_XML));

    port = config.getIntNode("soapserver/port");
    host = config.getNode("soapserver/host").trim();
    context = config.getNode("soapserver/context").trim();
    username = config.getNode("soapserver/username").trim();
    password = config.getNode("soapserver/password").trim();
    String institution = config.getNode("soapserver/institution").trim();

    if (context.length() == 0) {
      context = "/";
    } else {
      if (!context.startsWith("/")) {
        context = '/' + context;
      }

      if (!context.endsWith("/")) {
        context += '/';
      }
    }
    if (institution.length() > 0) {
      context += institution + "/";
    }

    sleep = TimeUnit.HOURS.toMillis(config.getIntNode("sleep"));

    String proxyHost = config.getNode("proxy/host");
    String proxyUser = config.getNode("proxy/user");
    String proxyPass = config.getNode("proxy/pass");
    int proxyPort = config.getIntNode("proxy/port");

    Proxy.setProxy(proxyHost, proxyPort, proxyUser, proxyPass);

    serviceEndpoint = new URL("http", host, port, context + "services/tools.service");

    PropBagEx headers = config.getSubtree("headers");
    if (headers != null) {
      for (PropBagEx header : headers.iterateAll("header")) {
        String headerName = header.getNode("@name");
        List<String> list = SEND_HEADERS.get(headerName);
        if (list == null) {
          list = new ArrayList<String>();
          SEND_HEADERS.put(headerName, list);
        }
        list.add(header.getNode(""));
      }
    }
  }

  private Pair<ToolsService, String> getSession() {
    SoapHelper soapHelper = new SoapHelper();
    final ToolsService clientService =
        soapHelper.createSoap(
            ToolsService.class, serviceEndpoint, "http://soap.remoting.web.tle.com", null);
    final String sessionid = clientService.login(username, password);
    return new Pair<ToolsService, String>(clientService, sessionid);
  }

  @Override
  public void run() {
    LOGGER.info("Starting Daemon Service");
    try {
      while (true) {
        long snooze = getSchedule();
        sleepEasy(snooze);

        cacheData();
        sleepEasy(sleep);
      }
    } catch (Exception ex) {
      LOGGER.fatal("Stopping Daemon Service", ex);
    }
  }

  private void cacheData() throws Exception {
    PropBagEx cache = null;
    try (InputStream in = getClass().getResourceAsStream(RESOURCES_XML)) {
      cache = new PropBagEx(in);
    } catch (Exception ex) {
      LOGGER.warn("No previous cache log found.  Creating new version.");
      cache = new PropBagEx();
    }

    String lastUpdate = cache.getNode("last.update", "2000-01-01T00:00:00+0000");

    LOGGER.info("Retrieving resouce differences since " + lastUpdate);
    Set<String> additions = new HashSet<String>();
    Set<String> removals = new HashSet<String>();
    Set<String> all = new HashSet<String>();

    Pair<ToolsService, String> session = getSession();
    String date = getDifferences(session, lastUpdate, additions, removals);

    // Firstly we exclude all the removed urls
    LOGGER.info(removals.size() + " resources have expired.");

    for (String item : cache.iterateValues("resources/item")) {
      if (!removals.contains(item)) {
        all.add(item);
      } else {
        LOGGER.info("Removing item: " + item);
      }
    }
    all.addAll(additions);

    // Now we include in the new additions
    LOGGER.info(additions.size() + " new resources need downloading.");

    cache.deleteNode("resources");

    PropBagEx resXml = cache.newSubtree("resources");
    for (String item : all) {
      LOGGER.info("Adding item: " + item);
      resXml.createNode("item", item);
    }

    // We also need to set the date that we retrieved this list
    cache.setNode("last.update", date);

    // And we'll write the list back to storage before we lose it
    String outFile = getClass().getResource(RESOURCES_XML).getFile();
    outFile = URLDecoder.decode(outFile, "UTF-8");
    try (BufferedWriter out = new BufferedWriter(new FileWriter(outFile))) {
      out.write(cache.toString());
    } catch (IOException ex) {
      LOGGER.error("Possibly could not write resources list back to disk", ex);
    }

    // And now we download each of the remaining urls
    final int totalCount = cache.nodeCount("resources/item");
    LOGGER.info("There are now " + totalCount + " resources in total that require caching.");

    StringBuffer serverBase = new StringBuffer();
    serverBase.append("http://");
    serverBase.append(host);
    if (port != DEFAULT_HTTP_PORT) {
      serverBase.append(':');
      serverBase.append(port);
    }
    serverBase.append(context);
    serverBase.append("items/");

    for (String node : cache.iterateValues("resources/item")) {
      ItemId key;
      if (node.indexOf(':') > 0) {
        ItemKey key2 = new ItemKey(node);
        key = new ItemId(key2.getUuid(), key2.getVersion());
      } else {
        // key/uuid/version
        String[] s = node.split("/");
        int i = s.length - 2;
        key = new ItemId(s[i], Integer.parseInt(s[i + 1]));
      }

      downloadItemUrls(key, serverBase.toString(), session.getSecond());
    }
  }

  private void downloadItemUrls(ItemId key, String serverBase, String sessionid)
      throws IOException {
    StringBuilder itemBaseBuff = new StringBuilder(serverBase.toString());
    itemBaseBuff.append(key.getUuid());
    itemBaseBuff.append('/');
    itemBaseBuff.append(key.getVersion());
    itemBaseBuff.append('/');

    String itemBase = itemBaseBuff.toString();

    LOGGER.info("Retrieving attachment list for item " + itemBase);

    // For each attachment....
    URL attachListUrl = new URL(itemBase.toString() + '^');
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    BufferedOutputStream buff = new BufferedOutputStream(out);

    Wget wget = new Wget();
    wget.setCookies(true);
    wget.setRequestParameter("ssid", sessionid);
    try {
      wget.retrieveURL(attachListUrl, buff);
    } catch (Exception ex) {
      LOGGER.warn("Could not retrieve attachment list", ex);
      return;
    }

    // Need to send a GET request for squid, and we should use the cookie
    // that has been set.
    wget.removeRequestParameter("ssid");

    buff.flush();

    try (BufferedReader in =
        new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), "UTF-8"))) {
      String attachment = in.readLine();
      while (attachment != null) {
        attachment = urlEncode(attachment);
        URL url = new URL(itemBase + attachment);
        getURL(wget, url);
        attachment = in.readLine();
      }
    } catch (IOException ex) {
      // This should never happen as we are only reading from a byte
      // array, but if it does, we can ignore it anyway.
    }
  }

  @SuppressWarnings("nls")
  private static String urlEncode(String url) {
    try {
      url = URLEncoder.encode(url, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // Ignore - it's not going to happen
    }

    // Ensure forward slashes are still slashes
    url = url.replaceAll("%2F", "/");

    // Ensure that pluses are changed into the correct %20
    url = url.replaceAll("\\+", "%20");

    return url;
  }

  private String getDifferences(
      Pair<ToolsService, String> session,
      String lastUpdate,
      Collection<String> additions,
      Collection<String> removals)
      throws Exception {
    String[] list = session.getFirst().getCacheList(session.getSecond(), lastUpdate);

    String date = list[0];

    for (int i = 1; i < list.length; i++) {
      String item = list[i].substring(1);
      if (item.length() > 0) {
        if (list[i].charAt(0) == '+') {
          additions.add(item);
        } else {
          removals.add(item);
        }
      }
    }

    return date;
  }

  private long getSchedule() throws Exception {
    Pair<ToolsService, String> session = getSession();
    String time = session.getFirst().getCacheSchedule(session.getSecond());
    LOGGER.info("Scheduled for " + time);

    long schedule = DATE_FORMAT.parse(time).getTime();
    long current = System.currentTimeMillis();

    return schedule - current;
  }

  private void sleepEasy(long sleepInterval) {
    LOGGER.info("Sleeping for " + TimeUnit.MILLISECONDS.toSeconds(sleepInterval) + " seconds");
    long wakeUpTime = sleepInterval + System.currentTimeMillis();
    while (System.currentTimeMillis() < wakeUpTime) {
      try {
        sleep(wakeUpTime - System.currentTimeMillis());
      } catch (InterruptedException ex) {
        // We don't care if we are interrupted.
      }
    }
    LOGGER.info("Waking up!");
  }

  /**
   * The following method is a cut-n-paste rehashing of the ANT Get task which is licenced under the
   * Apache licence.
   */
  private void getURL(Wget wget, URL source) {
    if (wget == null) {
      wget = new Wget();
    }

    wget.clearHeaders();
    for (Map.Entry<String, List<String>> header : SEND_HEADERS.entrySet()) {
      for (String val : header.getValue()) {
        wget.setHeader(header.getKey(), val);
      }
    }

    if (LOGGER.isDebugEnabled()) {
      wget.setHandler(new MyHandler());
    }

    LOGGER.info("Attempting to retrieve " + source.toString());
    try {
      wget.retrieveURL(source, new NobodyCaresAboutThisOutputStream());
    } catch (IOException ex) {
      LOGGER.warn("Could not retrieve " + source.toString());
    }
  }

  /**
   * This is a output stream that nobody cares about. It just throws away anything that is written.
   *
   * @author Nicholas Read
   */
  private static final class NobodyCaresAboutThisOutputStream extends OutputStream {
    @Override
    public void write(int b) {
      // Nobody cares
    }

    @Override
    public void write(byte[] b) throws IOException {
      // Nobody cares
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      // Nobody cares
    }
  }

  /**
   * We use this to print out the HTTP headers with Wget.
   *
   * @author Nicholas Read
   */
  private final class MyHandler implements WgetConnectionHandler {
    @Override
    public void connectionMade(URLConnection connection) {
      if (connection instanceof HttpURLConnection) {
        HttpURLConnection httpcon = (HttpURLConnection) connection;

        StringBuffer headerBuf = new StringBuffer();
        int headerCount = 1;
        boolean done = false;
        while (!done) {
          String headerKey = httpcon.getHeaderFieldKey(headerCount);
          if (headerKey != null) {
            String headerVal = httpcon.getHeaderField(headerCount);

            boolean found = false;
            LOGGER.debug(headerKey + " :: " + headerVal);
            for (int i = 0; i < SEEK_FOR_HEADERS.length && !found; i++) {

              if (headerKey.equalsIgnoreCase(SEEK_FOR_HEADERS[i])) {
                headerBuf.append('<').append(headerKey).append(": ").append(headerVal).append('>');
                found = true;
              }
            }
          } else {
            done = true;
          }
          headerCount++;
        }

        if (headerBuf.indexOf("MISS") >= 0) {
          LOGGER.debug("URL Not Cached: " + headerBuf);
        }
      } else {
        LOGGER.debug("Headers could not be retrieved: Not a HTTP Connection");
      }
    }
  }
}
