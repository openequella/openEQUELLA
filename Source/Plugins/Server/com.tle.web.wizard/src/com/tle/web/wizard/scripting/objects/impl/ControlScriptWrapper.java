package com.tle.web.wizard.scripting.objects.impl;

import com.dytech.edge.wizard.beans.control.EditBox;
import com.tle.common.i18n.LangUtils;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.wizard.scripting.objects.ControlScriptObject;

/**
 * @author aholland
 */
public class ControlScriptWrapper implements ControlScriptObject
{
	private final WizardPage page;
	private final HTMLControl control;

	public ControlScriptWrapper(HTMLControl control, WizardPage page)
	{
		this.control = control;
		this.page = page;
	}

	@Override
	public void setHidden(boolean hidden)
	{
		control.setHidden(hidden);
	}

	@Override
	public boolean isHidden()
	{
		return control.isHidden();
	}

	@Override
	public void setVisible(boolean visible)
	{
		control.setVisible(visible);
	}

	@Override
	public boolean isVisible()
	{
		return control.isVisible();
	}

	@Override
	public void setValue(String value)
	{
		control.setValues(value);
	}

	@Override
	public String getValue()
	{
		return control.getNameValue().getValue();
	}

	@Override
	public boolean isEmpty()
	{
		return control.isEmpty();
	}

	@Override
	public void clearInvalid()
	{
		control.clearInvalid();
	}

	@Override
	public void setInvalid(boolean invalid, String message)
	{
		control.setInvalid(invalid, LangUtils.createTextTempLangugageBundle(message));
	}

	@Override
	public void scriptEnter()
	{
	}

	@Override
	public void scriptExit()
	{
	}

	/*
	 * Note: this replicates the code in AbstractWebControl. Perhaps we should
	 * hold a reference to the AbstractWebControl instead? Not easy... Note the
	 * hackery for the EditBox type, since there is code in
	 * com.tle.web.wizard.standard.controls.EditBox to remove the page prefix
	 * from the id. (No, I don't know why)
	 */
	@Override
	public String getFormId()
	{
		if( control.getControlBean() instanceof EditBox )
		{
			return control.getFormName();
		}
		return "p" + page.getPageNumber() + control.getFormName(); //$NON-NLS-1$
	}
}
