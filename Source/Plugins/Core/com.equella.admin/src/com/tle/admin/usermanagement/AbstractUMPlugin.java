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

package com.tle.admin.usermanagement;

import com.tle.admin.gui.EditorException;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.admin.plugin.PluginDialog;
import com.tle.admin.service.AdminUserDirectoryConfigService;
import com.tle.beans.ump.UserManagementSettings;
import java.awt.Frame;
import java.io.Serial;

public class AbstractUMPlugin extends PluginDialog<UserManagementSettings, UMPConfig> {
  @Serial private static final long serialVersionUID = 1L;
  private UserManagementSettings settings;
  private final AdminUserDirectoryConfigService userDirectoryConfigService;

  public AbstractUMPlugin(
      Frame frame,
      String title,
      UMPConfig setting,
      GeneralPlugin<UserManagementSettings> plugin,
      AdminUserDirectoryConfigService userDirectoryConfigService) {
    super(frame, title, setting, plugin);
    this.userDirectoryConfigService = userDirectoryConfigService;
  }

  @Override
  protected void _load(GeneralPlugin<UserManagementSettings> gplugin) {
    settings = userDirectoryConfigService.loadSettingsOrThrow(setting);
    gplugin.load(settings);
  }

  @Override
  protected void _save(GeneralPlugin<UserManagementSettings> gplugin) throws EditorException {
    if (gplugin.save(settings)) {
      userDirectoryConfigService.setPluginConfig(setting.getSettingsClass(), settings);
    }
  }
}
