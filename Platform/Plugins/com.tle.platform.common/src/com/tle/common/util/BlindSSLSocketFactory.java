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

package com.tle.common.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BlindSSLSocketFactoryTest Simple test to show an Active Directory (LDAP) and HTTPS connection
 * without verifying the server's certificate. http://blog.platinumsolutions.com/node/79
 *
 * @author Mike McKinney, Platinum Solutions, Inc.
 */
@SuppressWarnings("nls")
public class BlindSSLSocketFactory extends SSLSocketFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(BlindSSLSocketFactory.class);
  private static SSLSocketFactory originalFactory;
  private static HostnameVerifier originalHostnameVerifier;

  public static TrustManager createTrustManager() {
    return new X509TrustManager() {
      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      @Override
      public void checkClientTrusted(X509Certificate[] c, String a) {
        // Ignore
      }

      @Override
      public void checkServerTrusted(X509Certificate[] c, String a) {
        // IGNORE
      }
    };
  }

  public static SSLContext createBlindSSLContext() {
    // Create a trust manager that will purposefully fall down on the job
    TrustManager manager = createTrustManager();
    TrustManager[] blindTrustMan = new TrustManager[] {manager};

    // create our "blind" ssl socket factory with our lazy trust manager
    try {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, blindTrustMan, new SecureRandom());
      return sc;
    } catch (GeneralSecurityException ex) {
      throw new RuntimeException("Error creating blind SSL context", ex);
    }
  }

  private static SSLSocketFactory blindFactory = createBlindSSLContext().getSocketFactory();

  /**
   * @see javax.net.SocketFactory#getDefault()
   */
  public static synchronized SocketFactory getDefault() {
    return new BlindSSLSocketFactory();
  }

  public static SSLSocketFactory getDefaultSSL() {
    return new BlindSSLSocketFactory();
  }

  /** Easy way of not worrying about stupid SSL validation. */
  public static void register() {
    if (!(HttpsURLConnection.getDefaultSSLSocketFactory() instanceof BlindSSLSocketFactory)) {
      originalFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
      originalHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
      LOGGER.info("Registering BlindSSLSocketFactory");

      HttpsURLConnection.setDefaultSSLSocketFactory(getDefaultSSL());
      // I'm not sure if you need this...
      HttpsURLConnection.setDefaultHostnameVerifier(
          new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
              return true;
            }
          });
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (originalFactory != null) {
      HttpsURLConnection.setDefaultSSLSocketFactory(originalFactory);
    }
    if (originalHostnameVerifier != null) {
      HttpsURLConnection.setDefaultHostnameVerifier(originalHostnameVerifier);
    }
    super.finalize();
  }

  @Override
  public Socket createSocket(String arg0, int arg1) throws IOException {
    return blindFactory.createSocket(arg0, arg1);
  }

  @Override
  public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
    return blindFactory.createSocket(arg0, arg1);
  }

  @Override
  public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
    return blindFactory.createSocket(arg0, arg1, arg2, arg3);
  }

  @Override
  public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3)
      throws IOException {
    return blindFactory.createSocket(arg0, arg1, arg2, arg3);
  }

  @Override
  public Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException {
    return blindFactory.createSocket(socket, s, i, flag);
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return blindFactory.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return blindFactory.getSupportedCipherSuites();
  }
}
