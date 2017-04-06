package com.dytech.edge.wizard.beans;

import java.util.List;

import com.dytech.edge.wizard.beans.control.TreeNav;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.beans.entity.LanguageBundle;

public class NavPage extends WizardPage
{
	private static final long serialVersionUID = 1L;
	public static final String TYPE = "navpage"; //$NON-NLS-1$
	private LanguageBundle title;

	public LanguageBundle getTitle()
	{
		return title;
	}

	public void setTitle(LanguageBundle title)
	{
		this.title = title;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	public DefaultWizardPage createPage()
	{
		DefaultWizardPage page = new DefaultWizardPage();
		page.setTitle(title);
		page.setScript(getScript());
		List<WizardControl> controls = page.getControls();
		controls.add(new TreeNav());
		return page;
	}
}
