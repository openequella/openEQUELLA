package com.tle.admin.controls.cloudcontrol.configcomponent;

import com.dytech.gui.ChangeDetector;
import com.tle.admin.controls.cloudcontrol.CloudControlConfigControl;
import com.tle.beans.cloudproviders.CloudControlConfig;
import com.tle.common.wizard.controls.cloud.CloudControl;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TextFieldConfig implements CloudControlConfigControl {
  private JTextField textField = new JTextField();
  private CloudControlConfig cloudControlConfig;

  public TextFieldConfig(
      CloudControlConfig cloudControlConfig, JPanel configPanel, ChangeDetector changeDetector) {
    this.cloudControlConfig = cloudControlConfig;
    changeDetector.watch(textField);
    configPanel.add(textField, BorderLayout.CENTER);
  }

  @Override
  public void saveConfig(CloudControl control) {
    control.getAttributes().put(cloudControlConfig.id(), textField.getText());
  }

  @Override
  public void loadConfig(CloudControl control) {
    if (control.getAttributes().containsKey(cloudControlConfig.id())) {
      textField.setText(control.getAttributes().get(cloudControlConfig.id()).toString());
    }
  }
}
