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

package com.tle.client.impl;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.tle.client.guice.ClientModule;
import com.tle.common.applet.SessionHolder;
import com.tle.common.applet.client.ClientProxyFactory;
import com.tle.common.applet.client.ClientService;
import com.tle.core.remoting.RemoteUserService;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientServiceImpl implements ClientService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientServiceImpl.class);

  private final SessionHolder session;
  private final ClassToInstanceMap<Object> services = MutableClassToInstanceMap.create();

  private final Injector injector;

  public ClientServiceImpl(SessionHolder session) {
    this.session = session;

    LOGGER.debug("Starting up Guice");
    this.injector = Guice.createInjector(new ClientModule(session, this));
  }

  @Override
  public String getParameter(String key) {
    return System.getProperty(key);
  }

  @Override
  public void showDocument(URL url) {
    if (Desktop.isDesktopSupported()) {
      try {
        Desktop.getDesktop().browse(url.toURI());
      } catch (IOException | URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void stop() {
    LOGGER.info("Stopping the Admin Console"); // $NON-NLS-1$
    try {
      session.getLoginService().logout();
    } catch (Exception e) {
      LOGGER.error("Error logging out", e); // $NON-NLS-1$
    }

    // Sonar disapproves of System.exit:
    // ..."shuts down the entire Java virtual machine. This should only been done when it is
    // appropriate"
    // OK, so here it's appropriate.
    System.exit(0); // NOSONAR
  }

  @Override
  public URL getServerURL() {
    return session.getUrl();
  }

  @Override
  public SessionHolder getSession() {
    return session;
  }

  @Override
  public <T> T getService(Class<T> clazz) {
    // If one of the new classes managed with Guice, then use Guice to create it
    if (injector.getExistingBinding(Key.get(clazz)) != null) {
      return injector.getInstance(clazz);
    }

    // Otherwise, use the old HTTP Invoker service instantiation method
    return getInvokerService(clazz);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getInvokerService(Class<T> clazz) {
    synchronized (services) {
      T t = services.getInstance(clazz);
      if (t == null) {
        t =
            ClientProxyFactory.createSessionProxy(
                this,
                clazz,
                "invoker/"
                    + clazz.getName() // $NON-NLS-1$
                    + ".service"); //$NON-NLS-1$

        if (t instanceof RemoteUserService) {
          t = (T) new CachingUserServiceImpl((RemoteUserService) t);
        }
        services.put(clazz, t);
      }

      return t;
    }
  }
}
