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

package com.tle.admin.collection.summarydisplay;

import com.dytech.edge.admin.script.SafeScripting;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.PluginService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

@SuppressWarnings("nls")
public class NewDisplaySummarySectionDialog extends AbstractChoiceDialog<SummarySectionsConfig> {
  private final Map<String, String> defaultNames = new HashMap<String, String>();

  private static String KEY_PFX =
      AbstractPluginService.getMyPluginId(NewDisplaySummarySectionDialog.class) + ".";

  protected static String getString(String key) {
    return CurrentLocale.get(getKey(key));
  }

  protected static String getKey(String key) {
    return KEY_PFX + key;
  }

  public NewDisplaySummarySectionDialog(final PluginService pluginService) {
    super(
        getString("summarysections.adddialog.instructions"),
        getString("summarysections.adddialog.title"));

    for (Extension ext :
        pluginService.getConnectedExtensions("com.tle.admin.collection.tool", "summaryDisplay")) {
      final String id = ext.getParameter("id").valueAsString();
      Parameter param = ext.getParameter("advancedScripting");
      if (SafeScripting.isSafeScripting() && param != null && param.valueAsBoolean()) {
        continue;
      }
      addChoice(id, CurrentLocale.get(ext.getParameter("nameKey").valueAsString()));
      defaultNames.put(id, ext.getParameter("defaultNameKey").valueAsString());
    }
  }

  @Override
  protected void addClicked(String key) {
    selection = new SummarySectionsConfig(key);
    String title = CurrentLocale.get(defaultNames.get(key));
    selection.setTitle(title);
    LanguageBundle bundleTitle = new LanguageBundle();
    LangUtils.setString(bundleTitle, CurrentLocale.getLocale(), title);
    selection.setBundleTitle(bundleTitle);
    selection.setUuid(UUID.randomUUID().toString());
    dialog.dispose();
  }
}
