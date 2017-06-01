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

import java.lang.reflect.Constructor;

import org.java.plugin.registry.Extension;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.editor.Editor;
import com.dytech.edge.admin.wizard.model.Control;
import com.google.common.base.Throwables;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.schema.SchemaModel;
import com.tle.core.plugins.PluginService;

public class StandardEditorFactory implements EditorFactory
{

	@Override
	public Editor getEditor(Control control, int type, SchemaModel schema, PluginService pluginService)
	{
		ControlDefinition definition = control.getDefinition();

		Extension extension = definition.getExtension();
		String editorClassName = extension.getParameter("editorClass").valueAsString(); //$NON-NLS-1$

		try
		{
			Class<?> editorClass = pluginService.getClassLoader(extension.getDeclaringPluginDescriptor()).loadClass(
				editorClassName);
			Constructor<?> cons = editorClass.getConstructor(WizardHelper.getEditorParamTypes());

			// Invoke the constructor for the new editor.
			Object[] params = {control, type, schema};

			return (Editor) cons.newInstance(params);
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}
}
