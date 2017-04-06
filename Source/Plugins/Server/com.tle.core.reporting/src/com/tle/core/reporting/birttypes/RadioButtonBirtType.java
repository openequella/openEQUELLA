package com.tle.core.reporting.birttypes;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;

import com.dytech.edge.wizard.beans.control.RadioGroup;
import com.dytech.edge.wizard.beans.control.WizardControl;

public class RadioButtonBirtType extends AbstractListBirtType
{

	public RadioButtonBirtType(IScalarParameterDefn def, int paramNum)
	{
		super(def, paramNum);
	}

	@Override
	protected WizardControl createControl(IGetParameterDefinitionTask paramTask)
	{
		return new RadioGroup();
	}

}
