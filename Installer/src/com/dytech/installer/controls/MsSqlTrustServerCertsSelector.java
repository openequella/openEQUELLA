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
