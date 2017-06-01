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
