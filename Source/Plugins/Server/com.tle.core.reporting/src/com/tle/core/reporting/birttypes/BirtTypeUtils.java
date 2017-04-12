package com.tle.core.reporting.birttypes;

import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;

public final class BirtTypeUtils
{
	private BirtTypeUtils()
	{
		throw new Error();
	}

	public static AbstractBirtType createWrapper(IScalarParameterDefn scalarDef, int paramNum, IParameterGroupDefn group)
	{
		AbstractBirtType control = null;

		int controlType = scalarDef.getControlType();
		if( controlType == IScalarParameterDefn.CHECK_BOX )
		{
			control = new CheckBoxBirtType(scalarDef, paramNum);
		}
		else if( controlType == IScalarParameterDefn.TEXT_BOX )
		{
			control = new TextBoxBirtType(scalarDef, paramNum);
		}
		else if( controlType == IScalarParameterDefn.LIST_BOX )
		{
			control = new ListBoxBirtType(scalarDef, paramNum, group);
		}
		else if( controlType == IScalarParameterDefn.RADIO_BUTTON )
		{
			control = new RadioButtonBirtType(scalarDef, paramNum);
		}
		if( control == null )
		{
			throw new BirtTypeException("invalid control type specified."); //$NON-NLS-1$
		}

		return control;
	}
}
