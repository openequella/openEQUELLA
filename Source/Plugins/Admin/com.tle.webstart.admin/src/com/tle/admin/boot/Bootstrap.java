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

package com.tle.admin.boot;

import com.dytech.common.net.Proxy;
import com.dytech.devlib.Base64;
import com.dytech.edge.common.Version;
import com.tle.admin.PluginServiceImpl;
import com.tle.client.ListCookieHandler;
import com.tle.client.harness.HarnessInterface;
import com.tle.common.Check;
import com.tle.core.plugins.PluginAwareObjectInputStream;
import com.tle.core.plugins.PluginAwareObjectOutputStream;
import com.tle.core.remoting.RemotePluginDownloadService;
import com.tle.core.remoting.SessionLogin;
import com.tle.exceptions.BadCredentialsException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;

@SuppressWarnings("nls")
public final class Bootstrap {
  private static final Pattern LOCALE_REGEX =
      Pattern.compile("^([a-z][a-z])?(?:_([A-Z][A-Z])?(?:_(\\w+))?)?$");

  public static final String PROPERTY_PREFIX = "jnlp.";
  public static final String TOKEN_PARAMETER = PROPERTY_PREFIX + "SESSION";
  public static final String ENDPOINT_PARAMETER = PROPERTY_PREFIX + "ENDPOINT";
  public static final String LOCALE_PARAMETER = PROPERTY_PREFIX + "LOCALE";
  public static final String INSTITUTION_NAME_PARAMETER = PROPERTY_PREFIX + "INSTITUTIONNAME";
  public static final String USERNAME_PARAMETER = PROPERTY_PREFIX + "USERNAME";
  public static final String PASSWORD_PARAMETER = PROPERTY_PREFIX + "PASSWORD";
  public static final String PROXY_HOST = PROPERTY_PREFIX + "PROXYHOST";
  public static final String PROXY_PORT = PROPERTY_PREFIX + "PROXYPORT";
  public static final String PROXY_USERNAME = PROPERTY_PREFIX + "PROXYUSERNAME";
  public static final String PROXY_PASSWORD = PROPERTY_PREFIX + "PROXYPASSWORD";

  private final String endpointParam;
  private final Locale locale;

  public static void main(String[] args) {
    new Bootstrap().run();
  }

  private Bootstrap() {
    String endpointParam = System.getProperty(ENDPOINT_PARAMETER);
    if (endpointParam == null) {
      endpointParam = System.getProperty("ENDPOINT");
    }
    if (endpointParam == null) {
      throw new RuntimeException("ENDPOINT parameter not specified");
    }
    this.endpointParam = endpointParam;

    final String localeParam = System.getProperty(LOCALE_PARAMETER);
    if (localeParam != null) {
      locale = parseLocale(localeParam);
    } else {
      locale = Locale.getDefault();
    }

    final String proxyHostParam = System.getProperty(PROXY_HOST);
    final String proxyPortParam = System.getProperty(PROXY_PORT);
    if (proxyHostParam != null && proxyPortParam != null) {
      try {
        final int proxyPort = Integer.parseInt(proxyPortParam);
        Proxy.setProxy(
            proxyHostParam,
            proxyPort,
            System.getProperty(PROXY_USERNAME),
            System.getProperty(PROXY_PASSWORD));
      } catch (NumberFormatException nfe) {
        throw new RuntimeException("Invalid proxy port " + proxyPortParam);
      }
    }
  }

  private void run() {
    try {
      System.setSecurityManager(null);

      final URL endpointUrl = new URL(endpointParam);
      if (login(endpointUrl)) {
        final PluginServiceImpl pluginService =
            new PluginServiceImpl(
                endpointUrl,
                Version.load().getCommit(),
                createInvoker(RemotePluginDownloadService.class, endpointUrl));
        pluginService.registerPlugins();

        final HarnessInterface client =
            (HarnessInterface)
                pluginService.getBean("com.equella.admin", "com.tle.admin.AdminConsole");
        client.setPluginService(pluginService);
        client.setLocale(locale);
        client.setEndpointURL(endpointUrl);
        client.start();
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private boolean login(URL endpointUrl) {
    try {
      String tokenParam = System.getProperty(TOKEN_PARAMETER);
      if (tokenParam != null) {
        String token =
            new String(new Base64().decode(System.getProperty(TOKEN_PARAMETER)), "UTF-8");
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        SessionLogin.postLogin(endpointUrl, params);
        return true;
      } else {
        // bring up username/password modal
        ListCookieHandler lch = new ListCookieHandler();
        lch.setIgnoreCookieOverrideAttempts(true);
        CookieHandler.setDefault(lch);

        String username = System.getProperty(USERNAME_PARAMETER);
        String password = System.getProperty(PASSWORD_PARAMETER);

        LoginDialog loginDialog = new LoginDialog();
        try {
          if (username != null && password != null) {
            if (tryLogin(endpointUrl, username, password)) {
              return true;
            }
          }

          while (true) {
            loginDialog.setUsername(username);
            loginDialog.setVisible(true);
            if (loginDialog.getResult() == LoginDialog.RESULT_OK) {
              username = loginDialog.getUsername();
              password = loginDialog.getPassword();
              if (tryLogin(endpointUrl, username, password)) {
                return true;
              } else {
                loginDialog.setErrorMessage("Your credentials are invalid.");
              }
            } else {
              return false;
            }
          }
        } finally {
          loginDialog.dispose();
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean tryLogin(URL endpointUrl, String username, String password)
      throws IOException {
    try {
      Map<String, String> params = new HashMap<>();
      params.put("username", username);
      params.put("password", password);
      SessionLogin.postLogin(endpointUrl, params);
      return true;
    } catch (BadCredentialsException e) {
      return false;
    }
  }

  private static Locale parseLocale(String localeString) {
    if (localeString != null) {
      Matcher m = LOCALE_REGEX.matcher(localeString.trim());
      if (m.matches()) {
        return new Locale(
            Check.nullToEmpty(m.group(1)),
            Check.nullToEmpty(m.group(2)),
            Check.nullToEmpty(m.group(3)));
      }
    }
    throw new RuntimeException("Error parsing locale: " + localeString);
  }

  @SuppressWarnings("unchecked")
  protected static <T> T createInvoker(Class<T> clazz, URL endpointUrl) {
    HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
    try {
      factory.setServiceUrl(
          new URL(endpointUrl, "invoker/" + clazz.getName() + ".service").toString());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    factory.setServiceInterface(clazz);
    factory.setHttpInvokerRequestExecutor(new PluginAwareSimpleHttpInvokerRequestExecutor());
    factory.afterPropertiesSet();
    return (T) factory.getObject();
  }

  public static class PluginAwareSimpleHttpInvokerRequestExecutor
      extends SimpleHttpInvokerRequestExecutor {
    @Override
    protected ObjectInputStream createObjectInputStream(InputStream is, String codebaseUrl)
        throws IOException {
      return new PluginAwareObjectInputStream(is);
    }

    @Override
    protected void writeRemoteInvocation(RemoteInvocation invocation, OutputStream os)
        throws IOException {
      ObjectOutputStream oos = new PluginAwareObjectOutputStream(decorateOutputStream(os));
      try {
        doWriteRemoteInvocation(invocation, oos);
        oos.flush();
      } finally {
        oos.close();
      }
    }
  }
}
