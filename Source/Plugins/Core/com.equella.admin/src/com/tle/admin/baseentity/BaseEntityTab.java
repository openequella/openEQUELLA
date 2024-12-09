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

package com.tle.admin.baseentity;

import com.dytech.gui.JStatusBar;
import com.tle.admin.Driver;
import com.tle.admin.PluginServiceImpl;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.JFakePanel;
import com.tle.admin.i18n.Lookup;
import com.tle.beans.entity.BaseEntity;
import com.tle.common.adminconsole.RemoteAdminService;
import com.tle.common.applet.client.ClientService;
import java.awt.Component;
import javax.swing.JDialog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseEntityTab<T extends BaseEntity> extends JFakePanel {
  public static final Log LOGGER = LogFactory.getLog(BaseEntityTab.class);

  protected static String getString(String key) {
    return Lookup.lookup.text(key);
  }

  protected static String getKey(String key) {
    return Lookup.lookup.key(key);
  }

  protected EditorState<T> state;

  protected ClientService clientService;
  protected PluginServiceImpl pluginService;
  protected RemoteAdminService adminService;

  protected JStatusBar statusBar;
  protected JDialog parent;
  protected DynamicTabService dynamicTabService;

  public void setDriver(Driver driver) {
    this.clientService = driver.getClientService();
    this.pluginService = driver.getPluginService();

    this.adminService = clientService.getService(RemoteAdminService.class);
  }

  public void setParent(JDialog parent) {
    this.parent = parent;
  }

  public void setStatusBar(JStatusBar statusBar) {
    this.statusBar = statusBar;
  }

  public void setState(EditorState<T> state) {
    this.state = state;
  }

  public void setDynamicTabService(DynamicTabService dynamicTabService) {
    this.dynamicTabService = dynamicTabService;
  }

  public abstract void save();

  public void afterSave() {
    // Nothing by default
  }

  public abstract void load();

  public abstract void init(Component parent);

  /**
   * @return the title that should appear on the tab.
   */
  public abstract String getTitle();

  /**
   * The tab must validate it's data.
   *
   * @throws EditorException if something is invalid.
   */
  public abstract void validation() throws EditorException;
}
