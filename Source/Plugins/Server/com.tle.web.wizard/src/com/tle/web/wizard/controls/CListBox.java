package com.tle.web.wizard.controls;

import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.core.wizard.controls.WizardPage;

/**
 * Provides a data model for list box controls.
 * 
 * @author Nicholas Read
 */
public class CListBox extends OptionCtrl
{
	private static final long serialVersionUID = 1L;

	public CListBox(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
	}

	public boolean isShowSelection()
	{
		return !isMandatory() || isEmpty();
	}
}
