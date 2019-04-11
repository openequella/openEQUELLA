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

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.editor.Editor;
import com.dytech.gui.ChangeDetector;
import com.tle.admin.controls.cloudcontrol.CloudControlConfigControl;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.beans.cloudproviders.CloudControlConfig;
import com.tle.common.wizard.controls.cloud.CloudControl;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JPanel;

public class XpathConfig implements CloudControlConfigControl {
  private CloudControlConfig cloudControlConfig;
  private MultiTargetChooser picker;

  public XpathConfig(
      CloudControlConfig cloudControlConfig,
      JPanel configPanel,
      ChangeDetector changeDetector,
      Editor editor) {
    this.picker = WizardHelper.createMultiTargetChooser(editor);
    this.cloudControlConfig = cloudControlConfig;
    changeDetector.watch(picker);
    configPanel.add(picker, BorderLayout.CENTER);
  }

  @Override
  public void saveConfig(CloudControl control) {
    control.getAttributes().put(cloudControlConfig.id(), picker.getTargets());
  }

  @Override
  @SuppressWarnings("unchecked")
  public void loadConfig(CloudControl control) {
    if (control.getAttributes().containsKey(cloudControlConfig.id())) {
      picker.setTargets((List<String>) control.getAttributes().get(cloudControlConfig.id()));
    }
  }
}
