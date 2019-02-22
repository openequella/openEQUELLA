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

package com.tle.core.equella.runner;

import java.net.URL;
import org.java.plugin.registry.ManifestInfo;

public class TLEPluginLocation implements org.java.plugin.PluginManager.PluginLocation {
  private final String jar;
  private final ManifestInfo manifestInfo;
  private final URL context;
  private final URL manifest;
  private int version = -1;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof TLEPluginLocation)) {
      return false;
    }

    return context.toString().equals(((TLEPluginLocation) obj).context.toString());
  }

  @Override
  public int hashCode() {
    return context.toString().hashCode();
  }

  public TLEPluginLocation(ManifestInfo info, String jar, URL context, URL manifest) {
    this.manifestInfo = info;
    this.jar = jar;
    this.context = context;
    this.manifest = manifest;
  }

  public int getVersion() {
    return version;
  }

  public String getJar() {
    return jar;
  }

  @Override
  public URL getContextLocation() {
    return context;
  }

  @Override
  public URL getManifestLocation() {
    return manifest;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public ManifestInfo getManifestInfo() {
    return manifestInfo;
  }
}
