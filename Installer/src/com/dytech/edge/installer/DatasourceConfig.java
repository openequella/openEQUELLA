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
  private final String localPath;
  private final String database;
  private final String installPath;

  public DatasourceConfig(PropBagEx commandBag, PropBagEx resultBag) throws InstallerException {
    super(commandBag, resultBag);

    database = resultBag.getNode("datasource/dbtype"); // $NON-NLS-1$
    localPath = resultBag.getNode("installer/local"); // $NON-NLS-1$
    installPath = resultBag.getNode("install.path"); // $NON-NLS-1$
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
      Substitution sub = new Substitution(new XpathResolver(resultBag), "${ }"); // $NON-NLS-1$
      sub.resolve(in, out);
      propogateSubtaskCompleted();
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new InstallerException("Problem copying datasources.xml");
    }
  }

  /** Return a nice string for the progress box to show. */
  @Override
  public String toString() {
    return "Updating data sources XML...";
  }
}
