package com.tle.admin.controls.cloudcontrol;

import com.dytech.edge.admin.wizard.model.CustomControlModel;
import com.tle.admin.controls.CloudControlDefinitionImpl;
import com.tle.beans.cloudproviders.CloudControlConfig;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.cloud.CloudControl;

public class CloudControlModel extends CustomControlModel<CloudControl> {
  private CloudControlDefinitionImpl definition;

  public CloudControlModel(CloudControlDefinitionImpl definition) {
    super(definition);
    this.definition = definition;
  }

  @Override
  public String doValidation(ClientService clientService) {
    CloudControl cloudControl = getControl();
    if (cloudControl.getTitle() == null) {
      return CurrentLocale.get(
          "wizard.cloudcontrol.validation.message", CurrentLocale.get("wizard.controls.title"));
    }
    for (CloudControlConfig c : definition.getDef().configDefinition()) {
      if (cloudControl.isConfigMandatory(c)) {
        Object value = cloudControl.getAttributes().get(c.id());
        if (value == null || value.toString().isEmpty()) {
          return CurrentLocale.get("wizard.cloudcontrol.validation.message", c.name());
        }
      }
    }
    return null;
  }
}
