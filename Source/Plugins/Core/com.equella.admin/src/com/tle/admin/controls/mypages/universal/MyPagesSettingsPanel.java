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

package com.tle.admin.controls.mypages.universal;

import com.tle.admin.controls.universal.UniversalControlSettingPanel;
import com.tle.common.wizard.controls.universal.UniversalSettings;

/**
 * @author Aaron
 */
public class MyPagesSettingsPanel extends UniversalControlSettingPanel
{
	@SuppressWarnings("nls")
	@Override
	protected String getTitleKey()
	{
		return getKey("mypages.settings.title");
	}

	@Override
	public void load(UniversalSettings state)
	{
	}

	@Override
	public void removeSavedState(UniversalSettings state)
	{
	}

	@Override
	public void save(UniversalSettings state)
	{
	}
}
