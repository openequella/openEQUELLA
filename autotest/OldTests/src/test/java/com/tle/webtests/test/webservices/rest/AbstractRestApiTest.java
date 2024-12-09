package com.tle.webtests.test.webservices.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.tle.annotation.Nullable;
import com.tle.common.Pair;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.test.AbstractSessionTest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;

@TestInstitution("rest")
public abstract class AbstractRestApiTest extends AbstractSessionTest {
  private PoolingClientConnectionManager conMan;

  protected ObjectMapper mapper;
  protected ApiAssertions asserter;

  protected final List<OAuthClient> clients = Lists.newArrayList();
  private String token;

  @BeforeClass
  public void setup()
      throws NoSuchAlgorithmException,
          KeyManagementException,
          UnrecoverableKeyException,
          KeyStoreException {
    X509TrustManager trustManager =
        new X509TrustManager() {
          final X509Certificate[] acceptedIssuers = new X509Certificate[] {};

          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return acceptedIssuers;
          }

          @Override
          public void checkServerTrusted(X509Certificate[] chain, String authType)
              throws CertificateException {
            // Nothing to do here
          }

          @Override
          public void checkClientTrusted(X509Certificate[] chain, String authType)
              throws CertificateException {
            // Nothing to do here
          }
        };

    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, new TrustManager[] {trustManager}, new SecureRandom());

    SSLSocketFactory socketFactory =
        new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

    conMan = new PoolingClientConnectionManager();
    conMan.getSchemeRegistry().register(new Scheme("https", 443, socketFactory));
    conMan.setMaxTotal(200);
    conMan.setDefaultMaxPerRoute(100);

    mapper = new ObjectMapper();
    asserter = new ApiAssertions(context);
  }

  private DefaultHttpClient createClient() {
    final DefaultHttpClient client = new DefaultHttpClient(conMan);
    // Allows a slightly lenient cookie acceptance
    client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
    // Allows follow of redirects on POST
    client.setRedirectStrategy(new LaxRedirectStrategy());

    String proxyHost = testConfig.getProperty("proxy.host");
    if (proxyHost != null) {
      HttpHost proxy = new HttpHost(proxyHost, testConfig.getIntProperty("proxy.port", 80));
      client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }
    return client;
  }

  @BeforeClass
  public void registerClients() throws Exception {
    // Create OAuth client-credentials clients
    try {
      logonOnly("AutoTest", "automated");
      final List<OAuthClient> oauthClients = getOAuthClients();
      for (OAuthClient clientInfo : oauthClients) {
        OAuthUtils.createClient(context, clientInfo);
        clients.add(clientInfo);
      }
      logout();
    } catch (Exception e) {
      e.printStackTrace();
      // Avoid skipping
    }
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    logonOnly("AutoTest", "automated");
    for (OAuthClient client : clients) {
      OAuthUtils.deleteClient(context, client.getClientId());
    }
  }

  protected String getToken() throws IOException {
    if (token == null) {
      token = requestToken(clients.get(0));
    }
    return token;
  }

  protected String requestToken(OAuthClient client) throws IOException {
    return requestToken(client, context);
  }

  protected String requestToken(OAuthClient client, PageContext contextToUse) throws IOException {
    final String tokenGetUrl =
        contextToUse.getBaseUrl()
            + "oauth/access_token?grant_type=client_credentials&client_id="
            + client.getClientId()
            + "&redirect_uri=default&client_secret="
            + client.getSecret();
    final HttpResponse response = execute(new HttpGet(tokenGetUrl), false);
    final JsonNode tokenNode = readJson(mapper, response);
    return tokenNode.get("access_token").asText();
  }

  protected String requestToken(String id) throws IOException {
    for (OAuthClient client : clients) {
      if (client.getClientId().equals(id)) {
        return requestToken(client);
      }
    }
    return null;
  }

  protected List<OAuthClient> getOAuthClients() {
    final List<Pair<String, String>> clients = Lists.newArrayList();
    final List<OAuthClient> oClients = Lists.newArrayList();
    addOAuthClients(clients);
    for (Pair<String, String> client : clients) {
      OAuthClient oClient = new OAuthClient();
      oClient.setName(client.getFirst());
      oClient.setClientId(client.getFirst());
      oClient.setUsername(client.getSecond());
      oClients.add(oClient);
    }
    return oClients;
  }

  protected abstract void addOAuthClients(List<Pair<String, String>> clients);

  protected HttpResponse download(String url, File downloadTo) throws Exception {
    return download(url, downloadTo, null);
  }

  protected HttpResponse download(String url, File downloadTo, String token) throws Exception {
    final HttpResponse response = execute(new HttpGet(url), false, token);
    final InputStream echoedContent = response.getEntity().getContent();

    OutputStream out = null;
    try {
      out = new BufferedOutputStream(new FileOutputStream(downloadTo));
      ByteStreams.copy(echoedContent, out);
      out.flush();
      return response;
    } finally {
      Closeables.close(out, true);
    }
  }

  protected JsonNode getEntity(String uri, String token, Object... params) throws IOException {
    HttpResponse response =
        execute(new HttpGet(appendQueryString(uri, queryString(params))), false, token);
    Assert.assertEquals(
        response.getStatusLine().getStatusCode(), 200, "Error getting a resource at " + uri);
    return mapper.readTree(response.getEntity().getContent());
  }

  protected HttpResponse postEntity(
      @Nullable String json, String uri, String token, boolean consume, Object... paramNameValues)
      throws IOException {
    final HttpPost request = new HttpPost(appendQueryString(uri, queryString(paramNameValues)));
    return putOrPostEntity(request, token, json, consume);
  }

  protected HttpResponse putEntity(
      @Nullable String json, String uri, String token, boolean consume, Object... paramNameValues)
      throws IOException {
    final HttpPut request = new HttpPut(appendQueryString(uri, queryString(paramNameValues)));
    return putOrPostEntity(request, token, json, consume);
  }

  protected HttpResponse putOrPostEntity(
      HttpEntityEnclosingRequestBase request, String token, @Nullable String json, boolean consume)
      throws IOException {
    if (json != null) {
      final StringEntity ent = new StringEntity(json, "UTF-8");
      ent.setContentType("application/json");
      request.setEntity(ent);
    }
    return execute(request, consume, token);
  }

  protected HttpResponse deleteResource(String uri, String token, Object... paramNameValues)
      throws IOException {
    return execute(
        new HttpDelete(appendQueryString(uri, queryString(paramNameValues))), false, token);
  }

  protected HttpPut getPut(String url, File file, Object... paramNameValues) throws IOException {
    final HttpPut put = new HttpPut(appendQueryString(url, queryString(paramNameValues)));
    InputStreamEntity inputStreamEntity =
        new InputStreamEntity(new FileInputStream(file), file.length());
    inputStreamEntity.setContentType("application/octet-stream");
    put.setEntity(inputStreamEntity);
    return put;
  }

  protected HttpPost getPost(String url, String jsonEntity) throws UnsupportedEncodingException {
    final HttpPost post = new HttpPost(url);
    final StringEntity string = new StringEntity(jsonEntity, ContentType.APPLICATION_JSON);
    post.setEntity(string);
    return post;
  }

  protected HttpPut getPut(String url, String jsonEntity) throws UnsupportedEncodingException {
    final HttpPut put = new HttpPut(url);
    final StringEntity string = new StringEntity(jsonEntity, ContentType.APPLICATION_JSON);
    put.setEntity(string);
    return put;
  }

  protected HttpResponse getPut(String url, JsonNode jsonNode, String token) throws IOException {
    JSONEntity jsonEntity = new JSONEntity(mapper, jsonNode);
    final HttpPut put = new HttpPut(url);
    put.setEntity(jsonEntity);
    HttpResponse retResponse = execute(put, false, token);
    return retResponse;
  }

  protected static void assertResponse(HttpResponse response, int status, String message) {
    AssertJUnit.assertEquals(message, status, response.getStatusLine().getStatusCode());
  }

  protected HttpResponse execute(HttpUriRequest request, boolean consume)
      throws ClientProtocolException, IOException {
    return execute(request, consume, null);
  }

  protected HttpResponse execute(HttpUriRequest request, boolean consume, String token)
      throws ClientProtocolException, IOException {
    if (token != null) {
      final Header tokenHeader = new BasicHeader("X-Authorization", "access_token=" + token);
      request.setHeader(tokenHeader);
    }

    try {
      HttpResponse response = createClient().execute(request);
      if (consume) {
        EntityUtils.consume(response.getEntity());
      }
      return response;
    } catch (SSLException ex) {
      throw new RuntimeException(
          "SSL exception attempting to talk connect to " + request.getURI(), ex);
    }
  }

  protected ObjectNode readJson(ObjectMapper mapper, HttpResponse response) throws IOException {
    InputStream content = null;
    try {
      content = response.getEntity().getContent();
      return (ObjectNode) mapper.readTree(content);
    } finally {
      if (content != null) {
        content.close();
      }
    }
  }

  protected String getLink(ObjectNode rootNode, String linkName) {
    ObjectNode linksNode = (ObjectNode) rootNode.get("links");
    if (linksNode == null) {
      return null;
    }
    JsonNode linkNode = linksNode.get(linkName);
    if (linkNode == null) {
      return null;
    }
    return linkNode.asText();
  }

  protected ArrayNode getArray(ObjectNode rootNode, String fieldName) {
    return (ArrayNode) rootNode.get(fieldName);
  }

  protected ObjectNode getObject(ObjectNode rootNode, String fieldName) {
    return (ObjectNode) rootNode.get(fieldName);
  }

  /**
   * Appends a query string to a URL, detecting if the URL already contains a query string
   *
   * @param url
   * @param queryString
   * @return
   */
  public String appendQueryString(String url, String queryString) {
    return url
        + (queryString == null || queryString.equals("")
            ? ""
            : (url.contains("?") ? '&' : '?') + queryString);
  }

  /**
   * @param paramNameValues An even number of strings indicating the parameter names and values i.e.
   *     "param1", "value1", "param2", "value2"
   * @return
   */
  public String queryString(Object... paramNameValues) {
    final List<NameValuePair> params = Lists.newArrayList();
    if (paramNameValues != null) {
      if (paramNameValues.length % 2 != 0) {
        throw new RuntimeException("Must supply an even number of paramNameValues");
      }
      for (int i = 0; i < paramNameValues.length; i += 2) {
        Object val = paramNameValues[i + 1];
        if (val != null) {
          params.add(new BasicNameValuePair(paramNameValues[i].toString(), val.toString()));
        }
      }
    }
    return queryString(params);
  }

  /**
   * Turns a list of NameValuePair into a query string. e.g param1=val1&param2=val2
   *
   * @param params
   * @return
   */
  public String queryString(List<NameValuePair> params) {
    if (params == null) {
      return null;
    }
    return URLEncodedUtils.format(params, "UTF-8");
  }

  protected String paramString(Map<String, ?> paramMap) {
    List<NameValuePair> params = Lists.newArrayList();
    if (paramMap != null) {
      for (Entry<String, ?> paramEntry : paramMap.entrySet()) {
        Object value = paramEntry.getValue();
        if (value != null) {
          params.add(new BasicNameValuePair(paramEntry.getKey(), value.toString()));
        }
      }
    }
    return URLEncodedUtils.format(params, "UTF-8");
  }

  /**
   * A bit crude, but should do the trick: 'available' and 'results' are fixed features of the
   * SearchBean class
   */
  protected JsonNode containsInSearchBeanResult(JsonNode json, String lookFor, String lookIn) {
    int available = json.get("available").asInt();
    JsonNode results = json.get("results");

    for (int i = 0; i < available; i++) {
      if (results.get(i).get(lookIn).asText().equals(lookFor)) {
        return results.get(i);
      }
    }
    return null;
  }

  protected String superSerialResponse(HttpResponse response) {
    try {
      final StringWriter sw = new StringWriter();
      CharStreams.copy(new InputStreamReader(response.getEntity().getContent()), sw);
      return sw.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
