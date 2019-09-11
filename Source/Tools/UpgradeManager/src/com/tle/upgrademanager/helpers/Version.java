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

package com.tle.upgrademanager.helpers;

import com.tle.upgrademanager.ManagerConfig;
import com.tle.upgrademanager.Utils;
import com.tle.upgrademanager.handlers.PagesHandler.WebVersion;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;

@SuppressWarnings("nls")
public class Version {

  private final ManagerConfig config;

  public Version(ManagerConfig config) {
    this.config = config;
  }

  public SortedSet<WebVersion> getVersions() {
    final SortedSet<WebVersion> versions = new TreeSet<WebVersion>(Utils.VERSION_COMPARATOR);

    final File upgradeFolder = config.getUpdatesDir();
    if (upgradeFolder.isDirectory()) {
      for (String file : upgradeFolder.list()) {
        if (file != null && Utils.VERSION_EXTRACT.matcher(file).matches()) {
          versions.add(getWebVersionFromFile(file));
        }
      }
    } else {
      throw new RuntimeException("No upgrades folder found");
    }

    return versions;
  }

  public WebVersion getDeployedVersion() {
    WebVersion version = new WebVersion();
    File versionFile = new File(getVersionPropertiesDirectory(), "version.properties");
    try (FileInputStream in = new FileInputStream(versionFile)) {
      Properties p = new Properties();
      p.load(in);

      String displayName = p.getProperty("version.display");
      String semanticVersion = getSemanticVersion(displayName);

      version.setDisplayName(displayName);
      version.setSemanticVersion(semanticVersion);
      version.setFilename(
          MessageFormat.format("tle-upgrade-{0} ({1}).zip", semanticVersion, displayName));
    } catch (IOException ex) {
      version.setDisplayName(Utils.UNKNOWN_VERSION);
    }
    return version;
  }

  public File getUpgradeFile(String filename) {
    File vdir = config.getUpdatesDir();
    if (vdir.exists()) {
      File file = new File(vdir, filename);
      if (file.exists()) {
        return file;
      }
    }

    return null;
  }

  private File getVersionPropertiesDirectory() {
    return new File(config.getInstallDir(), Utils.EQUELLASERVER_DIR);
  }

  private static String getSemanticVersion(String displayName) {
    // semanticVersion is part of displayName, e.g. 2019.1.1-Stable.OSE
    Matcher matcher = Utils.VERSION_DISPLAY.matcher(displayName);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Provided display name (" + displayName + ") is invalid.");
    }

    return displayName.substring(0, displayName.indexOf("-"));
  }

  private WebVersion getWebVersionFromFile(String filename) {
    Matcher matcher = Utils.VERSION_EXTRACT.matcher(filename);

    if (!matcher.matches()) {
      throw new IllegalArgumentException("Unrecognised filename format for: " + filename);
    }

    final String displayName = matcher.group(2);
    return new WebVersion(displayName, getSemanticVersion(displayName), filename);
  }
}
