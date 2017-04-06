package com.dytech.edge.admin.wizard.model;

import java.util.List;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.wizard.beans.NavPage;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.applet.client.ClientService;

public class NavigationPageModel extends AbstractPageModel<NavPage>
{
	public NavigationPageModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public List<?> getChildObjects()
	{
		return null;
	}

	@Override
	public LanguageBundle getTitle()
	{
		return getPage().getTitle();
	}

	@Override
	public void setTitle(LanguageBundle title)
	{
		getPage().setTitle(title);
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		return Validation.hasTitle(this);
	}
}
