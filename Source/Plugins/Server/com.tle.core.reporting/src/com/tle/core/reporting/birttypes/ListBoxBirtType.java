package com.tle.core.reporting.birttypes;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;

import com.dytech.edge.wizard.beans.control.ListBox;
import com.dytech.edge.wizard.beans.control.ShuffleBox;
import com.dytech.edge.wizard.beans.control.WizardControl;

public class ListBoxBirtType extends AbstractListBirtType
{
	public ListBoxBirtType(IScalarParameterDefn def, int paramNum, IParameterGroupDefn group)
	{
		super(def, paramNum, group);
	}

	@Override
	protected WizardControl createControl(IGetParameterDefinitionTask paramTask)
	{
		if( scalarDef.getScalarParameterType().equals("multi-value") ) //$NON-NLS-1$
		{
			return new ShuffleBox();
		}
		return new ListBox();
	}

}
