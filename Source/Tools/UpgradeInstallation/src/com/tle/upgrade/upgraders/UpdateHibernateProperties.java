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

public class UpdateHibernateProperties extends AbstractUpgrader {
  private static final String HIBERNATE_DIALECT = "hibernate.dialect"; // $NON-NLS-1$

  @Override
  public String getId() {
    return "UpdateHibernateProperties"; //$NON-NLS-1$
  }

  @SuppressWarnings("nls")
  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    PropertyFileModifier modifier =
        new PropertyFileModifier(
            new File(
                new File(tleInstallDir, CONFIG_FOLDER), PropertyFileModifier.HIBERNATE_CONFIG)) {
          @Override
          protected boolean modifyProperties(PropertiesConfiguration props) {
            String dialect = props.getString(HIBERNATE_DIALECT);
            String newdialect = null;
            if (dialect.equals("org.hibernate.dialect.PostgreSQLDialect")) {
              newdialect = "com.tle.hibernate.dialect.ExtendedPostgresDialect";
            } else if (dialect.equals("org.hibernate.dialect.Oracle10gDialect")) {
              newdialect = "com.tle.hibernate.dialect.ExtendedOracle10gDialect";
            } else if (dialect.equals("org.hibernate.dialect.Oracle9iDialect")
                || dialect.equals("org.hibernate.dialect.Oracle9Dialect")) {
              newdialect = "com.tle.hibernate.dialect.ExtendedOracle9iDialect";
            }
            if (newdialect != null) {
              props.setProperty(HIBERNATE_DIALECT, newdialect);
              return true;
            }
            return false;
          }
        };
    modifier.updateProperties();
  }

  @Override
  public boolean canBeRemoved() {
    return true;
  }
}
