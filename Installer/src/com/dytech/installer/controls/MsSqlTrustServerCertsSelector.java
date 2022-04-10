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

package com.dytech.installer.controls;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.installer.DatasourceConfig.DatabaseTypes;
import com.dytech.edge.installer.DatasourceConfig.DatasourceNodes;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Wizard;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;

/**
 * Control to conditionally display for MS SQL server installs to determine whether to trust server
 * certificates as introduced in mssql-jdbc 10.2.0.
 *
 * <p>Based on com.dytech.installer.controls.OracleIdSelector
 */
public class MsSqlTrustServerCertsSelector extends GCheckBoxGroup {
  Wizard grandParent;
  PropBagEx defaults;

  public MsSqlTrustServerCertsSelector(PropBagEx controlBag, Wizard grandParent)
      throws InstallerException {
    super(controlBag);

    this.grandParent = grandParent;
    this.defaults = grandParent != null ? grandParent.getDefaults() : null;
    if (items.size() != 1) {
      throw new InstallerException(
          "Expected 1 item definitions for MS SQL trust server certificates selector.");
    }

    update();
  }

  @Override
  public JComponent generateControl() {
    JComponent generated = super.generateControl();
    update();
    return generated;
  }

  private void update() {
    PropBagEx stateOfThings = grandParent.getOutputNow();
    String dbtype = stateOfThings.getNode(DatasourceNodes.TYPE.path());
    boolean relevance = (DatabaseTypes.MSSQL.id().equals(dbtype));
    if (relevance) {
      loadControl(defaults);
    } else {
      // If not relevant, hide
      this.hide();
    }
  }

  @Override
  public AbstractButton generateButton(String name, ButtonGroup group) {
    return super.generateButton(name, group);
  }
}
