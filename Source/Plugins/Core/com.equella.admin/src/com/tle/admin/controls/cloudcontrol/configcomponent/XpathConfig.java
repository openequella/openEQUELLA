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
