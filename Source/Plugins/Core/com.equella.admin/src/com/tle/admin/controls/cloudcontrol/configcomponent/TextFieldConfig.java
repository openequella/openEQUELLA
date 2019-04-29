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
