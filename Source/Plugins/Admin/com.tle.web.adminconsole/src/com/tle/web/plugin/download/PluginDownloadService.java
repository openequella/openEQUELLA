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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that manages plugin metadata and JAR file location for the Admin Console.
 *
 * <p>This service implements {@link RemotePluginDownloadService} and is remotely invoked by the
 * Admin Console to discover available plugins and their download locations. It works in conjunction
 * with {@link DownloadServlet} to enable plugin distribution in production environments.
 *
 * <h2>Architecture Overview</h2>
 *
 * When the Admin Console launches, it:
 *
 * <ol>
 *   <li>Calls {@link #getAllPluginDetails(String)} to get the list of available plugins
 *   <li>Receives plugin metadata with rewritten JAR URLs pointing to {@link DownloadServlet} (e.g.,
 *       {@code https://institution.url/ds/plugin-name.jar})
 *   <li>Downloads the JAR files through the {@link DownloadServlet}'s {@code /ds/*} endpoint
 *   <li>Loads the plugins locally from the downloaded JARs
 * </ol>
 *
 * <h2>Core Responsibilities</h2>
 *
 * <ol>
 *   <li><strong>Plugin Discovery:</strong> Identifies all plugins of a given type (e.g.,
 *       "admin-console") and their dependencies
 *   <li><strong>URL Rewriting:</strong> Converts local {@code jar:file:} URLs to HTTP-accessible
 *       {@code jar:https:} URLs that point to {@link DownloadServlet}
 *   <li><strong>JAR File Resolution:</strong> Maps JAR filenames to their physical filesystem
 *       locations
 *   <li><strong>Plugin Filtering:</strong> Excludes certain system plugins (e.g., Guice, Spring,
 *       Hibernate) from distribution
 * </ol>
 *
 * <h2>URL Rewriting Mechanism</h2>
 *
 * The {@link #getAllPluginDetails(String)} method performs conditional URL rewriting:
 *
 * <ul>
 *   <li><strong>Production (JAR protocol):</strong> When plugins are loaded from {@code
 *       jar:file:/path/to/plugin.jar!/}, URLs are rewritten to {@code
 *       jar:https://institution.url/ds/plugin.jar!/} to enable HTTP download via {@link
 *       DownloadServlet}
 *       <ul>
 *         <li>Original: {@code jar:file:/opt/equella/plugins/plugin-name.jar!/}
 *         <li>Rewritten: {@code jar:https://institution.url/ds/plugin-name.jar!/}
 *       </ul>
 *   <li><strong>Development (File protocol):</strong> When plugins are loaded from {@code
 *       file:/path/to/plugin/}, URLs are left unchanged, allowing direct filesystem access
 *       <ul>
 *         <li>No rewriting occurs
 *         <li>{@link DownloadServlet} is never invoked
 *         <li>Admin Console reads plugins directly from the local filesystem
 *       </ul>
 * </ul>
 *
 * <h2>Admin Console Integration</h2>
 *
 * The Admin Console uses this service as follows:
 *
 * <ol>
 *   <li>Obtains a remote proxy to this service via {@code
 *       clientService.getService(RemotePluginDownloadService.class)}
 *   <li>Calls {@link #getAllPluginDetails(String)} with plugin type "admin-console"
 *   <li>Receives a list of {@link com.tle.core.remoting.RemotePluginDownloadService.PluginDetails}
 *       containing:
 *       <ul>
 *         <li>JAR download URLs (rewritten to use {@link DownloadServlet} in production)
 *         <li>Plugin manifest XML content
 *       </ul>
 *   <li>Downloads JARs via the provided URLs
 *   <li>Registers and activates plugins locally
 * </ol>
 *
 * <h2>Servlet Mapping Configuration</h2>
 *
 * The service automatically discovers the {@link DownloadServlet} URL pattern during {@link
 * #setupMapping()} initialization by reading the {@code downloadServletMapping} extension from
 * {@code plugin-jpf.xml}. This pattern (typically {@code /ds/*}) is used to construct the rewritten
 * download URLs.
 *
 * @see DownloadServlet
 * @see com.tle.core.remoting.RemotePluginDownloadService
 * @see com.tle.admin.PluginServiceImpl
 */
@Bind
@Singleton
public class PluginDownloadService implements RemotePluginDownloadService {
  private final Logger LOGGER = LoggerFactory.getLogger(PluginDownloadService.class);
  private String jarPath;

  @Inject private PluginService pluginService;
  @Inject private InstitutionService institutionService;

  @SuppressWarnings("nls")
  private final Set<String> DISALLOWED =
      ImmutableSet.of(
          "com.tle.core.guice",
          "com.tle.core.spring",
          "org.hibernate",
          "org.springframework.httpinvoker");

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
      String descId = desc.getId();
      TLEPluginLocation location = manifestToLocation.get(descId);
      if (!pluginService.isPluginDisabled(location)) {
        StringWriter manWriter = new StringWriter();
        try {
          Resources.asCharSource(location.getManifestLocation(), Charsets.UTF_8).copyTo(manWriter);

          URL jarUrl = location.getContextLocation();
          String originalProtocol = jarUrl.getProtocol();
          if (originalProtocol.equals("jar")) {
            LOGGER.debug(
                "PluginDownloadService: Rewriting JAR URL for plugin '{}'. Original URL protocol:"
                    + " {}",
                descId,
                originalProtocol);

            jarUrl =
                new URL(
                    "jar",
                    "",
                    new URL(
                            institutionService.getInstitutionUrl(),
                            jarPath + location.getJar() + "!/")
                        .toString());
            LOGGER.debug("PluginDownloadService: New download URL for '{}': {}", descId, jarUrl);
          } else {
            LOGGER.debug(
                "PluginDownloadService: Keeping original URL for plugin '{}' (protocol: {}): {}",
                descId,
                originalProtocol,
                jarUrl);
          }
          details.add(new PluginDetails(jarUrl, manWriter.toString()));
        } catch (IOException e) {
          throw new RuntimeException(e);
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
