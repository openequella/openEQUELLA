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

package com.tle.admin.controls.cloudcontrol;

import com.dytech.edge.admin.wizard.model.CustomControlModel;
import com.tle.admin.controls.CloudControlDefinitionImpl;
import com.tle.admin.i18n.Lookup;
import com.tle.beans.cloudproviders.CloudControlConfig;
import com.tle.beans.cloudproviders.CloudControlConfigType;
import com.tle.common.applet.client.ClientService;
import com.tle.common.wizard.controls.cloud.CloudControl;
import java.util.List;

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
      return Lookup.lookup.text(
          "cloudcontrol.validation.message", Lookup.lookup.text("cloudcontrol.title"));
    }
    for (CloudControlConfig c : definition.getDef().getConfigDefinition()) {
      if (c.isConfigMandatory()) {
        Object value = cloudControl.getAttributes().get(c.id());
        if (value == null
            || value.toString().isEmpty()
            || (c.configType() == CloudControlConfigType.XPath()
                && ((List<?>) value).size() == 0)) {
          return Lookup.lookup.text("cloudcontrol.validation.message", c.name());
        }
      }
    }
    return null;
  }
}
