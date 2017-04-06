package com.tle.admin.controls.htmleditor;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.admin.wizard.model.CustomControlModel;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.htmleditmce.HtmlEditMceControl;

@SuppressWarnings("nls")
public class HtmlEditMceModel extends CustomControlModel<HtmlEditMceControl>
{
	public HtmlEditMceModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		final HtmlEditMceControl control = getControl();
		// If we've restricted all the possible sources ...
		if( control.isRestrictCollections() && control.isRestrictDynacolls() && control.isRestrictSearches()
			&& control.isRestrictContributables() )
		{
			// ... there must therefore be something, anything to restrict to.
			if( Check.isEmpty(control.getCollectionsUuids()) && Check.isEmpty(control.getDynaCollectionsUuids())
				&& Check.isEmpty(control.getSearchUuids()) && Check.isEmpty(control.getContributableUuids()) )
			{
				return CurrentLocale.get("com.tle.admin.controls.htmleditor.validate");
			}
		}
		return Validation.hasTarget(control);
	}
}
