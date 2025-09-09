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

import com.tle.common.util.ExecUtils;
import com.tle.upgrade.LineFileModifier;
import com.tle.upgrade.UpgradeMain;
import com.tle.upgrade.UpgradeResult;
import java.io.File;
import java.net.URL;

public class UpdateManagerJar extends AbstractUpgrader {
  private static final String MANAGERJAR = "/manager/manager.jar"; // $NON-NLS-1$

  @SuppressWarnings("nls")
  @Override
  public String getId() {
    return "UpgradeManager-r" + UpgradeMain.getCommit();
  }

  @Override
  public boolean isRunOnInstall() {
    return true;
  }

  @Override
  public boolean canBeRemoved() {
    return true;
  }

  @SuppressWarnings("nls")
  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    URL managerUrl = getClass().getResource(MANAGERJAR);
    if (managerUrl == null) {
      return;
    }
    String commit = UpgradeMain.getCommit();
    final File newJarFile = new File(tleInstallDir, "manager/manager-r" + commit + ".jar");

    File managerConfig = new File(tleInstallDir, "manager/manager.conf");
    if (managerConfig.exists()) {
      LineFileModifier modifier =
          new LineFileModifier(managerConfig, result) {
            @Override
            protected String processLine(String line) {
              return line.replaceAll("manager.*\\.jar", newJarFile.getName());
            }
          };
      modifier.update();
    }

    String config =
        ExecUtils.determinePlatform().startsWith(ExecUtils.PLATFORM_WIN)
            ? "manager/manager-config.bat"
            : "manager/manager-config.sh";

    File file = new File(tleInstallDir, config);
    if (file.exists()) {
      new LineFileModifier(file, result) {
        @Override
        protected String processLine(String line) {
          return line.replaceAll("manager.*\\.jar", newJarFile.getName());
        }
      }.update();
    }
    copyResource(MANAGERJAR, newJarFile);
  }
}
