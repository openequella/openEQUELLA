package com.tle.admin.controls.cloudcontrol;

import com.tle.common.wizard.controls.cloud.CloudControl;

public interface CloudControlConfigControl {

  void saveConfig(CloudControl control);

  void loadConfig(CloudControl control);
}
