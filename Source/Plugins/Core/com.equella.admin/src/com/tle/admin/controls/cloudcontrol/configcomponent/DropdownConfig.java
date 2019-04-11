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
        .getOptions()
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
