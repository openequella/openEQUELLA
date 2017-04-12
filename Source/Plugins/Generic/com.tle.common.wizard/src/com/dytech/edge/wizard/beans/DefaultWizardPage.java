/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans;

import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.beans.entity.LanguageBundle;

public class DefaultWizardPage extends WizardPage
{
	private static final long serialVersionUID = 1;

	public static final String TYPE = "page"; //$NON-NLS-1$

	private LanguageBundle title;
	private String customName;
	private List<WizardControl> controls = new ArrayList<WizardControl>();
	private String additionalCssClass;

	@Override
	public String getType()
	{
		return TYPE;
	}

	public String getCustomName()
	{
		return customName;
	}

	public void setCustomName(String customName)
	{
		this.customName = customName;
	}

	public LanguageBundle getTitle()
	{
		return title;
	}

	public void setTitle(LanguageBundle title)
	{
		this.title = title;
	}

	public List<WizardControl> getControls()
	{
		return controls;
	}

	public void setControls(List<WizardControl> controls)
	{
		this.controls = controls;
	}

	public String getAdditionalCssClass()
	{
		return additionalCssClass;
	}

	public void setAdditionalCssClass(String additionalCssClass)
	{
		this.additionalCssClass = additionalCssClass;
	}
}
