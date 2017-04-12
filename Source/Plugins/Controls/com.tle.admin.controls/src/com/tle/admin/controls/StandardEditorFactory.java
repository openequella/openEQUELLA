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
