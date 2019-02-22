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

import com.tle.beans.Institution;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.PluginService;
import com.tle.core.services.impl.UrlServiceImpl;
import org.java.plugin.Plugin;

public class PluginResourceHelper {
  public static final String KEY_RESOURCEHELPER = "$RSRCHELPER$"; // $NON-NLS-1$

  private final PluginService pluginService;
  private final String pluginId;

  public PluginResourceHelper(Object relativeTo) {
    pluginService = AbstractPluginService.get();
    if (relativeTo instanceof String) {
      pluginId = (String) relativeTo;
    } else if (relativeTo instanceof Plugin) {
      pluginId = ((Plugin) relativeTo).getDescriptor().getId();
    } else {
      pluginId = pluginService.getPluginForObject(relativeTo).getDescriptor().getId();
    }
  }

  public String instUrl(String path) {
    Institution institution = CurrentInstitution.get();
    String baseUrl;
    if (institution != null) {
      baseUrl = institution.getUrl();
    } else {
      baseUrl = UrlServiceImpl.instance().getAdminUrl().toString();
    }
    return baseUrl + path;
  }

  public String key(String key) {
    return pluginId + '.' + key;
  }

  public String gkey(String pluginId, String key, Object... values) {
    return CurrentLocale.get(ResourcesService.gkey(pluginId, key), values);
  }

  public String getString(String localKey, Object... values) {
    return CurrentLocale.get(pluginId + '.' + localKey, values);
  }

  @SuppressWarnings("unchecked")
  public <T> T getBean(String clazzName) {
    return (T) pluginService.getBean(pluginId, clazzName);
  }

  public String url(String resource) {
    return ResourcesService.getUrl(pluginId, resource);
  }

  public String plugUrl(String pluginId, String resource) {
    return ResourcesService.getUrl(pluginId, resource);
  }

  public String pluginId() {
    return pluginId;
  }
}
