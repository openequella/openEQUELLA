/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.connectors.blackboard.service.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * EasySSLProtocolSocketFactory can be used to creats SSL {@link Socket}s that accept self-signed
 * certificates.
 *
 * <p>This socket factory SHOULD NOT be used for productive systems due to security reasons, unless
 * it is a concious decision and you are perfectly aware of security implications of accepting
 * self-signed certificates
 *
 * <p>Example of using custom protocol socket factory for a specific host:
 *
 * <pre>
 * Protocol easyhttps = new Protocol(&quot;https&quot;, new EasySSLProtocolSocketFactory(), 443);
 *
 * URI uri = new URI(&quot;https://localhost/&quot;, true);
 * // use relative url only
 * GetMethod httpget = new GetMethod(uri.getPathQuery());
 * HostConfiguration hc = new HostConfiguration();
 * hc.setHost(uri.getHost(), uri.getPort(), easyhttps);
 * HttpClient client = new HttpClient();
 * client.executeMethod(hc, httpget);
 * </pre>
 *
 * <p>Example of using custom protocol socket factory per default instead of the standard one:
 *
 * <pre>
 * Protocol easyhttps = new Protocol(&quot;https&quot;, new EasySSLProtocolSocketFactory(), 443);
 * Protocol.registerProtocol(&quot;https&quot;, easyhttps);
 *
 * HttpClient client = new HttpClient();
 * GetMethod httpget = new GetMethod(&quot;https://localhost/&quot;);
 * client.executeMethod(httpget);
 * </pre>
 *
 * @author <a href="mailto:oleg -at- ural.ru">Oleg Kalnichevski</a>
 *     <p>DISCLAIMER: HttpClient developers DO NOT actively support this component. The component is
 *     provided as a reference material, which may be inappropriate for use without additional
 *     customization.
 */
public class EasySSLProtocolSocketFactory implements SecureProtocolSocketFactory {

  /** Log object for this class. */
  private static final Log LOG = LogFactory.getLog(EasySSLProtocolSocketFactory.class);

  private SSLContext sslcontext = null;

  /** Constructor for EasySSLProtocolSocketFactory. */
  public EasySSLProtocolSocketFactory() {
    super();
  }

  private static SSLContext createEasySSLContext() {
    try {
      SSLContext context = SSLContext.getInstance("SSL");
      context.init(
          null,
          new TrustManager[] {
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
            }
          },
          null);
      return context;
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new HttpClientError(e.toString());
    }
  }

  private SSLContext getSSLContext() {
    if (this.sslcontext == null) {
      this.sslcontext = createEasySSLContext();
    }
    return this.sslcontext;
  }

  /**
   * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
   */
  @Override
  public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort)
      throws IOException, UnknownHostException {

    return getSSLContext().getSocketFactory().createSocket(host, port, clientHost, clientPort);
  }

  /**
   * Attempts to get a new socket connection to the given host within the given time limit.
   *
   * <p>To circumvent the limitations of older JREs that do not support connect timeout a controller
   * thread is executed. The controller thread attempts to create a new socket within the given
   * limit of time. If socket constructor does not return until the timeout expires, the controller
   * terminates and throws an {@link ConnectTimeoutException}
   *
   * @param host the host name/IP
   * @param port the port on the host
   * @param clientHost the local host name/IP to bind the socket to
   * @param clientPort the port on the local machine
   * @param params {@link HttpConnectionParams Http connection parameters}
   * @return Socket a new socket
   * @throws IOException if an I/O error occurs while creating the socket
   * @throws UnknownHostException if the IP address of the host cannot be determined
   */
  @Override
  public Socket createSocket(
      final String host,
      final int port,
      final InetAddress localAddress,
      final int localPort,
      final HttpConnectionParams params)
      throws IOException, UnknownHostException, ConnectTimeoutException {
    if (params == null) {
      throw new IllegalArgumentException("Parameters may not be null");
    }
    int timeout = params.getConnectionTimeout();
    SocketFactory socketfactory = getSSLContext().getSocketFactory();
    if (timeout == 0) {
      return socketfactory.createSocket(host, port, localAddress, localPort);
    } else {
      Socket socket = socketfactory.createSocket();
      SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
      SocketAddress remoteaddr = new InetSocketAddress(host, port);
      socket.bind(localaddr);
      socket.connect(remoteaddr, timeout);
      return socket;
    }
  }

  /**
   * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
   */
  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return getSSLContext().getSocketFactory().createSocket(host, port);
  }

  /**
   * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
   */
  @Override
  public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
      throws IOException, UnknownHostException {
    return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
  }

  @Override
  public boolean equals(Object obj) {
    return ((obj != null) && obj.getClass().equals(EasySSLProtocolSocketFactory.class));
  }

  @Override
  public int hashCode() {
    return EasySSLProtocolSocketFactory.class.hashCode();
  }
}
