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

package com.tle.admin.controls.standard.universal;

import com.tle.admin.controls.universal.UniversalControlSettingPanel;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.common.wizard.controls.universal.handlers.LinkSettings;
import javax.swing.JCheckBox;

@SuppressWarnings("nls")
public class LinksSettingsPanel extends UniversalControlSettingPanel {
  private final JCheckBox linkDuplicationCheck =
      new JCheckBox(getString("links.settings.duplicate.check"));

  public LinksSettingsPanel() {
    super();
    add(linkDuplicationCheck, "span 2");
  }

  @Override
  protected String getTitleKey() {
    return getKey("links.settings.title");
  }

  @Override
  public void load(UniversalSettings state) {
    LinkSettings settings = new LinkSettings(state);
    linkDuplicationCheck.setSelected(settings.isDuplicationCheck());
  }

  @Override
  public void removeSavedState(UniversalSettings state) {
    // Nothing to do
  }

  @Override
  public void save(UniversalSettings state) {
    LinkSettings settings = new LinkSettings(state);
    settings.setDuplicationCheck(linkDuplicationCheck.isSelected());
  }
}
