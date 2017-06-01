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

package com.tle.web.wizard.scripting.objects.impl;

import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.wizard.scripting.objects.ControlScriptObject;
import com.tle.web.wizard.scripting.objects.PageScriptObject;

/**
 * @author aholland
 */
public class PageScriptWrapper implements PageScriptObject
{
	private static final long serialVersionUID = 1L;

	private final WizardPage page;

	public PageScriptWrapper(WizardPage page)
	{
		this.page = page;
	}

	@Override
	public boolean isEnabled()
	{
		return page.isEnabled();
	}

	@Override
	public int getPageNumber()
	{
		return page.getPageNumber();
	}

	@Override
	public String getPageTitle()
	{
		return page.getPageTitle();
	}

	@Override
	public boolean isValid()
	{
		return page.isValid();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		page.setEnabled(enabled);
	}

	@Override
	public void setPageTitle(String title)
	{
		page.setPageTitle(title);
	}

	@Override
	public void setValid(boolean valid)
	{
		page.setValid(valid);
	}

	@Override
	public int getControlCount()
	{
		return page.getControls().size();
	}

	@Override
	public ControlScriptObject getControlByIndex(int index)
	{
		if( index < 0 || index >= page.getControls().size() )
		{
			return null;
		}
		return new ControlScriptWrapper(page.getControls().get(index), page);
	}

	@Override
	public void scriptEnter()
	{
	}

	@Override
	public void scriptExit()
	{
	}
}
