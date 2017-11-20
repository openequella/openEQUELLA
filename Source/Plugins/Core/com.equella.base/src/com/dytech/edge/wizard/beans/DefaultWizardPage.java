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

package com.dytech.edge.wizard.beans;

import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.beans.entity.LanguageBundle;

public class DefaultWizardPage extends WizardPage
{
	private static final long serialVersionUID = 1;

	public static final String TYPE = "page"; //$NON-NLS-1$

	private LanguageBundle title;
	private String customName;
	private List<WizardControl> controls = new ArrayList<WizardControl>();
	private String additionalCssClass;

	@Override
	public String getType()
	{
		return TYPE;
	}

	public String getCustomName()
	{
		return customName;
	}

	public void setCustomName(String customName)
	{
		this.customName = customName;
	}

	public LanguageBundle getTitle()
	{
		return title;
	}

	public void setTitle(LanguageBundle title)
	{
		this.title = title;
	}

	public List<WizardControl> getControls()
	{
		return controls;
	}

	public void setControls(List<WizardControl> controls)
	{
		this.controls = controls;
	}

	public String getAdditionalCssClass()
	{
		return additionalCssClass;
	}

	public void setAdditionalCssClass(String additionalCssClass)
	{
		this.additionalCssClass = additionalCssClass;
	}
}
