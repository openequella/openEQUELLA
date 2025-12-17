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

import com.tle.upgrade.UpgradeResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

@SuppressWarnings("nls")
public class ModifyBirtConfig extends AbstractUpgrader {
  public static final String ID = "ModifyBirtConfig";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean canBeRemoved() {
    return false;
  }

  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    File configFolder = new File(tleInstallDir, CONFIG_FOLDER);
    Properties props = new Properties();
    try (FileInputStream mandatoryStream =
        new FileInputStream(new File(configFolder, "mandatory-config.properties"))) {
      props.load(new InputStreamReader(mandatoryStream, "UTF-8"));
      File reportingConfig =
          new File(props.getProperty("reporting.workspace.location"), "configuration");
      if (!reportingConfig.exists()) {
        throw new Exception("Reporting configuration folder could not be found");
      }
      copyResource("data/config.ini", new File(reportingConfig, "config.ini"));
    }
  }
}
