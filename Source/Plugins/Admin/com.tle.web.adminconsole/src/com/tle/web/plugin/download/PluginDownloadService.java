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

package com.tle.web.plugin.download;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.tle.common.filters.EqFilter;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.plugins.AbstractPluginService.TLEPluginLocation;
import com.tle.core.plugins.PluginService;
import com.tle.core.remoting.RemotePluginDownloadService;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginAttribute;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.util.IoUtil;

@Bind
@Singleton
public class PluginDownloadService implements RemotePluginDownloadService {
  private String jarPath;

  @Inject private PluginService pluginService;
  @Inject private InstitutionService institutionService;

  @SuppressWarnings("nls")
  private Set<String> DISALLOWED =
      ImmutableSet.of(
          "com.tle.core.guice",
          "com.tle.core.spring",
          "org.hibernate",
          "org.springframework.httpinvoker",
          "com.tle.webstart.admin");

  /** Don't use directly - call getJarMap(). */
  private Map<String, TLEPluginLocation> jarMap;

  @Override
  @SuppressWarnings("nls")
  public List<PluginDetails> getAllPluginDetails(String pluginType) {
    final Set<PluginDescriptor> plugins =
        pluginService.getAllPluginsAndDependencies(new FilterByType(pluginType), DISALLOWED, false);
    final Map<String, TLEPluginLocation> manifestToLocation = pluginService.getPluginIdToLocation();

    List<PluginDetails> details = new ArrayList<PluginDetails>();
    for (PluginDescriptor desc : plugins) {
      TLEPluginLocation location = manifestToLocation.get(desc.getId());
      if (!pluginService.isPluginDisabled(location)) {
        StringWriter manWriter = new StringWriter();
        try {
          Resources.asCharSource(location.getManifestLocation(), Charsets.UTF_8).copyTo(manWriter);

          URL jarUrl = location.getContextLocation();
          if (jarUrl.getProtocol().equals("jar")) {
            jarUrl =
                new URL(
                    "jar",
                    "",
                    new URL(
                            institutionService.getInstitutionUrl(),
                            jarPath + location.getJar() + "!/")
                        .toString());
          }
          details.add(new PluginDetails(jarUrl, manWriter.toString()));
        } catch (IOException e) {
          throw Throwables.propagate(e);
        }
      }
    }
    return details;
  }

  @SuppressWarnings("nls")
  @PostConstruct
  void setupMapping() {
    Extension extension =
        pluginService
            .getPluginForObject(getClass())
            .getDescriptor()
            .getExtension("downloadServletMapping");
    String jarFilePath = extension.getParameter("url-pattern").valueAsString(); // $NON-NLS-1$
    this.jarPath = jarFilePath.substring(1, jarFilePath.length() - 1);
  }

  private synchronized Map<String, TLEPluginLocation> getJarMap() {
    if (jarMap == null) {
      jarMap = new HashMap<String, TLEPluginLocation>();
      for (TLEPluginLocation loc : pluginService.getPluginIdToLocation().values()) {
        jarMap.put(loc.getJar(), loc);
      }
    }
    return jarMap;
  }

  public File getFileForJar(String jarFile) {
    TLEPluginLocation location = getJarMap().get(jarFile);
    if (location != null) {
      return IoUtil.url2file(location.getContextLocation());
    }
    return null;
  }

  private static class FilterByType extends EqFilter<PluginDescriptor> {
    public FilterByType(String pluginType) {
      super(pluginType);
    }

    @Override
    protected Object getForComparison(PluginDescriptor d) {
      PluginAttribute attr = d.getAttribute("type"); // $NON-NLS-1$
      return attr == null ? null : attr.getValue();
    }
  }
}
