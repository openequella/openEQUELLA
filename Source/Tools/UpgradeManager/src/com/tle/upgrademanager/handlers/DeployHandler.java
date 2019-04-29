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

package com.tle.upgrademanager.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.tle.upgrademanager.ManagerConfig;
import com.tle.upgrademanager.helpers.AjaxState;
import com.tle.upgrademanager.helpers.Deployer;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("nls")
public class DeployHandler extends UrlDispatchHandler {
  private static final Log LOGGER = LogFactory.getLog(DeployHandler.class);

  private final ManagerConfig config;
  private final AjaxState ajax;

  public DeployHandler(ManagerConfig config, AjaxState ajax) {
    this.config = config;
    this.ajax = ajax;
  }

  public void download(HttpExchange exchange) throws IOException {
    HttpExchangeUtils.respondRedirect(exchange, "/pages/"); // $NON-NLS-1$
  }

  public void deploy(HttpExchange exchange) throws IOException {
    final String ajaxId = UUID.randomUUID().toString();
    final String path = "/deploy/deploy/"; // $NON-NLS-1$
    final String filename = exchange.getRequestURI().getPath().substring(path.length());

    new Thread() {
      @Override
      public void run() {
        LOGGER.debug("Running deployer on version " + filename);
        Deployer deploy = new Deployer(ajaxId, ajax, config);
        deploy.deploy(filename);
      }
    }.start();

    HttpExchangeUtils.respondRedirect(exchange, "/pages/progress/" + ajaxId); // $NON-NLS-1$
  }
}
