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

package com.tle.tomcat.service.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.tle.common.Check;
import com.tle.core.application.StartupBean;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.DataSourceService;
import com.tle.core.migration.impl.HibernateMigrationService;
import com.tle.core.zookeeper.ZookeeperService;
import com.tle.tomcat.events.TomcatRestartListener;
import com.tle.tomcat.service.TomcatService;
import com.tle.web.dispatcher.RequestDispatchFilter;
import io.prometheus.client.exporter.MetricsServlet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.session.FileStore;
import org.apache.catalina.session.JDBCStore;
import org.apache.catalina.session.PersistentManager;
import org.apache.catalina.session.StoreBase;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.RemoteIpValve;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

@Bind(TomcatService.class)
@Singleton
@SuppressWarnings("nls")
public class TomcatServiceImpl implements TomcatService, StartupBean, TomcatRestartListener {

  private static final String BIO_HTTP = "org.apache.coyote.http11.Http11Protocol";
  private static final String BIO_AJP = "org.apache.coyote.ajp.AjpProtocol";

  private static final Log LOGGER = LogFactory.getLog(TomcatServiceImpl.class);

  private String tomcatContextPath;
  private String tomcatBasePath;

  @Inject(optional = true)
  @Named("userService.useXForwardedFor")
  private boolean useXForwardedFor;

  @Inject
  @Named("http.port")
  private int httpPort;

  @Inject
  @Named("https.port")
  private int httpsPort;

  @Inject
  @Named("ajp.port")
  private int ajpPort;

  @Inject
  @Named("ajp.address")
  private String ajpAddress;

  @Inject
  @Named("ajp.secret")
  private String ajpSecret;

  @Inject
  @Named("ajp.secret.required")
  private boolean ajpSecretRequired;

  @Inject
  @Named("tomcat.max.threads")
  private int maxThreads;

  @Inject(optional = true)
  @Named("jvmroute.id")
  private String jvmRouteId;

  @Inject(optional = true)
  @Named("sessions.neverPersist")
  private final boolean sessionsNeverPersist = false;

  @Inject(optional = true)
  @Named("tomcat.useBio")
  private final boolean useBio = false;

  @Inject(optional = true)
  @Named("tomcat.internalProxies")
  private String internalProxies;

  @Inject private DataSourceService dataSourceService;

  @Inject private ZookeeperService zookeeperService;

  @Inject private HibernateMigrationService hibernateMigrationService;

  private Tomcat tomcat;

