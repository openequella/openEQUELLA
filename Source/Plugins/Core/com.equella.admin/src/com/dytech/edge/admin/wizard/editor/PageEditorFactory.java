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

package com.dytech.edge.admin.wizard.editor;

import com.dytech.edge.admin.wizard.model.Control;
import com.tle.admin.controls.EditorFactory;
import com.tle.admin.schema.SchemaModel;
import com.tle.core.plugins.PluginService;

public class PageEditorFactory implements EditorFactory
{
	@Override
	public Editor getEditor(Control control, int type, SchemaModel schemaModel, PluginService pluginService)
	{
		return new DefaultWizardPageEditor(control, type, schemaModel);
	}
}
