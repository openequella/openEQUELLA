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

package com.tle.admin.controls.repository;

import com.dytech.edge.admin.wizard.editor.Editor;
import com.dytech.edge.admin.wizard.model.Control;
import com.tle.admin.controls.EditorFactory;
import com.tle.admin.schema.SchemaModel;
import java.util.Set;

public interface ControlDefinition {
  EditorFactory editorFactory();

  Set<String> getContexts();

  String getName();

  String getId();

  boolean hasContext(String context);

  Editor createEditor(Control control, int type, SchemaModel schema);

  String getIcon();

  Control createControlModel();

  Object createWrappedObject();
}
