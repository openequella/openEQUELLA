package com.tle.web.wizard.controls;

import com.dytech.edge.wizard.beans.control.RadioGroup;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.core.wizard.controls.WizardPage;

/**
 * Provides a base data model for CheckBox-like controls.
 * 
 * @author Nicholas Read
 */
public class CCheckBoxGroup extends OptionCtrl
{
	private static final long serialVersionUID = 1L;
	private String type;

	public CCheckBoxGroup(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
		if( controlBean instanceof RadioGroup )
		{
			type = "radio"; //$NON-NLS-1$
		}
		else
		{
			type = "checkbox"; //$NON-NLS-1$
		}
	}

	public String getType()
	{
		return type;
	}
}
