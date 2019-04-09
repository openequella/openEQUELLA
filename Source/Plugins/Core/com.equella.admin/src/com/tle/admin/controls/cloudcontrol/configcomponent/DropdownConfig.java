package com.tle.admin.controls.cloudcontrol.configcomponent;

import com.dytech.gui.ChangeDetector;
import com.tle.admin.controls.cloudcontrol.CloudControlConfigControl;
import com.tle.beans.cloudproviders.CloudControlConfig;
import com.tle.common.wizard.controls.cloud.CloudControl;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class DropdownConfig implements CloudControlConfigControl {
  private CloudControlConfig cloudControlConfig;
  private JComboBox<String> comboBox;

  public DropdownConfig(
      CloudControlConfig cloudControlConfig, JPanel configPanel, ChangeDetector changeDetector) {
    this.comboBox = new JComboBox<>();
    this.cloudControlConfig = cloudControlConfig;
    cloudControlConfig
        .options()
        .forEach(cloudConfigOption -> comboBox.addItem(cloudConfigOption.name()));
    changeDetector.watch(comboBox);
    configPanel.add(comboBox);
  }

  @Override
  public void saveConfig(CloudControl control) {
    control.getAttributes().put(cloudControlConfig.id(), comboBox.getSelectedIndex());
  }

  @Override
  public void loadConfig(CloudControl control) {
    if (control.getAttributes().containsKey(cloudControlConfig.id())) {
      comboBox.setSelectedIndex((int) control.getAttributes().get(cloudControlConfig.id()));
    }
  }
}
