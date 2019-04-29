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

package com.tle.admin.controls;

import com.dytech.edge.admin.wizard.model.Control;
import com.google.common.collect.ImmutableSet;
import com.tle.admin.controls.cloudcontrol.CloudControlEditor;
import com.tle.admin.controls.cloudcontrol.CloudControlModel;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.cloudproviders.CloudControlDefinition;
import com.tle.common.wizard.controls.cloud.CloudControl;
import java.util.Set;

public class CloudControlDefinitionImpl implements ControlDefinition {

  private final CloudControlDefinition def;
  private final String fullId;
  private static final Set<String> contexts = ImmutableSet.of("page");

  public CloudControlDefinitionImpl(CloudControlDefinition def) {
    this.def = def;
    this.fullId = "cp." + def.providerId().toString() + "." + def.controlId();
  }

  public CloudControlDefinition getDef() {
    return def;
  }

  @Override
  public EditorFactory editorFactory() {
    return new StandardEditorFactory();
  }

  @Override
  public Set<String> getContexts() {
    return contexts;
  }

  @Override
  public String getName() {
    return def.name();
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public String getId() {
    return fullId;
  }

  @Override
  public boolean hasContext(String context) {
    return getContexts().contains(context);
  }

  @Override
  public CloudControlEditor createEditor(Control control, int type, SchemaModel schema) {
    return new CloudControlEditor(def, control, type, schema);
  }

  @Override
  public String getIcon() {
    return def.iconUrl();
  }

  @Override
  public Control createControlModel() {
    return new CloudControlModel(this);
  }

  @Override
  public Object createWrappedObject() {

    CloudControl cloudControl = new CloudControl();
    cloudControl.setClassType(getId());
    return cloudControl;
  }
}
