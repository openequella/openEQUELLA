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

import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeResult;
import java.io.File;
import org.apache.commons.configuration.PropertiesConfiguration;

@SuppressWarnings("nls")
public class RemoveClusterGroupName extends AbstractUpgrader {
  public static final String ID = "RemoveClusterGroupName";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isBackwardsCompatible() {
    return false;
  }

  @Override
  public void upgrade(final UpgradeResult result, File tleInstallDir) throws Exception {
    new PropertyFileModifier(
        new File(new File(tleInstallDir, CONFIG_FOLDER), PropertyFileModifier.MANDATORY_CONFIG)) {
      @Override
      protected boolean modifyProperties(PropertiesConfiguration props) {
        if (props.containsKey("cluster.group.name")) {
          result.addLogMessage("Removing cluster.group.name property");
          props.clearProperty("cluster.group.name");
          return true;
        }
        return false;
      }
    }.updateProperties();
  }
}