  @Override
  public void startup() {
    if (ajpPort == -1 && httpPort == -1 && httpsPort == -1) {
      throw new RuntimeException(
          "You must specify either 'http.port, https.port' or 'ajp.port' in your"
              + " mandatory-config.properties");
    }
    try {
      setupDirectories();
    } catch (IOException e) {
      LOGGER.error("Failed to setup tomcat directories: ", e);
      throw new RuntimeException(e);
    }

    tomcat = new Tomcat();
    tomcat.setBaseDir(tomcatBasePath);

    Context context = new StandardContext();
    context.addLifecycleListener(new AprLifecycleListener());
    context.setName("/");
    context.setPath("");
    context.setDocBase(tomcatContextPath);
    context.setUseHttpOnly(false);
    if (useXForwardedFor) {
      LOGGER.debug("Enabling the Tomcat RemoteIpValve.");
      RemoteIpValve protoValve = new RemoteIpValve();
      protoValve.setProtocolHeader("X-Forwarded-Proto");

      if (!Check.isEmpty(internalProxies)) {
        LOGGER.debug(
            "Setting the Tomcat RemoteIpValve InternalProxies to: [" + internalProxies + "]");
        protoValve.setInternalProxies(internalProxies);
      } else {
        LOGGER.debug("Not enabling the Tomcat RemoteIpValve InternalProxies - config is empty");
      }
      context.getPipeline().addValve(protoValve);
    }

    ContextConfig ctxCfg = new ContextConfig();
    context.addLifecycleListener(ctxCfg);
    ctxCfg.setDefaultWebXml(tomcat.noDefaultWebXmlPath());

    tomcat.getHost().addChild(context);

    Tomcat.initWebappDefaults(context);
    context.removeChild(context.findChild("jsp"));

    final String servletName = "PrometheusMetricsServlet";
    Tomcat.addServlet(context, servletName, new MetricsServlet());
    context.addServletMappingDecoded("/metrics", servletName);

    context.addFilterDef(dispatchFilter("RequestDispatchFilter", "REQUEST"));
    context.addFilterDef(dispatchFilter("ForwardDispatchFilter", "FORWARD"));
    context.addFilterDef(dispatchFilter("ErrorDispatchFilter", "ERROR"));

    context.addFilterMap(dispatchMap("RequestDispatchFilter", "REQUEST"));
    context.addFilterMap(dispatchMap("ForwardDispatchFilter", "FORWARD"));
    context.addFilterMap(dispatchMap("ErrorDispatchFilter", "ERROR"));

    StandardWrapper defaultServlet = (StandardWrapper) context.findChild("default");
    defaultServlet.setMultipartConfigElement(
        new MultipartConfigElement("", 1000000000L, 1000000000L, 1000000000));

    ErrorPage notFound = new ErrorPage();
    notFound.setErrorCode(404);
    notFound.setLocation("/error.do?method=notFound");

    ErrorPage accessDenied = new ErrorPage();
    accessDenied.setErrorCode(403);
    accessDenied.setLocation("/error.do?method=accessDenied");

    ErrorPage throwableError = new ErrorPage();
    throwableError.setExceptionType("java.lang.Throwable");
    throwableError.setLocation("/error.do?method=throwable");

    context.addErrorPage(notFound);
    context.addErrorPage(accessDenied);
    context.addErrorPage(throwableError);

    if (ajpPort != -1) {
      Connector connector = new Connector(useBio ? BIO_AJP : "AJP/1.3");
      connector.setPort(ajpPort);
      if (!ajpSecret.equals("ignore")) {
        connector.setProperty("secret", ajpSecret);
      }
      connector.setProperty("secretRequired", String.valueOf(ajpSecretRequired));
      connector.setProperty("tomcatAuthentication", String.valueOf(false));
      connector.setProperty("packetSize", "65536");
      connector.setProperty("address", ajpAddress);
      setConnector(connector);
    }

    if (httpPort != -1) {
      Connector connector = new Connector(useBio ? BIO_HTTP : "HTTP/1.1");
      connector.setPort(httpPort);
      setConnector(connector);
    }

    if (httpsPort != -1) {
      Connector connector = new Connector(useBio ? BIO_HTTP : "HTTP/1.1");
      connector.setPort(httpsPort);
      connector.setSecure(true);
      setConnector(connector);
    }

    // Clustering
    setupClusteringConfig(context);

    context.setJarScanner(
        new JarScanner() {
          @Override
          public void scan(
              JarScanType scanType, ServletContext context, JarScannerCallback callback) {
            // No-op
          }

          @Override
          public JarScanFilter getJarScanFilter() {
            return null;
          }

          @Override
          public void setJarScanFilter(JarScanFilter jarScanFilter) {}
        });

    try {
      tomcat.getServer().setParentClassLoader(getClass().getClassLoader());
      tomcat.start();
    } catch (LifecycleException e) {
      LOGGER.error("Failed to start tomcat: ", e);
      throw new RuntimeException(e);
    }
  }

  private void setConnector(Connector connector) {
    if (maxThreads != -2) {
      connector.setAttribute("maxThreads", maxThreads);
    }
    connector.setURIEncoding("UTF-8");
    connector.setUseBodyEncodingForURI(true);
    tomcat.getService().addConnector(connector);
    tomcat.setConnector(connector);
  }

