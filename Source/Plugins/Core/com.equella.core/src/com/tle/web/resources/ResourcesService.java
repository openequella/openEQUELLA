/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.resources;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import org.java.plugin.Plugin;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.institution.InstitutionService;
import com.tle.core.plugins.PluginService;
import com.tle.core.services.ApplicationVersion;

import java.util.Map;

@SuppressWarnings("nls")
@NonNullByDefault
public class ResourcesService {
  private static String baseUrl = "p/r/" + ApplicationVersion.get().getMmr() + '/';

  private static Map<String, String> renamed =
      ImmutableMap.<String, String>builder()
          .put("com.tle.web.sections.equella", "com.equella.core")
          .put("com.tle.web.sections.standard", "com.equella.core")
          .put("com.tle.web.connectors", "com.equella.core")
          .put("com.tle.web.contribute", "com.equella.core")
          .put("com.tle.web.search", "com.equella.core")
          .put("com.tle.web.itemlist", "com.equella.core")
          .put("com.tle.web.htmleditor.tinymce", "com.equella.core")
          .put("com.tle.web.wizard.controls.universal", "com.equella.core")
          .build();

  public static String getRealPluginId(String pluginId) {
    String actualPluginId = renamed.get(pluginId);
    if (actualPluginId == null) {
      return pluginId;
    }
    return actualPluginId;
  }

  public static String gkey(String pluginId, String key) {
    return getRealPluginId(pluginId) + "." + key;
  }

  public static String getUrl(String pluginId, String path) {
    StringBuilder b = new StringBuilder(75);
    b.append(baseUrl);
    b.append(getRealPluginId(pluginId));
    b.append('/');
    b.append(path);
    return b.toString();
  }

  public static PluginResourceHelper getResourceHelper(Object pluginObj) {
    return new PluginResourceHelper(pluginObj);
  }
}
