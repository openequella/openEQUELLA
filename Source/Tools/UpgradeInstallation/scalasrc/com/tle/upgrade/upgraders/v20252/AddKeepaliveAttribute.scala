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

package com.tle.upgrade.upgraders.v20252

import com.tle.upgrade.{PropertyFileModifier, UpgradeResult}
import com.tle.upgrade.upgraders.AbstractUpgrader
import org.apache.commons.configuration.PropertiesConfiguration

import java.io.File

/** Recommended configuration by Hikari.
  *
  * Reference： https://github.com/brettwooldridge/HikariCP/wiki/Setting-Driver-or-OS-TCP-Keepalive
  */
class AddKeepaliveAttribute extends AbstractUpgrader {
  private final val HIBERNATE_DIALECT = "hibernate.dialect";

  private final val POSTGRESQL_KEEP_ALIVE_ATTRIBUTE = "hibernate.connection.tcpKeepAlive"
  private final val ORACLE_KEEP_ALIVE_ATTRIBUTE     = "hibernate.connection.oracle.net.keepAlive"

  private final val POSTGRES_DIALECT  = "com.tle.hibernate.dialect.ExtendedPostgresDialect"
  private final val ORACLE_9_DIALECT  = "com.tle.hibernate.dialect.ExtendedOracle9iDialect"
  private final val ORACLE_10_DIALECT = "com.tle.hibernate.dialect.ExtendedOracle10iDialect"
  private final val MSSQL_DIALECT     = "com.tle.hibernate.dialect.SQLServerDialect"

  override def getId: String                  = "AddKeepaliveAttribute"
  override def isBackwardsCompatible: Boolean = true

  @throws[Exception]
  override def upgrade(result: UpgradeResult, tleInstallDir: File): Unit = {
    result.addLogMessage("Updating hibernate.properties to enable JDBC keepalive")
    updateHibernateProperties(result, tleInstallDir)
  }

  private def updateHibernateProperties(result: UpgradeResult, configDir: File): Unit = {
    val configFolder  = new File(configDir, AbstractUpgrader.CONFIG_FOLDER)
    val hibernateFile = new File(configFolder, PropertyFileModifier.HIBERNATE_CONFIG)

    val modifier = new PropertyFileModifier(hibernateFile) {
      override protected def modifyProperties(props: PropertiesConfiguration): Boolean = {
        props.getString(HIBERNATE_DIALECT) match {
          case POSTGRES_DIALECT =>
            props.addProperty(POSTGRESQL_KEEP_ALIVE_ATTRIBUTE, true)
            true
          case ORACLE_9_DIALECT | ORACLE_10_DIALECT =>
            props.addProperty(ORACLE_KEEP_ALIVE_ATTRIBUTE, true)
            true
          case MSSQL_DIALECT =>
            // Skip mssql since it needs manual configuration.
            true
          case _ =>
            result.addLogMessage("Unsupported dialect found in hibernate.properties")
            false
        }
      }
    }

    modifier.updateProperties()
  }
}