  private void setupClusteringConfig(Context context) {
    if (zookeeperService.isCluster()) {
      if (!sessionsNeverPersist) {
        // Setup persistent manager
        context.setManager(getSessionManager(context));
      }

      if (!Check.isEmpty(jvmRouteId)) {
        tomcat.getEngine().setJvmRoute(jvmRouteId);
      }
    }
  }

  private Manager getSessionManager(Context context) {
    // Must be set for the persistent manager to work correctly.
    System.setProperty("org.apache.catalina.session.StandardSession.ACTIVITY_CHECK", "true");

    PersistentManager manager = new PersistentManager();
    StoreBase store;

    // Check if Migration has run and setup JDBC persistence
    if (!hibernateMigrationService.hasRunSystemMigration(
        "com.tle.tomcat.migration.CreateTomcatSessionEntity")) {
      // Use filestore until JDBC setup complete
      LOGGER.info("Setting up temporary file session store");
      FileStore fileStore = new FileStore();
      fileStore.setDirectory(System.getProperty("java.io.tmpdir"));
      store = fileStore;
    } else {
      LOGGER.info("Setting up JDBC session store");
      JDBCStore jdbcStore = new JDBCStore();
      jdbcStore.setConnectionURL(dataSourceService.getSystemUrl());
      jdbcStore.setConnectionName(dataSourceService.getSystemUsername());
      jdbcStore.setConnectionPassword(dataSourceService.getSystemPassword());
      jdbcStore.setDriverName(dataSourceService.getDriverClass());
      jdbcStore.setSessionTable("tomcat_sessions");
      store = jdbcStore;
    }

    manager.setContext(context);
    manager.setProcessExpiresFrequency(3); // Every 30 seconds
    manager.setStore(store);
    // Persist immediately but leave in memory
    manager.setMaxIdleBackup(0);
    // Don't remove from memory for 30 minutes
    manager.setMaxIdleSwap((int) TimeUnit.MINUTES.toSeconds(30));

    return manager;
  }

  @Override
  public void stop() throws Exception {
    if (tomcat != null) {
      tomcat.stop();
    }
  }

  @Override
  public void restart() throws Exception {
    if (tomcat != null) {
      LOGGER.info("Stopping and destroying tomcat...");
      tomcat.stop();
      tomcat.destroy();
      LOGGER.info("Starting tomcat...");
      startup();
    }
  }

  private FilterDef dispatchFilter(String name, String type) {
    FilterDef filterDef = new FilterDef();
    filterDef.setFilterName(name);
    filterDef.setFilterClass(RequestDispatchFilter.class.getName());
    filterDef.addInitParameter("dispatcher", type);
    return filterDef;
  }

  private FilterMap dispatchMap(String name, String dispatcher) {
    FilterMap filterMap = new FilterMap();
    filterMap.setFilterName(name);
    filterMap.setDispatcher(dispatcher);
    filterMap.addURLPattern("/*");
    return filterMap;
  }

  @Override
  public void restartTomcat() {
    try {
      restart();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /** Setup the directory for Tomcat and the oEQ webapp context. */
  private void setupDirectories() throws IOException {
    if (tomcatContextPath != null && tomcatBasePath != null) {
      return;
    }
    // Setup a temporary directory to sandbox this instance of Tomcat and the context for
    // the oEQ web app.
    final File tempDir = Files.createTempDirectory("oeq-tomcat-basedir-").toFile();
    // Ideally here we'd be able to use File.deleteOnExit() or a Runtime.addShutdownHook()
    // to clean-up this file on termination. However we'd have to synchronise with the
    // tomcat daemon etc. That could be a future optimisation if needed.
    final File contextDir = new File(tempDir, "context/");
    if (!contextDir.mkdir()) {
      throw new RuntimeException(
          "Failed to setup context directory: " + contextDir.getAbsolutePath());
    }

    tomcatBasePath = tempDir.getAbsolutePath();
    tomcatContextPath = contextDir.getAbsolutePath();

    LOGGER.info("Using base directory: " + tomcatBasePath);
    LOGGER.info("Using context directory: " + tomcatContextPath);
  }
}
