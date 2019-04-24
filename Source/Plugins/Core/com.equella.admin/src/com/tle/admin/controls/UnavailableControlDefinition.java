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

import com.dytech.edge.admin.wizard.editor.Editor;
import com.dytech.edge.admin.wizard.model.AbstractControlModel;
import com.dytech.edge.admin.wizard.model.Control;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.schema.SchemaModel;
import java.util.Set;

public class UnavailableControlDefinition implements ControlDefinition {

  private final String id;

  public UnavailableControlDefinition(String id) {
    this.id = id;
  }

  @Override
  public EditorFactory editorFactory() {
    return new StandardEditorFactory();
  }

  @Override
  public Set<String> getContexts() {
    return null;
  }

  @Override
  public String getName() {
    return "{" + id + "}";
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean hasContext(String context) {
    return false;
  }

  @Override
  public Editor createEditor(Control control, int type, SchemaModel schema) {
    return new UnavailableControlEditor(control, type, schema);
  }

  @Override
  public String getIcon() {
    return null;
  }

  @Override
  public Control createControlModel() {
    return new AbstractControlModel(this) {};
  }

  @Override
  public Object createWrappedObject() {
    return null;
  }
}
