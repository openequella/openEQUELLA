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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dytech.edge.admin.wizard.Contexts;
import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.applet.client.ClientService;

/**
 * @author Nicholas Read
 */
public class PageModel extends AbstractPageModel<DefaultWizardPage>
{
	private DefaultWizardPage defPage;

	/**
	 * Constructs a new PageModel.
	 */
	public PageModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public void setWrappedObject(Object wrappedObject)
	{
		super.setWrappedObject(wrappedObject);
		defPage = (DefaultWizardPage) wrappedObject;
	}

	@Override
	public List<?> getChildObjects()
	{
		return defPage.getControls();
	}

	@Override
	public String getTargetBase()
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public LanguageBundle getTitle()
	{
		return defPage.getTitle();
	}

	@Override
	public boolean allowsChildren()
	{
		return true;
	}

	@Override
	public void setTitle(LanguageBundle title)
	{
		defPage.setTitle(title);
	}

	@Override
	public Object save()
	{
		List<WizardControl> controls = new ArrayList<WizardControl>();
		List<Control> children = getChildren();
		for( Control control : children )
		{
			controls.add((WizardControl) control.save());
		}
		defPage.setControls(controls);
		return defPage;
	}

	@Override
	public List<String> getContexts()
	{
		return Arrays.asList(Contexts.CONTEXT_PAGE);
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		return Validation.hasTitle(this);
	}

	public String getAdditionalCssClass()
	{
		return page.getAdditionalCssClass();
	}

	public void setAdditionalCssClass(String additionalCssClass)
	{
		page.setAdditionalCssClass(additionalCssClass);
	}
}
