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

package com.tle.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.java.plugin.JpfException;
import org.java.plugin.ObjectFactory;
import org.java.plugin.PathResolver;
import org.java.plugin.registry.ManifestInfo;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.util.ExtendedProperties;

import com.google.common.io.CharStreams;
import com.tle.common.URLUtils;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.remoting.RemotePluginDownloadService;
import com.tle.core.remoting.RemotePluginDownloadService.PluginDetails;

public class PluginServiceImpl extends AbstractPluginService {
  private final RemotePluginDownloadService downloadService;

  @SuppressWarnings("nls")
  public PluginServiceImpl(
      URL serverUrl, String shortVersion, RemotePluginDownloadService downloadService) {
    this.downloadService = downloadService;

    ExtendedProperties properties = new ExtendedProperties();
    properties.put(PathResolver.class.getName(), TleShadingPathResolver.class.getName());
    try {
      // eg, /tmp/.jpf-shadow/http%3A%2F%2Fdemo.equella.com/5.0.12345/
      File f = new File(System.getProperty("java.io.tmpdir"), ".jpf-shadow");
      f = new File(f, URLUtils.basicUrlEncode(serverUrl.toString()));
      f = new File(f, shortVersion);

      properties.put("com.tle.admin.TleShadingPathResolver.shadowFolder", f.getCanonicalPath());
    } catch (Exception ex) {
      throw new RuntimeException("Error", ex);
    }
    ObjectFactory factory = ObjectFactory.newInstance(properties);
    pluginManager = factory.createManager();
  }

  @Override
  public Object getBean(final PluginDescriptor plugin, final String clazzName) {
    return instantiatePluginClass(plugin, clazzName);
  }

  public Class<?> getBeanClass(final PluginDescriptor plugin, final String className) {
    ensureActivated(plugin);
    try {
      return pluginManager.getPluginClassLoader(plugin).loadClass(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("nls")
  public void registerPlugins() throws IOException, JpfException {
    List<TLEPluginLocation> locations = new ArrayList<TLEPluginLocation>();
    List<PluginDetails> allPluginDetails = downloadService.getAllPluginDetails("admin-console");
    for (PluginDetails details : allPluginDetails) {
      File file = File.createTempFile("manifest", ".xml");

      try (OutputStreamWriter writer =
          new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
        CharStreams.copy(new StringReader(details.getManifestXml()), writer);
      } finally {
        file.deleteOnExit();
      }
      URL manifestUrl = file.toURI().toURL();
      ManifestInfo info = pluginManager.getRegistry().readManifestInfo(manifestUrl);
      TLEPluginLocation location =
          new TLEPluginLocation(info, null, details.getBaseUrl(), manifestUrl);
      locations.add(location);
    }
    pluginManager.publishPlugins(locations.toArray(new TLEPluginLocation[locations.size()]));
  }

  @Override
  public boolean isPluginDisabled(TLEPluginLocation location) {
    throw new UnsupportedOperationException();
  }
}
