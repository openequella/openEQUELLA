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
