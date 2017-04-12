package com.dytech.edge.admin.wizard.editor;

import com.dytech.edge.admin.wizard.model.AbstractControlModel;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.admin.schema.SchemaModel;

public abstract class AbstractControlEditor<T extends WizardControl> extends Editor
{
	public AbstractControlEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
	}

	@SuppressWarnings("unchecked")
	public T getWizardControl()
	{
		AbstractControlModel<T> control = (AbstractControlModel<T>) getControl();
		return control.getControl();
	}
}
