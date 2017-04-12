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
