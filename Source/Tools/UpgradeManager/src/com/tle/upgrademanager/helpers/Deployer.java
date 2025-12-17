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

import com.dytech.common.io.FileUtils;
import com.dytech.common.io.ZipUtils;
import com.google.common.base.Strings;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.tle.upgrademanager.ManagerConfig;
import com.tle.upgrademanager.deploy.UpgraderLauncher;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Deployer {
  public static final String UPGRADER_JAR = "installation-upgrader.jar";

  private static final String CONVERSION_SERVICE_JAR = "conversion-service.jar";
  private static final String EQUELLA_JAR_NAME = "equella-server.jar";
  private static final String PLUGINS_SOURCE_DIR = "plugins";
  private static final String PLUGIN_DEST_DIR = "Standard";
  private static final String VERSION_PROPS = "version.properties";

  private final String ajaxId;
  private final AjaxState ajax;
  private final ManagerConfig config;
  private static final Log LOG = LogFactory.getLog(Deployer.class);

  public Deployer(String ajaxId, AjaxState ajax, ManagerConfig config) {
    this.ajaxId = ajaxId;
    this.ajax = ajax;
    this.config = config;
  }

  public void deploy(String filename) {
    String redirect = "/pages/";
    String finishMessage = "Click here to continue";
    ajax.start(ajaxId);
    try {
      ajax.addHeading(ajaxId, "Stopping service");

      ServiceWrapper serviceWrapper = new ServiceWrapper(config);
      try {
        serviceWrapper.stop();
      } catch (Exception ex) {
        throw new DeployException("Error stopping service " + ex.getMessage(), ex);
      }

      ajax.addHeading(ajaxId, "Starting upgrade");
      File file = new Version(config).getUpgradeFile(filename);
      try {
        unzipUpgrade(file);
      } catch (Exception ex) {
        throw new DeployException(
            "Error while attempting to deploy JAR file: " + ex.getMessage(), ex);
      }

      try {
        ajax.addHeading(ajaxId, "Migrating installation files");
        getUpgraderLauncher().upgrade();
        ajax.addHeading(ajaxId, "Migrating complete");
      } catch (Exception ex) {
        throw new DeployException(
            "Error while attempting to migrate the installation " + ex.getMessage(), ex);
      }

      redirect = "/pages/restartmanager";
      ajax.addHeading(ajaxId, "Upgrade complete");
    } catch (DeployException de) {
      ajax.addError(ajaxId, de.getMessage());
      ajax.addErrorRaw(
          ajaxId,
          "Unfortunately an error has"
              + " occurred while attempting to upgrade. Please see above for more"
              + " details. Click the button to go back and retry.");
      finishMessage = "Click here to try again";
      LOG.error("Error deploying", de);
    } finally {
      ajax.finish(ajaxId, finishMessage, redirect);
    }
  }

  public UpgraderLauncher getUpgraderLauncher() {
    return new UpgraderLauncher(config, ajaxId, ajax);
  }

  public void unzipUpgrade(File upgradeZip) throws IOException {
    // Create a temporary directory
    File tempDir = null;

    ZipInputStream zip = null;
    try {
      tempDir = File.createTempFile("tle-", "temp");
      tempDir.delete();
      tempDir.mkdirs();

      zip = new ZipInputStream(new FileInputStream(new java.io.File(upgradeZip, "")));
      ZipUtils.extract(zip, tempDir);

      ajax.addBasic(ajaxId, "Upgrading JAR...");
      deployJar(new File(tempDir, EQUELLA_JAR_NAME));

      ajax.addBasic(ajaxId, "Upgrading Plugins...");
      deployPlugins(new File(tempDir, PLUGINS_SOURCE_DIR));

      ajax.addBasic(ajaxId, "Upgrading Installation Upgrader...");
      deployDatabaseUpgrader(new File(tempDir, UPGRADER_JAR));

      ajax.addBasic(ajaxId, "Upgrading Conversion Service...");
      deployConversionService(new File(tempDir, CONVERSION_SERVICE_JAR));

      // Move version props
      moveToInstall(new File(tempDir, VERSION_PROPS), "server/" + VERSION_PROPS);

      ajax.addHeading(ajaxId, "Upgrade Complete");
    } catch (IOException e) {
      String message = e.getMessage();
      if (Strings.isNullOrEmpty(message)) {
        message = e.toString();
      }
      ajax.addError(ajaxId, message);
      LOG.error("Error unzipping upgrade", e);
      throw new RuntimeException(e);
    } finally {
      Closeables.close(zip, true);
      ajax.addBasic(ajaxId, "Removing temporary directory...");
      FileUtils.delete(tempDir);
    }
  }

  private void deployConversionService(File conversionJar) throws IOException {
    moveToInstall(conversionJar, "conversion/" + CONVERSION_SERVICE_JAR);
  }

  private void deployDatabaseUpgrader(File dbUpgraderJar) throws IOException {
    moveToInstall(dbUpgraderJar, "manager/" + UPGRADER_JAR);
  }

  private void deployPlugins(File pluginsDir) throws IOException {
    moveDirToInstall(pluginsDir, "plugins/" + PLUGIN_DEST_DIR);
  }

  private void moveDirToInstall(File pluginsDir, String destination) throws IOException {
    Path installPath = config.getInstallDir().toPath();
    Path fullDest = installPath.resolve(destination);
    FileUtils.delete(fullDest.toFile());
    Files.createParentDirs(fullDest.toFile());
    org.apache.commons.io.FileUtils.moveDirectoryToDirectory(pluginsDir, fullDest.toFile(), true);
  }

  private void deployJar(File serverJar) throws IOException {
    moveToInstall(serverJar, "server/" + EQUELLA_JAR_NAME);
  }

  private void moveToInstall(File source, String destination) throws IOException {
    Path installPath = config.getInstallDir().toPath();
    Path fullDest = installPath.resolve(destination);
    ajax.addBasic(ajaxId, "Copying: " + source.getPath() + " to: " + fullDest);
    FileUtils.delete(fullDest.toFile());
    Files.createParentDirs(fullDest.toFile());
    Files.move(source, fullDest.toFile());
  }
}
