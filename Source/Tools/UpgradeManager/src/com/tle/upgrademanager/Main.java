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
import com.tle.upgrademanager.helpers.Deployer;
import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To debug this:
 *
 * <ul>
 *   <li>Create a <code>version.properties</code> file in <code>src/com/tle/upgrademanager</code>.
 *   <li>Set program arguments: <code>start</code>.
 *   <li>Set VM arguments: <code>-Dequella.install.directory=C:/svn/tle/trunk -DDEBUG=true</code>.
 *       Please note that the directory should have an oeq installed.
 * </ul>
 */
public class Main {
  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
  // Flag to stop the manager.
  private static volatile boolean stop;

  public static final String EQUELLA_INSTALL_DIRECTORY_KEY = "equella.install.directory";

  private final List<Filter> filters;
  private final ManagerConfig config;

  private static HttpServer server;

  /**
   * Method to be called when the Manager is started. According to the Procrun documentation, this
   * method should not return until the stop method has been called in JVM mode.
   */
  public static void main(String[] args) throws Exception {
    if (Check.isEmpty(args) || Check.isEmpty(args[0]) || args[0].equals("start")) {
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

  public void start() throws Exception {
    startServer();
  }

  public void stop() {
    if (server != null) {
      server.stop(4);
    }
  }

  // Expected by jsvc when running in daemon mode
  public void init(String[] arguments) {}

  // Expected by jsvc when running in daemon mode
  public void destroy() {}

  public Main() throws Exception {
    filters = new ArrayList<>();
    filters.add(new SuperDuperFilter());

    try (InputStream in = Main.class.getResourceAsStream("/version.properties")) // $NON-NLS-1$
    {
      config = new ManagerConfig(new File(findInstall()), in);
    }
  }

  public void startServer() throws Exception {
    if (isRunOffline()) {
      LOGGER.info("Starting offline upgrade");
      executeUpgrader();
    }

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

  private void executeUpgrader() {
    File managerDir = config.getManagerDir();
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
          new File(managerDir, Deployer.UPGRADER_JAR).getAbsolutePath()
        };
    LOGGER.info(
        "Executing {} with working dir {}", Arrays.asList(args), managerDir.getAbsolutePath());
    ExecResult result = ExecUtils.exec(args, null, managerDir);
    if (result.getExitStatus() != 0) {
      LOGGER.error("Error running upgrader");
      LOGGER.error("Out:{}", result.getStdout());
      LOGGER.error("Err:{}", result.getStderr());
    }
  }

  private boolean isRunOffline() {
    return new File(config.getManagerDir(), "runoffline").exists();
  }

  private String findInstall() {
    return Optional.ofNullable(System.getProperty(EQUELLA_INSTALL_DIRECTORY_KEY))
        .orElseGet(Main::getEquellaInstallPath);
  }

  /**
   * Attempts to find the install path by looking for a known file on the classpath. This will only
   * work if the classpath is set up correctly, which it is when running from the service wrapper.
   */
  private static String getEquellaInstallPath() {
    final String knownFile = "config.properties";

    return Optional.ofNullable(Main.class.getClassLoader().getResource(knownFile))
        .filter(r -> r.getProtocol().equals("file"))
        .map(r -> new File(URLDecoder.decode(r.getFile(), StandardCharsets.UTF_8)))
        .map(f -> f.getParentFile().getParentFile().getAbsolutePath())
        .orElseThrow(() -> new RuntimeException("Missing " + knownFile + " from classpath"));
  }
}
