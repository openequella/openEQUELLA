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

package com.dytech.edge.installer;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.ForeignCommand;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Progress;
import com.tle.upgrademanager.ManagerConfig;
import com.tle.upgrademanager.helpers.AjaxMessage;
import com.tle.upgrademanager.helpers.AjaxState;
import com.tle.upgrademanager.helpers.Deployer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class JarResolver extends ForeignCommand {

  public JarResolver(PropBagEx commandBag, PropBagEx resultBag) {
    super(commandBag, resultBag);
  }

  @Override
  public void execute() throws InstallerException {
    String installDir = resultBag.getNode("install.path");
    final String serviceManagerPath = installDir + "/manager"; // $NON-NLS-1$

    File updatesDir = new File(serviceManagerPath, "updates"); // $NON-NLS-1$
    if (!updatesDir.exists() || !updatesDir.isDirectory()) {
      throw new InstallerException("Upgrade Zip folder (" + updatesDir.toString() + ")not found");
    }

    File[] zipFiles =
        updatesDir.listFiles(
            new FilenameFilter() {
              @Override
              public boolean accept(File dir, String name) {
                return name.startsWith("tle-upgrade-")
                    && name.endsWith(".zip"); // $NON-NLS-1$ //$NON-NLS-2$
              }
            });

    if (zipFiles.length > 0) {
      File zipFile = zipFiles[0];

      Properties config = new Properties();
      try (InputStream fis =
          new FileInputStream(new File(serviceManagerPath, "config.properties"))) // $NON-NLS-1$
      {
        config.load(fis);
      } catch (Exception ex) {
        throw new InstallerException("Could not load properties file", ex);
      }

      Deployer deployer;
      try {
        deployer =
            new Deployer(
                null,
                new FakeAjaxState(getProgress()),
                new ManagerConfig(new File(installDir), null));
        deployer.unzipUpgrade(zipFile);
      } catch (IOException ex) {
        throw new InstallerException("Error while trying to deploy JAR file", ex);
      }
      try {
        deployer.getUpgraderLauncher().install();
      } catch (Exception ex) {
        throw new InstallerException("Error while attempting to migrate the installation", ex);
      }
    } else {
      throw new InstallerException("Could not find any EQUELLA updates");
    }
  }

  @Override
  public String toString() {
    return "Preparing web application.";
  }

  /** A dummy ajax state class that'll print messages out via System.out.println(); */
  public static class FakeAjaxState implements AjaxState {
    private final Progress progrezz;

    public FakeAjaxState(Progress progrezz) {
      this.progrezz = progrezz;
    }

    @Override
    public void addBasic(String id, String message) {
      System.out.println("Basic: " + message);
      progrezz.addMessage(message);
    }

    @Override
    public void addConsole(String id, String message) {
      System.out.println("Console: " + message);
      progrezz.addMessage(message);
    }

    @Override
    public void addError(String id, String message) {
      System.out.println("ERROR: " + message);
      progrezz.addMessage(message);
    }

    @Override
    public void addHeading(String id, String message) {
      System.out.println("Heading: " + message);
      progrezz.addMessage(message);
    }

    @Override
    public void finish(String id, String nothing, String nothing2) {
      System.out.println("Finished!");
      progrezz.addMessage("Finished!");
    }

    @Override
    public List<AjaxMessage> getListOfAllMessages(String id) {
      // dummy method!
      return null;
    }

    @Override
    public void start(String id, String message) {
      System.out.println("Start!");
      progrezz.addMessage("Start!");
    }

    @Override
    public void start(String id) {
      System.out.println("Start!");
      progrezz.addMessage("Start!");
    }

    @Override
    public void addErrorRaw(String id, String message) {
      addError(id, message);
    }
  }
}
