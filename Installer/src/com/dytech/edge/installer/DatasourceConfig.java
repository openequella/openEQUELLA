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

import com.dytech.common.text.Substitution;
import com.dytech.devlib.PropBagEx;
import com.dytech.installer.ForeignCommand;
import com.dytech.installer.InstallerException;
import com.dytech.installer.XpathResolver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class DatasourceConfig extends ForeignCommand {

  public static final String ROOT_NODE = "datasource";

  /** Marker used in configuration template files to mark variables to be substituted. */
  public static final String SUBSTITUTION_MARKER = "${ }";

  private final String localPath;
  private final String database;
  private final String installPath;

  public DatasourceConfig(PropBagEx commandBag, PropBagEx resultBag) throws InstallerException {
    super(commandBag, resultBag);

    database = resultBag.getNode(DatasourceNodes.TYPE.path());
    localPath = resultBag.getNode("installer/local");
    installPath = resultBag.getNode("install.path");
  }

  /**
   * When this is called, we actually do all the work that needs doing. In this case, we're
   * populating the database.
   */
  @Override
  public void execute() throws InstallerException {
    propogateTaskStarted(2);

    String source =
        localPath + "/learningedge-config/hibernate.properties." + database; // $NON-NLS-1$
    String destination = installPath + "/learningedge-config/hibernate.properties"; // $NON-NLS-1$
    copy(source, destination);

    propogateSubtaskCompleted();

    propogateTaskCompleted();
  }

  private void copy(String source, String destination) {
    try (BufferedReader in = new BufferedReader(new FileReader(source));
        BufferedWriter out = new BufferedWriter(new FileWriter(destination))) {
      Substitution sub = new Substitution(new XpathResolver(resultBag), SUBSTITUTION_MARKER);
      sub.resolve(in, out);
      propogateSubtaskCompleted();
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new InstallerException("Problem copying Hibernate property files.");
    }
  }

  /** Return a nice string for the progress box to show. */
  @Override
  public String toString() {
    return "Updating Hibernate property files...";
  }

  public static PropBagEx extractDatasourceConfig(PropBagEx config) {
    final PropBagEx cfg = new PropBagEx().newSubtree("cfg");
    cfg.append("", config.getSubtree(ROOT_NODE));
    return cfg;
  }

  /**
   * Given a PropBag with at least the datasource/host specified, will attempt to use that to
   * attempt to set the port. This is done by either splitting the host on the port delimiter (':')
   * or using the default port determined by datasource/dbtype.
   *
   * @param config a PropBag with expected values ready to process
   * @return the original config mutated in place
   */
  public static PropBagEx updateHostAndPort(PropBagEx config) {
    String dbhost = config.getNode(DatasourceNodes.HOST.path());
    if (dbhost.indexOf(':') >= 0) {
      String[] parts = dbhost.split(":");
      config.setNode(DatasourceNodes.HOST.path(), parts[0]);
      config.setNode(DatasourceNodes.PORT.path(), parts[1]);
    } else {
      String dbtype = config.getNode(DatasourceNodes.TYPE.path());
      config.setNode(DatasourceNodes.PORT.path(), DatabaseCommand.getDefaultPort(dbtype));
    }

    return config;
  }

  /** Names of the various names under `datasource/` used for configuration. */
  public enum DatasourceNodes {
    HOST("host"),
    PORT("port"),
    TYPE("dbtype"),
    USERNAME("username"),
    PASSWORD("password"),
    DATABASE("database"),
    /** Configures the Oracle JDBC driver to use either SID or Service based access. */
    ORACLE_IDTYPE("idtype"),
    /**
     * Controls whether the MSSQL JDBC driver option `trustServerCertificate` should be true or
     * false.
     */
    MSSQL_TRUSTCERTS("trustservercerts");

    private final String path;

    DatasourceNodes(String node) {
      path = ROOT_NODE + "/" + node;
    }

    public String path() {
      return this.path;
    }
  }

  public enum DatabaseTypes {
    MSSQL("sqlserver"),
    ORACLE("oracle"),
    PGSQL("postgresql");

    private final String id;

    DatabaseTypes(String id) {
      this.id = id;
    }

    public String id() {
      return id;
    }
  }
}
