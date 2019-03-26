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

package com.tle.admin.controls;

import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.model.CustomControlModel;
import com.dytech.edge.wizard.beans.control.CustomControl;
import com.google.common.collect.ImmutableSet;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.cloudproviders.CloudControlDefinition;
import java.util.Set;

public class CloudControlDefinitionImpl implements ControlDefinition {

  private final CloudControlDefinition def;
  private final String fullId;
  private static final Set<String> contexts = ImmutableSet.of("page");

  public CloudControlDefinitionImpl(CloudControlDefinition def) {
    this.def = def;
    this.fullId = "cp." + def.providerId().toString() + "." + def.controlId();
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
    return new CustomControlModel<CustomControl>(this);
  }

  @Override
  public Object createWrappedObject() {

    CustomControl customControl = new CustomControl();
    customControl.setClassType(getId());
    return customControl;
  }
}
