/*
 * Created on Apr 22, 2005
 */
package com.dytech.edge.admin.wizard.model;

import java.util.Date;
import java.util.List;

import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.edge.wizard.beans.DRMPage.Container;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class DrmPageModel extends AbstractPageModel<DRMPage>
{
	public DrmPageModel(ControlDefinition definition)
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
		return null;
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		Container container = getPage().getContainer();
		Date acceptStart = container.getAcceptStart();
		Date acceptEnd = container.getAcceptEnd();
		if( acceptStart == null ^ acceptEnd == null )
		{
			return CurrentLocale.get("drm.validation.needdates");
		}
		return null;
	}
}
