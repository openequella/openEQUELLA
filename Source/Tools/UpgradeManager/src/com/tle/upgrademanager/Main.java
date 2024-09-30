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

package com.tle.upgrademanager;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import com.tle.common.Check;
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;
import com.tle.upgrademanager.filter.SuperDuperFilter;
import com.tle.upgrademanager.handlers.AjaxProgressHandler;
import com.tle.upgrademanager.handlers.DeployHandler;
import com.tle.upgrademanager.handlers.PagesHandler;
import com.tle.upgrademanager.handlers.ServerHandler;
import com.tle.upgrademanager.handlers.UploadHandler;
import com.tle.upgrademanager.handlers.WebRootHandler;
import com.tle.upgrademanager.helpers.AjaxState;
import com.tle.upgrademanager.helpers.AjaxStateImpl;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * To debug this 1. Create a version.properties file in src/com/tle/upgrademanager (you would get
 * this from the root level build: ant generate-version-properties) 2. VM arguments:
 * -Dequella.install.directory=c:/svn/tle/trunk -DDEBUG=true Classpath: wherever the
 * config.properties file lives
 */
public class Main {
  private static final Log LOGGER = LogFactory.getLog(Main.class);
  // Flag to stop the manager.
  private static volatile boolean stop;

  public static final String EQUELLA_INSTALL_DIRECTORY_KEY =
      "equella.install.directory"; //$NON-NLS-1$

  private final List<Filter> filters;
  private final ManagerConfig config;

  private static HttpServer server;

  /**
   * Method to be called when the Manager is started. According to the Procrun documentation, this
   * method should not return until the stop method has been called in JVM mode.
   */
  public static void main(String[] args) throws Exception {
    if (Check.isEmpty(args) || Check.isEmpty(args[0]) || args[0].equals("start")) // $NON-NLS-1$
    {
      Main m = new Main();
      m.startServer();
      while (!stop) {
        Thread.sleep(1000);
      }
    } else if (args[0].equals("stop")) {
      stop = true;
      if (server != null) {
        server.stop(4);
      }
    }
  }

  public void init(String[] arguments) {}

  public void start() throws Exception {
    startServer();
  }

  public void stop() {
    if (server != null) {
      server.stop(4);
    }
  }

  public void destroy() {}

  public Main() throws Exception {
    filters = new ArrayList<Filter>();
    filters.add(new SuperDuperFilter());

    try (InputStream in = Main.class.getResourceAsStream("/version.properties")) // $NON-NLS-1$
    {
      config = new ManagerConfig(new File(findInstall()), in);
    }
  }

  public void startServer() throws Exception {
    executeUpgrader();

    Authenticator auth = new MyAuthenticator(config);
    server =
        HttpServerProvider.provider()
            .createHttpServer(
                new InetSocketAddress(config.getManagerDetails().getManagerPort()), 50);

    AjaxState ajaxState = new AjaxStateImpl();

    createContext(server, "/", new WebRootHandler()); // $NON-NLS-1$
    createContext(server, "/pages/", new PagesHandler(config), auth); // $NON-NLS-1$
    createContext(server, "/server/", new ServerHandler(config), auth); // $NON-NLS-1$
    createContext(server, "/deploy/", new DeployHandler(config, ajaxState), auth); // $NON-NLS-1$
    createContext(server, "/ajax/", new AjaxProgressHandler(ajaxState)); // $NON-NLS-1$
    createContext(server, "/upload/", new UploadHandler(config)); // $NON-NLS-1$

    server.start();
  }

  private void createContext(HttpServer server, String path, HttpHandler handler) {
    createContext(server, path, handler, null);
  }

  private void createContext(
      HttpServer server, String path, HttpHandler handler, Authenticator auth) {
    final HttpContext context = server.createContext(path, handler);
    if (auth != null) {
      context.setAuthenticator(auth);
    }
    context.getFilters().addAll(filters);
  }

  @SuppressWarnings("nls")
  private void executeUpgrader() {
    File managerDir = config.getManagerDir();
    if (new File(managerDir, "runoffline").exists()) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      LOGGER.info("Executing upgrader");
      final String[] args =
          new String[] {
            config.getJavaBin().getAbsolutePath(),
            "-Dequella.offline=true",
            "-Dlog4j2.configurationFile=file:upgrader-log4j.yaml",
            "-classpath",
            managerDir.getAbsolutePath(),
            "-jar",
            new File(managerDir, "database-upgrader.jar").getAbsolutePath()
          };
      LOGGER.info(
          "Executing " + Arrays.asList(args) + " with working dir " + managerDir.getAbsolutePath());
      ExecResult result = ExecUtils.exec(args, null, managerDir);
      if (result.getExitStatus() != 0) {
        LOGGER.error("Error running upgrader");
        LOGGER.error("Out:" + result.getStdout());
        LOGGER.error("Err:" + result.getStderr());
      }
    }
  }

  @SuppressWarnings("nls")
  private String findInstall() {
    String equellaInstall = System.getProperty(EQUELLA_INSTALL_DIRECTORY_KEY);
    if (equellaInstall == null) {
      URL resource = Main.class.getClassLoader().getResource("config.properties");
      if (resource.getProtocol().equals("file")) {
        File configFile;
        try {
          configFile = new File(URLDecoder.decode(resource.getFile(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
        equellaInstall = configFile.getParentFile().getParentFile().getAbsolutePath();
      } else {
        throw new RuntimeException("Missing config.properties from classpath");
      }
    }
    return equellaInstall;
  }
}
