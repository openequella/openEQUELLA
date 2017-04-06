package com.tle.admin.controls;

import com.dytech.edge.admin.wizard.editor.Editor;
import com.dytech.edge.admin.wizard.model.Control;
import com.tle.admin.schema.SchemaModel;
import com.tle.core.plugins.PluginService;

public interface EditorFactory
{
	Editor getEditor(Control control, int type, SchemaModel schemaModel, PluginService pluginService);
}
