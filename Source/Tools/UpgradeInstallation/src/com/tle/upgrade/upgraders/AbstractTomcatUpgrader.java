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

package com.tle.upgrade.upgraders;

import com.dytech.common.io.FileUtils;
import com.dytech.common.io.ZipUtils;
import com.google.common.io.ByteStreams;
import com.tle.common.util.EquellaConfig;
import com.tle.upgrade.FileCopier;
import com.tle.upgrade.UpgradeMain;
import com.tle.upgrade.UpgradeResult;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.zip.ZipInputStream;

@SuppressWarnings("nls")
@Deprecated
public abstract class AbstractTomcatUpgrader extends AbstractUpgrader {
  private static final String UPGRADE_URL_KEY = "tomcat.upgrade.url"; // $NON-NLS-1$

  protected abstract String getTomcatZip();

  protected abstract String getDefaultUpgradeUrl();

  @Override
  public boolean canBeRemoved() {
    return false;
  }

  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    result.setCanRetry(true);

    final String tomcatZipFilename = getTomcatZip();

    // download zip
    final URL downloadLocation = new URL(getDownloadUrl(tleInstallDir), tomcatZipFilename);

    final URLConnection conn = downloadLocation.openConnection();
    final File temp = new File(tleInstallDir, "temp");
    temp.mkdir();
    final File tomcatZip = new File(temp, tomcatZipFilename);
    result.info("Downloading " + downloadLocation.toString());
    try (InputStream in = new BufferedInputStream(conn.getInputStream());
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tomcatZip))) {
      ByteStreams.copy(in, out);
    } catch (IOException io) {
      result.info(
          "Error downloading "
              + tomcatZipFilename
              + ", trying to find it locally at "
              + tomcatZip.getAbsolutePath());
      if (!tomcatZip.exists()) {
        throw new Exception(
            "Could not download "
                + tomcatZipFilename
                + ".  If your network connection requires a proxy server, please enter the proxy"
                + " server details on the Configuration tab and try again.",
            io);
      }
    }

    // backup tomcat folder
    result.info("Backing up current Tomcat");
    final String commit = UpgradeMain.getCommit();
    File tomcatFolder = new File(tleInstallDir, "tomcat");
    final File tomcatBackup = new File(tleInstallDir, "tomcat" + commit);

    try {
      new FileCopier(tomcatFolder, tomcatBackup, true).rename();
    } catch (Exception e) {
      throw new Exception(
          "Failed to backup existing Tomcat folder to "
              + tomcatBackup.getAbsolutePath()
              + ". Possibly open in file system?",
          e);
    }

    // extract tomcat zip
    result.info("Unzipping " + tomcatZipFilename);
    tomcatFolder = new File(tleInstallDir, "tomcat");
    tomcatFolder.mkdir();
    ZipUtils.extract(
        new ZipInputStream(new BufferedInputStream(new FileInputStream(tomcatZip))), tomcatFolder);

    afterTomcatExtraction(result, tleInstallDir, tomcatFolder, tomcatBackup);

    FileUtils.delete(temp);
  }

  protected void afterTomcatExtraction(
      UpgradeResult result, File tleInstallDir, File tomcatFolder, File tomcatBackupFolder)
      throws Exception {
    File webApps = new File(tomcatFolder, "webapps");
    File webAppOld = new File(tomcatBackupFolder, "webapps");
    FileUtils.delete(webApps);
    try {
      new FileCopier(webAppOld, webApps, true).rename();
    } catch (Exception e) {
      throw new Exception(
          "Failed to copy existing webapps folder. Possibly open in file system?", e);
    }
  }

  protected URL getDownloadUrl(File tleInstallDir) throws Exception {
    final EquellaConfig config = new EquellaConfig(tleInstallDir);
    final Properties managerProps = new Properties();
    try (FileReader reader =
        new FileReader(new File(config.getManagerDir(), "config.properties"))) {
      managerProps.load(reader);
    }
    return new URL(managerProps.getProperty(UPGRADE_URL_KEY, getDefaultUpgradeUrl()));
  }
}
