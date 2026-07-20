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

package com.tle.admin.usermanagement.tool;

import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.PluginServiceImpl;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.admin.plugin.PluginDialog;
import com.tle.admin.service.AdminUserDirectoryConfigService;
import com.tle.admin.usermanagement.AbstractUMPlugin;
import com.tle.admin.usermanagement.UMPConfig;
import com.tle.admin.usermanagement.UMWConfig;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ExtensionParamComparator;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

public class UserManagementTool extends AdminToolSelect {
  protected static final Log LOGGER = LogFactory.getLog(UserManagementTool.class);
  private Collection<UMWConfig> wrappers;
  private String toolName;
  private AdminUserDirectoryConfigService userDirectoryConfigService;

  public UserManagementTool() {
    super();
  }

  @Override
  public void setup(Set<String> grantedPrivileges, String toolName) {
    this.toolName = toolName;
    wrappers = new ArrayList<UMWConfig>();

    ClientService clientService = driver.getClientService();
    PluginServiceImpl pluginService = driver.getPluginService();
    userDirectoryConfigService = clientService.getService(AdminUserDirectoryConfigService.class);

    PluginTracker tracker =
        new PluginTracker(
            pluginService,
            "com.tle.admin.usermanagement.tool",
            "configUI",
            null,
            new ExtensionParamComparator("displayorder"));
    Collection<Extension> extensions = tracker.getExtensions();
    for (Extension extension : extensions) {
      String settingsClassName = extension.getParameter("settingsClass").valueAsString();

      Parameter param = extension.getParameter("class");
      String className = param != null ? param.valueAsString() : null;
      param = extension.getParameter("width");
      int width = param != null ? param.valueAsNumber().intValue() : 0;
      param = extension.getParameter("height");
      int height = param != null ? param.valueAsNumber().intValue() : 0;
      String name = CurrentLocale.get(extension.getParameter("name").valueAsString());

      UMWConfig umw = new UMWConfig(className, settingsClassName, name, width, height, extension);
      wrappers.add(umw);

      pluginService.ensureActivated(extension.getDeclaringPluginDescriptor());
      userDirectoryConfigService
          .loadSettings(umw)
          .ifPresentOrElse(
              settings -> umw.setEnabled(settings.isEnabled()), () -> umw.setVisible(false));
    }

    super.setup(grantedPrivileges, toolName);
  }

  @Override
  protected void fillLists() {
    for (UMWConfig umw : wrappers) {
      if (umw.isVisible()) {
        addWrapperElement(umw);
      }
    }
    wrapperList.updateUI();
  }

  @Override
  public void onConfigure(Object selected, final boolean select) {
    final UMPConfig plugin = (UMPConfig) selected;

    final GlassSwingWorker<Object> worker =
        new GlassSwingWorker<Object>() {
          @SuppressWarnings("unchecked")
          @Override
          public Object construct() throws Exception {
            if (plugin == null) {
              return null;
            }
            if (Check.isEmpty(plugin.getPluginClass())) {
              return CurrentLocale.get("com.tle.admin.gui.usermanagementtool.noconfiguration");
            }

            PluginServiceImpl pluginService = driver.getPluginService();
            GeneralPlugin<UserManagementSettings> umplugin =
                (GeneralPlugin<UserManagementSettings>)
                    pluginService.getBean(
                        plugin.getExtension().getDeclaringPluginDescriptor(),
                        plugin.getPluginClass());
            return createDialog(parentFrame, toolName, plugin, umplugin);
          }

          @Override
          public void finished() {
            Object o = get();
            if (o != null) {
              if (o instanceof PluginDialog) {
                PluginDialog window = (PluginDialog) o;
                window.setModal(true);
                window.setVisible(true);
              } else {
                Driver.displayInformation(parentFrame, o.toString());
              }
            } else {
              Driver.displayInformation(
                  parentFrame, CurrentLocale.get("com.tle.admin.gui.usermanagementtool.noplugin"));
            }
          }

          @Override
          public void exception() {
            Exception ex = getException();
            LOGGER.error(
                "Problem creating UMP '" + (plugin == null ? plugin : plugin.getClass()) + '\'',
                ex);
            Driver.displayError(parentFrame, "user.management/loading", ex);
          }
        };

    worker.setComponent(parentFrame);
    worker.start();
  }

  public PluginDialog<UserManagementSettings, UMPConfig> createDialog(
      Frame frame, String title, UMPConfig setting, GeneralPlugin<UserManagementSettings> gplugin) {
    AbstractUMPlugin plugin =
        new AbstractUMPlugin(frame, title, setting, gplugin, userDirectoryConfigService);
    plugin.setup();
    return plugin;
  }
}
