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

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Version;
import com.dytech.gui.ExceptionDialog;
import com.tle.admin.boot.Bootstrap;
import com.tle.admin.controls.ControlRepositoryImpl;
import com.tle.admin.controls.repository.ControlRepository;
import com.tle.admin.service.AdminConsolePluginService;
import com.tle.admin.service.AdminKeepAliveService;
import com.tle.admin.service.AdminLoginService;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.JpfException;

/**
 * Provides the communications ability for the admin console.
 *
 * @author Nicholas Read
 */
public final class Driver {
  private static final Log LOGGER = LogFactory.getLog(Driver.class);
  private static final String COLON = ":";

  private static Driver driver = null;

  private final Version version;
  private final String institutionName;
  private final String loggedInUserID;

  private final ClientService clientService;

  private ControlRepository controlRepository;
  private final PluginServiceImpl pluginService;

  /**
   * @return The singleton Driver instance or null.
   */
  public static Driver instance() {
    return driver;
  }

  public static Driver create(ClientService clientService, PluginServiceImpl pluginService)
      throws Exception {
    if (driver != null) {
      throw new IllegalStateException("Invalid attempt to try and create second Driver instance");
    }

    driver = new Driver(clientService, pluginService);
    return driver;
  }

  private Driver(ClientService clientService, PluginServiceImpl pluginService) throws Exception {
    this.clientService = clientService;

    // Setup some initial state.
    loggedInUserID =
        clientService
            .getService(AdminLoginService.class)
            .getLoggedInUserId()
            .orElseThrow(() -> new IllegalStateException("No logged in user"));
    institutionName = clientService.getParameter(Bootstrap.SERVER_NAME_PARAMETER);

    version = Version.load();

    if (pluginService == null) {
      LOGGER.info("Product Version is " + version.getFull());
      pluginService =
          new PluginServiceImpl(
              clientService.getServerURL(),
              version.getCommit(),
              clientService.getService(AdminConsolePluginService.class));
    }
    this.pluginService = pluginService;
    try {
      pluginService.registerPlugins();
    } catch (JpfException e) {
      throw new RuntimeException(e);
    }

    clientService.getService(AdminKeepAliveService.class).start();
  }

  // This is deprecated as far back as the history we have is (i.e. before 2013), but yet it is
  // used extensively and I don't see an alternative.
  // So perhaps this annotation should be removed.
  @Deprecated
  public ClientService getClientService() {
    return clientService;
  }

  public static void displayError(
      Component parent, String titleKey, String messageKey, Throwable throwable) {
    displayErrorRaw(parent, CurrentLocale.get(titleKey), CurrentLocale.get(messageKey), throwable);
  }

  @Deprecated
  public static void displayError(Component parent, String messageGroup, Throwable throwable) {
    PropBagEx xml = Messages.getInstance().getError(messageGroup);
    if (xml == null) {
      xml = Messages.getInstance().getError("unknown");
    }
    String title = xml.getNode("title");
    String message = xml.getNode("message");
    String thrownMsg = throwable.getMessage();
    if (!Check.isEmpty(thrownMsg)) {
      // Most likely thrown message is prefixed with a ':' separated chain
      // of exception types, which is just clutter for the purpose of a
      // quick display of brief message.
      if (thrownMsg.contains(COLON)) {
        thrownMsg = thrownMsg.substring(thrownMsg.lastIndexOf(COLON) + 1);
      }
      message += "\n\n" + thrownMsg;
    }
    message = message.replaceAll("\\\\n", "\n");
    displayErrorRaw(parent, title, message, throwable);
  }

  /**
   * Displays an error dialog reporting {@code throwable}, with the given title and message shown
   * verbatim (unlike {@link #displayError}, which resolves them as locale keys).
   *
   * @param parent the component to anchor the dialog to; may be {@code null} if no suitable
   *     component is available (or it hasn't been added to a window yet), in which case the dialog
   *     falls back to whichever window currently has focus, so it isn't shown ownerless behind the
   *     rest of the application
   * @param title the dialog title, displayed verbatim
   * @param message the error message, displayed verbatim
   * @param throwable the exception being reported, shown in the dialog's "Details" tab
   */
  public static void displayErrorRaw(
      @Nullable Component parent, String title, String message, Throwable throwable) {
    String version = instance().getVersion().getFull();
    Window owner = resolveOwnerWindow(parent);

    ExceptionDialog ed = new ExceptionDialog(owner, title, message, version, throwable);
    ed.setTitle(CurrentLocale.get("com.tle.admin.driver.title"));
    ed.setVisible(true);
  }

  // No parent to anchor to (or it isn't showing yet) - fall back to whichever window
  // currently has focus, so the dialog doesn't open ownerless behind the app.
  private static Window resolveOwnerWindow(@Nullable Component parent) {
    return Optional.ofNullable(parent)
        .map(SwingUtilities::getWindowAncestor)
        .orElseGet(() -> KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow());
  }

  public static void displayInformation(Component parent, String message) {
    JOptionPane.showMessageDialog(
        parent,
        message,
        CurrentLocale.get("com.tle.admin.driver.info"),
        JOptionPane.INFORMATION_MESSAGE); // $NON-NLS-1$
  }

  // ///////// SOAPED //////////////////////////////////////////////

  public Version getVersion() {
    return version;
  }

  public String getInstitutionName() {
    return institutionName;
  }

  public String getLoggedInUserUUID() {
    return loggedInUserID;
  }

  public PluginServiceImpl getPluginService() {
    return pluginService;
  }

  public ControlRepository getControlRepository() {
    if (controlRepository == null) {
      controlRepository = new ControlRepositoryImpl(pluginService, clientService);
    }
    return controlRepository;
  }
}
