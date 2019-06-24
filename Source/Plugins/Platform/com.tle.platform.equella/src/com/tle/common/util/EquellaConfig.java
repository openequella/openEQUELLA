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

package com.tle.common.util;

import com.dytech.edge.common.Constants;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class EquellaConfig {
  private static final String MANDATORY_CONFIG_PROPERTIES =
      "mandatory-config.properties"; //$NON-NLS-1$

  protected final File installDir;
  protected final File learningEdgeConfigDir;
  protected final File javaBin;
  protected final File managerDir;

  public EquellaConfig(File installDir) {
    this.installDir = installDir;
    learningEdgeConfigDir = new File(installDir, Constants.LEARNINGEDGE_CONFIG_FOLDER);
    managerDir = new File(installDir, Constants.MANAGER_FOLDER);

    try (InputStream propFile =
        new FileInputStream(new File(learningEdgeConfigDir, MANDATORY_CONFIG_PROPERTIES))) {
      Properties props = new Properties();
      props.load(propFile);
      String javaHome = props.getProperty("java.home"); // $NON-NLS-1$
		System.out.println("---------Temporary testing java home in 2018.2:" + javaHome);
      javaBin = ExecUtils.findExe(new File(javaHome, "bin/java")); // $NON-NLS-1$
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public File getInstallDir() {
    return installDir;
  }

  public File getConfigDir() {
    return learningEdgeConfigDir;
  }

  public File getJavaBin() {
    return javaBin;
  }

  public File getManagerDir() {
    return managerDir;
  }
}
