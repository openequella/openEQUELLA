/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
