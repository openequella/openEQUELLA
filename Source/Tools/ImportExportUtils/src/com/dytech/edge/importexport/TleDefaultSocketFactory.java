package com.dytech.edge.importexport;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.SocketFactory;
import org.apache.axis.components.net.TransportClientProperties;
import org.apache.axis.encoding.Base64;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;

/**
 * Copy of DefaultSocketFactory but using TleTransportClientPropertiesFactory so the proxy settings
 * arent cached
 *
 * @author will
 */
public class TleDefaultSocketFactory implements SocketFactory {

  /** Field log */
  protected static Log log = LogFactory.getLog(TleDefaultSocketFactory.class.getName());

  /** Field CONNECT_TIMEOUT */
  public static String CONNECT_TIMEOUT = "axis.client.connect.timeout";

  /** attributes */
  protected Hashtable attributes = null;

  private static boolean plain;
  private static Class inetClass;
  private static Constructor inetConstructor;
  private static Constructor socketConstructor;
  private static Method connect;

  static {
    try {
      inetClass = Class.forName("java.net.InetSocketAddress");
      plain = false;
      inetConstructor = inetClass.getConstructor(new Class[] {String.class, int.class});
      socketConstructor = Socket.class.getConstructor(new Class[] {});
      connect =
          Socket.class.getMethod("connect", new Class[] {inetClass.getSuperclass(), int.class});
    } catch (Exception e) {
      plain = true;
    }
  }

  /**
   * Constructor is used only by subclasses.
   *
   * @param attributes
   */
  public TleDefaultSocketFactory(Hashtable attributes) {
    this.attributes = attributes;
  }

  /**
   * Creates a socket.
   *
   * @param host
   * @param port
   * @param otherHeaders
   * @param useFullURL
   * @return Socket
   * @throws Exception
   */
  @Override
  public Socket create(String host, int port, StringBuffer otherHeaders, BooleanHolder useFullURL)
      throws Exception {

    int timeout = 0;
    if (attributes != null) {
      String value = (String) attributes.get(CONNECT_TIMEOUT);
      timeout = (value != null) ? Integer.parseInt(value) : 0;
    }

    TransportClientProperties tcp =
        TleTransportClientPropertiesFactory.create("http"); // $NON-NLS-1$

    Socket sock = null;
    boolean hostInNonProxyList = isHostInNonProxyList(host, tcp.getNonProxyHosts());

    if (tcp.getProxyUser().length() != 0) {
      StringBuilder tmpBuf = new StringBuilder();

      tmpBuf.append(tcp.getProxyUser()).append(":").append(tcp.getProxyPassword());
      otherHeaders
          .append(HTTPConstants.HEADER_PROXY_AUTHORIZATION)
          .append(": Basic ")
          .append(Base64.encode(tmpBuf.toString().getBytes()))
          .append("\r\n");
    }
    if (port == -1) {
      port = 80;
    }
    if ((tcp.getProxyHost().length() == 0)
        || (tcp.getProxyPort().length() == 0)
        || hostInNonProxyList) {
      sock = create(host, port, timeout);
      if (log.isDebugEnabled()) {
        log.debug(Messages.getMessage("createdHTTP00"));
      }
    } else {
      sock = create(tcp.getProxyHost(), new Integer(tcp.getProxyPort()).intValue(), timeout);
      if (log.isDebugEnabled()) {
        log.debug(Messages.getMessage("createdHTTP01", tcp.getProxyHost(), tcp.getProxyPort()));
      }
      useFullURL.value = true;
    }
    return sock;
  }

  /**
   * Creates a socket with connect timeout using reflection API
   *
   * @param host
   * @param port
   * @param timeout
   * @return
   * @throws Exception
   */
  private static Socket create(String host, int port, int timeout) throws Exception {
    Socket sock = null;
    if (plain || timeout == 0) {
      sock = new Socket(host, port);
    } else {
      Object address = inetConstructor.newInstance(new Object[] {host, new Integer(port)});
      sock = (Socket) socketConstructor.newInstance(new Object[] {});
      connect.invoke(sock, new Object[] {address, new Integer(timeout)});
    }
    return sock;
  }

