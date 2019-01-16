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

package com.tle.common.applet;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SessionHolder {
  static final Log LOGGER = LogFactory.getLog(SessionHolder.class);

  private final LoginService loginService;
  private final KeepAliveTask keepAliveTask;
  private final URL url;

  public SessionHolder(URL url) {
    this.url = url;
    loginService = new LoginService(this);
    keepAliveTask = new KeepAliveTask(this);
  }

  public void enableKeepAlive(boolean b) {
    if (b) {
      keepAliveTask.onSchedule();
    } else {
      keepAliveTask.cancel();
    }
  }

  public URL getUrl() {
    return url;
  }

  public LoginService getLoginService() {
    return loginService;
  }
}
