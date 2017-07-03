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

package com.tle.web.controls.advancedscript.scripting;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.web.controls.advancedscript.scripting.objects.impl.RequestMapScriptWrapper;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.wizard.scripting.WizardScriptContextCreationParams;
import com.tle.web.wizard.scripting.WizardScriptObjectContributor;

/**
 * @author aholland
 */
@Bind
@Singleton
public class AdvancedScriptControlObjectContributor implements WizardScriptObjectContributor
{
	@Inject
	private InstitutionService institutionService;
	@Inject
	private ViewableItemFactory viewableItemFactory;

	@SuppressWarnings("unchecked")
	@Override
	public void addWizardScriptObjects(Map<String, Object> objects, WizardScriptContextCreationParams params)
	{
		final Map<String, Object> attributes = params.getAttributes();
		final String prefix = (String) attributes.get(AdvancedScriptWebControlConstants.PREFIX);
		if( prefix != null )
		{
			objects.put(AdvancedScriptWebControlConstants.PREFIX, prefix);
			objects.put(AdvancedScriptWebControlConstants.SUBMIT_JS,
				attributes.get(AdvancedScriptWebControlConstants.SUBMIT_JS));
			objects.put(AdvancedScriptWebControlConstants.ATTRIBUTES,
				attributes.get(AdvancedScriptWebControlConstants.ATTRIBUTES));

			final String wizId = (String) attributes.get(AdvancedScriptWebControlConstants.WIZARD_ID);
			if( wizId != null )
			{
				objects.put(AdvancedScriptWebControlConstants.PREVIEW_URL_BASE,
					institutionService.institutionalise(viewableItemFactory.getItemdirForPreview(wizId)));
			}

			final Map<Object, Object> requestMap = (Map<Object, Object>) attributes
				.get(AdvancedScriptWebControlConstants.REQUEST_MAP);
			if( requestMap != null )
			{
				objects.put(AdvancedScriptWebControlConstants.REQUEST_MAP,
					new RequestMapScriptWrapper(prefix, requestMap));
			}
		}
	}
}