  /**
   * Check if the specified host is in the list of non proxy hosts.
   *
   * @param host host name
   * @param nonProxyHosts string containing the list of non proxy hosts
   * @return true/false
   */
  protected boolean isHostInNonProxyList(String host, String nonProxyHosts) {

    if ((nonProxyHosts == null) || (host == null)) {
      return false;
    }

    /*
     * The http.nonProxyHosts system property is a list enclosed in double
     * quotes with items separated by a vertical bar.
     */
    StringTokenizer tokenizer = new StringTokenizer(nonProxyHosts, "|\"");

    while (tokenizer.hasMoreTokens()) {
      String pattern = tokenizer.nextToken();

      if (log.isDebugEnabled()) {
        log.debug(Messages.getMessage("match00", new String[] {"HTTPSender", host, pattern}));
      }
      if (match(pattern, host, false)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Matches a string against a pattern. The pattern contains two special characters: '*' which
   * means zero or more characters,
   *
   * @param pattern the (non-null) pattern to match against
   * @param str the (non-null) string that must be matched against the pattern
   * @param isCaseSensitive
   * @return <code>true</code> when the string matches against the pattern, <code>false</code>
   *     otherwise.
   */
  protected static boolean match(String pattern, String str, boolean isCaseSensitive) {

    char[] patArr = pattern.toCharArray();
    char[] strArr = str.toCharArray();
    int patIdxStart = 0;
    int patIdxEnd = patArr.length - 1;
    int strIdxStart = 0;
    int strIdxEnd = strArr.length - 1;
    char ch;
    boolean containsStar = false;

    for (int i = 0; i < patArr.length; i++) {
      if (patArr[i] == '*') {
        containsStar = true;
        break;
      }
    }
    if (!containsStar) {

      // No '*'s, so we make a shortcut
      if (patIdxEnd != strIdxEnd) {
        return false; // Pattern and string do not have the same size
      }
      for (int i = 0; i <= patIdxEnd; i++) {
        ch = patArr[i];
        if (isCaseSensitive && (ch != strArr[i])) {
          return false; // Character mismatch
        }
        if (!isCaseSensitive && (Character.toUpperCase(ch) != Character.toUpperCase(strArr[i]))) {
          return false; // Character mismatch
        }
      }
      return true; // String matches against pattern
    }
    if (patIdxEnd == 0) {
      return true; // Pattern contains only '*', which matches anything
    }

    // Process characters before first star
    while ((ch = patArr[patIdxStart]) != '*' && (strIdxStart <= strIdxEnd)) {
      if (isCaseSensitive && (ch != strArr[strIdxStart])) {
        return false; // Character mismatch
      }
      if (!isCaseSensitive
          && (Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxStart]))) {
        return false; // Character mismatch
      }
      patIdxStart++;
      strIdxStart++;
    }
    if (strIdxStart > strIdxEnd) {

      // All characters in the string are used. Check if only '*'s are
      // left in the pattern. If so, we succeeded. Otherwise failure.
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (patArr[i] != '*') {
          return false;
        }
      }
      return true;
    }

    // Process characters after last star
    while ((ch = patArr[patIdxEnd]) != '*' && (strIdxStart <= strIdxEnd)) {
      if (isCaseSensitive && (ch != strArr[strIdxEnd])) {
        return false; // Character mismatch
      }
      if (!isCaseSensitive
          && (Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxEnd]))) {
        return false; // Character mismatch
      }
      patIdxEnd--;
      strIdxEnd--;
    }
    if (strIdxStart > strIdxEnd) {

      // All characters in the string are used. Check if only '*'s are
      // left in the pattern. If so, we succeeded. Otherwise failure.
      for (int i = patIdxStart; i <= patIdxEnd; i++) {
        if (patArr[i] != '*') {
          return false;
        }
      }
      return true;
    }

    // process pattern between stars. padIdxStart and patIdxEnd point
    // always to a '*'.
    while ((patIdxStart != patIdxEnd) && (strIdxStart <= strIdxEnd)) {
      int patIdxTmp = -1;

      for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
        if (patArr[i] == '*') {
          patIdxTmp = i;
          break;
        }
      }
      if (patIdxTmp == patIdxStart + 1) {

        // Two stars next to each other, skip the first one.
        patIdxStart++;
        continue;
      }

      // Find the pattern between padIdxStart & padIdxTmp in str between
      // strIdxStart & strIdxEnd
      int patLength = (patIdxTmp - patIdxStart - 1);
      int strLength = (strIdxEnd - strIdxStart + 1);
      int foundIdx = -1;

      strLoop:
      for (int i = 0; i <= strLength - patLength; i++) {
        for (int j = 0; j < patLength; j++) {
          ch = patArr[patIdxStart + j + 1];
          if (isCaseSensitive && (ch != strArr[strIdxStart + i + j])) {
            continue strLoop;
          }
          if (!isCaseSensitive
              && (Character.toUpperCase(ch)
                  != Character.toUpperCase(strArr[strIdxStart + i + j]))) {
            continue strLoop;
          }
        }
        foundIdx = strIdxStart + i;
        break;
      }
      if (foundIdx == -1) {
        return false;
      }
      patIdxStart = patIdxTmp;
      strIdxStart = foundIdx + patLength;
    }

    // All characters in the string are used. Check if only '*'s are left
    // in the pattern. If so, we succeeded. Otherwise failure.
    for (int i = patIdxStart; i <= patIdxEnd; i++) {
      if (patArr[i] != '*') {
        return false;
      }
    }
    return true;
  }
}
