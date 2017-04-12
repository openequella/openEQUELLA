package com.tle.web.wizard.controls;

import com.dytech.edge.common.Constants;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.core.wizard.controls.WizardPage;

public class CHidden extends EditableCtrl
{
	private static final long serialVersionUID = 1L;

	public CHidden(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
	}

	@Override
	public void resetToDefaults()
	{
		setValues(Constants.BLANK);
	}
}
