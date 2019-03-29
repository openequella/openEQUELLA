package com.tle.common.wizard.controls.cloud;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.beans.cloudproviders.CloudControlConfig;

public class CloudControl extends CustomControl {

  public CloudControl() {}

  public boolean isConfigMandatory(CloudControlConfig c) {
    return !(c.min() < c.max());
  }
}
