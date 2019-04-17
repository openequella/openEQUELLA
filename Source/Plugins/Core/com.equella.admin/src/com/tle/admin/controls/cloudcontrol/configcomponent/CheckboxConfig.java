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
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class CheckboxConfig implements CloudControlConfigControl {
  private CloudControlConfig cloudControlConfig;
  private List<Pair<String, JCheckBox>> checkBoxes = new ArrayList<>();
  private JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

  public CheckboxConfig(
      CloudControlConfig cloudControlConfig, JPanel configPanel, ChangeDetector changeDetector) {
    this.cloudControlConfig = cloudControlConfig;
    cloudControlConfig
        .getOptions()
        .forEach(
            cloudConfigOption -> {
              JCheckBox checkBox = new JCheckBox(cloudConfigOption.name());
              changeDetector.watch(checkBox);
              checkBoxPanel.add(checkBox);
              checkBoxes.add(new Pair<>(cloudConfigOption.value(), checkBox));
            });
    configPanel.add(checkBoxPanel, BorderLayout.CENTER);
  }

  @Override
  public void saveConfig(CloudControl control) {
    List<String> checkBoxSelections = new ArrayList<>();
    for (Pair<String, JCheckBox> checkBox : checkBoxes) {
      if (checkBox.getSecond().isSelected()) {
        checkBoxSelections.add(checkBox.getFirst());
      }
    }
    control.getAttributes().put(cloudControlConfig.id(), checkBoxSelections);
  }

  @Override
  public void loadConfig(CloudControl control) {
    Object selections = control.getAttributes().get(cloudControlConfig.id());
    if (selections instanceof List) {

      List<String> checkBoxSelections = (List<String>) selections;
      for (Pair<String, JCheckBox> checkBox : checkBoxes) {
        checkBox.getSecond().setSelected(checkBoxSelections.contains(checkBox.getFirst()));
      }
    }
  }
}
