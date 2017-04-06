package com.tle.admin.taxonomy.wizard;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.admin.wizard.model.CustomControlModel;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.wizard.TermSelectorControl;

@SuppressWarnings("nls")
public class TermControlModel extends CustomControlModel<TermSelectorControl>
{
	public TermControlModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		TermSelectorControl control = getControl();

		if( control.isAllowMultiple() )
		{
			String error = Validation.noAttributeTargets(control);
			if( error != null )
			{
				return error;
			}
		}

		if( Check.isEmpty(control.getSelectedTaxonomy()) )
		{
			return CurrentLocale.get("com.tle.admin.taxonomy.tool.wizard.termselector.taxonomy.notselected");
		}

		if( Check.isEmpty(control.getDisplayType()) )
		{
			return CurrentLocale.get("com.tle.admin.taxonomy.tool.wizard.termselector.taxonomy.nodisplayselected");
		}

		return null;
	}
}
