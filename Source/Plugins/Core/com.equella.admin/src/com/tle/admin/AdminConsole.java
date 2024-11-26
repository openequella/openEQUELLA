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

package com.tle.admin;

import com.tle.admin.boot.LoadingDialog;
import com.tle.admin.helper.ClientConfigurationHelper;
import com.tle.client.harness.HarnessInterface;
import com.tle.client.impl.ClientLocaleImplementation;
import com.tle.client.impl.ClientServiceImpl;
import com.tle.client.impl.CurrentTimeZoneClientSide;
import com.tle.common.applet.SessionHolder;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.core.remoting.RemoteLanguageService;
import com.tle.i18n.BundleCache;
import io.github.openequella.graphql.ClientConfiguration;
import java.awt.Window;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main class that launches the Administration Console.
 *
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class AdminConsole implements HarnessInterface {
  private static final Logger LOGGER = LoggerFactory.getLogger(AdminConsole.class);

  private static final String DOCUMENTBUILDERFACTORY = "javax.xml.parsers.DocumentBuilderFactory";
  private static final String DEFAULT_XML_PARSER5 =
      "com.sun.org.apache.xerces.internal.jaxp." + "DocumentBuilderFactoryImpl";

  private static final String ERROR_TITLE = "Error";
  private static final String ERROR_MESSAGE =
      "There has been an error loading the Administration Console"
          + "\nPlease consult your System Administrator";

  private Window managementDialog;

  private ClientService clientService;
  private Locale locale;
  private URL endpointURL;
  private PluginServiceImpl pluginService;

  public AdminConsole() {
    final String javaVersion = System.getProperty("java.version");
    final String osName = System.getProperty("os.name");

    LOGGER.info("Java version is '{}'", javaVersion);
    LOGGER.info("OS name is '{}'", osName);
  }

  protected void initLanguageBundles() throws IOException {
    // TODO: change the rtl stuff (if we ever support rtl in admin console)
    LOGGER.info("Locale is {}", locale);
    CurrentLocale.initialise(
        new ClientLocaleImplementation(endpointURL, getBundleGroups(), locale, false));
    CurrentTimeZone.initialise(new CurrentTimeZoneClientSide(TimeZone.getDefault()));
  }

  public String[] getBundleGroups() {
    return new String[] {"admin-console", "recipient-selector"};
  }

  /**
   * Starts the Administration Console, after the user has already authenticated and as a result,
   * the session has been created. A cookie for the session can be found in the system cookies.
   */
  @Override
  public void start() {
    try {
      // Detect the Mac hack param
      String tempDir = System.getProperty("jnlp.java.io.tmpdir");
      if (Strings.isNotEmpty(tempDir)) {
        System.setProperty("java.io.tmpdir", tempDir);
      }

      // Initialise server session
      SessionHolder holder = new SessionHolder(endpointURL);
      // Initialise services
      clientService = new ClientServiceImpl(holder);

      // Initialise bundle cache
      BundleCache.initialise(clientService.getService(RemoteLanguageService.class));

      // Make sure we are using the default XML Parser.
      System.setProperty(DOCUMENTBUILDERFACTORY, DEFAULT_XML_PARSER5);

      setupLookAndFeel();
      final LoadingDialog loading = new LoadingDialog("openEQUELLA: Administration Console");
      loading.setVisible(true);
      loading.toFront();

      // Initialise language bundle now
      initLanguageBundles();

      // Set the client configuration for the GraphQL library
      ClientConfiguration clientConfiguration = ClientConfigurationHelper.create(endpointURL);
      ClientConfigurationHelper.loadSystemCookies(clientConfiguration);

      // Create the driver interface.
      Driver.create(clientService, pluginService, clientConfiguration);

      // Create the management dialog.
      managementDialog = new ManagementDialog();

      loading.setVisible(false);
      loading.dispose();

      // Switch the visible windows
      managementDialog.setVisible(true);
      managementDialog.toFront();
    } catch (Exception ex) {
      LOGGER.error("Error starting the Administration Console", ex);
      JOptionPane.showMessageDialog(
          managementDialog, ERROR_MESSAGE, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
      clientService.stop();
    }
  }

  /** Ensures the system's native look and feel is set by default. */
  private void setupLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex) {
      LOGGER.error("Look And Feel could not be set.", ex);
    }
  }

  @Override
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  @Override
  public void setEndpointURL(URL endpointURL) {
    this.endpointURL = endpointURL;
  }

  @Override
  public void setPluginService(PluginServiceImpl pluginService) {
    this.pluginService = pluginService;
  }
}
