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
import com.tle.common.Pair;
import com.tle.common.wizard.controls.cloud.CloudControl;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class RadiobuttonConfig implements CloudControlConfigControl {
  private CloudControlConfig cloudControlConfig;
  private List<Pair<String, JRadioButton>> radioButtons = new ArrayList<>();
  private ButtonGroup radioButtonGroup = new ButtonGroup();
  private JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

  public RadiobuttonConfig(
      CloudControlConfig cloudControlConfig, JPanel configPanel, ChangeDetector changeDetector) {
    this.cloudControlConfig = cloudControlConfig;
    cloudControlConfig
        .getOptions()
        .forEach(
            cloudConfigOption -> {
              JRadioButton radioButton = new JRadioButton(cloudConfigOption.name());
              radioButtonGroup.add(radioButton);
              radioButtonPanel.add(radioButton);
              radioButtons.add(new Pair<>(cloudConfigOption.value(), radioButton));
            });
    changeDetector.watch(radioButtonGroup);
    configPanel.add(radioButtonPanel, BorderLayout.CENTER);
  }

  @Override
  public void saveConfig(CloudControl control) {
    for (int i = 0; i < radioButtons.size(); i++) {
      Pair<String, JRadioButton> button = radioButtons.get(i);
      if (button.getSecond().isSelected()) {
        control.getAttributes().put(cloudControlConfig.id(), button.getFirst());
        return;
      }
    }
  }

  @Override
  public void loadConfig(CloudControl control) {
    Object value = control.getAttributes().get(cloudControlConfig.id());
    if (value instanceof String) {
      for (Pair<String, JRadioButton> radio : radioButtons) {
        if (radio.getFirst().equals(value)) {
          radio.getSecond().setSelected(true);
          break;
        }
      }
    }
  }
}
