/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.net.URL;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.tle.common.applet.SessionHolder;
import com.tle.common.applet.client.ClientProxyFactory;
import com.tle.common.applet.client.ClientService;
import com.tle.core.remoting.RemoteUserService;

public class ClientServiceImpl implements ClientService {
  private static final Log LOGGER = LogFactory.getLog(ClientService.class);

  private final SessionHolder session;
  private final ClassToInstanceMap<Object> services = MutableClassToInstanceMap.create();

  public ClientServiceImpl(SessionHolder session) {
    this.session = session;
  }

  private Object getLocalService(Class<?> clazz) {
    try {
      return ServiceManager.lookup(clazz.getName());
    } catch (UnavailableServiceException e) {
      throw new RuntimeException(e);
    }
  }

  private BasicService getBasicService() {
    return (BasicService) getLocalService(BasicService.class);
  }

  @Override
  public String getParameter(String key) {
    return System.getProperty(key);
  }

  @Override
  public void showDocument(URL url, String string) {
    getBasicService().showDocument(url);
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

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getService(Class<T> clazz) {
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
