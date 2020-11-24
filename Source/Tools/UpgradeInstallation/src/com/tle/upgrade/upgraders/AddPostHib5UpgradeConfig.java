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

import com.dytech.edge.common.Constants;
import com.google.common.collect.Lists;
import com.tle.common.util.EquellaConfig;
import com.tle.upgrade.LineFileModifier;
import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.UpgradeResult;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Due to the Hibernate 5 upgrade, oEQ needs a couple hibernate configs added, and to suppress some
 * noisy deprecation warnings in the logs.
 */
public class AddPostHib5UpgradeConfig extends AbstractUpgrader {

  @Override
  public String getId() {
    return "AddPostHib5UpgradeConfig";
  }

  @Override
  public boolean isBackwardsCompatible() {
    return false;
  }

  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    EquellaConfig config = new EquellaConfig(tleInstallDir);

    result.addLogMessage("Updating hibernate properties");
    updateHibernateProperties(result, config.getConfigDir());

    result.addLogMessage("Updating log4j properties");
    updateLog4jProperties(result, config.getConfigDir());
  }

  private void updateHibernateProperties(final UpgradeResult result, File configDir) {
    try {
      LineFileModifier lineMod =
          new LineFileModifier(new File(configDir, PropertyFileModifier.HIBERNATE_CONFIG), result) {
            @Override
            protected String processLine(String line) {
              // Do nothing
              return line;
            }

            @Override
            protected List<String> addLines() {
              String line1 =
                  "# Needed to align with previous installs and certain database vendors";
              String line2 = "hibernate.id.new_generator_mappings=false";
              String line3 = "# Due to oEQ's inheritance strategy of type JOINED";
              String line4 = "hibernate.query.omit_join_of_superclass_tables=false";
              return Lists.newArrayList(
                  Constants.BLANK, line1, line2, Constants.BLANK, line3, line4);
            }
          };

      lineMod.update();
    } catch (IOException e) {
      throw new RuntimeException("Failed to update config file", e);
    }
  }

  private void updateLog4jProperties(final UpgradeResult result, File configDir) {
    try {
      LineFileModifier lineMod =
          new LineFileModifier(new File(configDir, PropertyFileModifier.LOG4J_CONFIG), result) {
            @Override
            protected String processLine(String line) {
              // Do nothing
              return line;
            }

            @Override
            protected List<String> addLines() {
              // This assumes the config file does not contain appender filters.  This will
              // override `filter.1` and `filter.2` if defined previously in the file.
              String line1 = "# Criteria deprecation warning suppression";
              String line2 =
                  "log4j.appender.FILE.filter.1=org.apache.log4j.varia.StringMatchFilter";
              String line3 = "log4j.appender.FILE.filter.1.StringToMatch=HHH90000022";
              String line4 = "log4j.appender.FILE.filter.1.AcceptOnMatch=false";
              String line5 = "# Generator warning suppression";
              String line6 =
                  "log4j.appender.FILE.filter.2=org.apache.log4j.varia.StringMatchFilter";
              String line7 = "log4j.appender.FILE.filter.2.StringToMatch=HHH90000014";
              String line8 = "log4j.appender.FILE.filter.2.AcceptOnMatch=false";
              return Lists.newArrayList(
                  Constants.BLANK,
                  line1,
                  line2,
                  line3,
                  line4,
                  Constants.BLANK,
                  line5,
                  line6,
                  line7,
                  line8);
            }
          };

      lineMod.update();
    } catch (IOException e) {
      throw new RuntimeException("Failed to update config file", e);
    }
  }
}
