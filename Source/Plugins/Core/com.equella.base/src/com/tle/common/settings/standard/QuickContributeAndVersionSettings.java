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

package com.tle.common.settings.standard;

import com.tle.beans.item.VersionSelection;
import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;

/**
 * @author larry
 */
public class QuickContributeAndVersionSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = 1069120212116280127L;

	@Property(key = "one.click.collection")
	private String oneClickCollection;
	@Property(key = "version.selection")
	private VersionSelection versionSelection;
	@Property(key = "select.summary.page.button.disable")
	private boolean buttonDisable;

	public void setOneClickCollection(String oneClickCollection)
	{
		this.oneClickCollection = oneClickCollection;
	}

	public String getOneClickCollection()
	{
		return oneClickCollection;
	}

	public void setVersionSelection(VersionSelection versionSelection)
	{
		this.versionSelection = versionSelection;
	}

	public VersionSelection getVersionSelection()
	{
		return versionSelection;
	}

	public boolean isButtonDisable()
	{
		return buttonDisable;
	}

	public void setButtonDisable(boolean buttonDisable)
	{
		this.buttonDisable = buttonDisable;
	}
}
