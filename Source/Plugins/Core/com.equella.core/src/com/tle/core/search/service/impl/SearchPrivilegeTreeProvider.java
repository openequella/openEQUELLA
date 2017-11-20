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

package com.tle.core.search.service.impl;

import javax.inject.Singleton;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class SearchPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider
{
	public SearchPrivilegeTreeProvider()
	{
		super(Type.SYSTEM_SETTING, "com.tle.core.search.securitytree.searchsettings", new SettingsTarget("searching"));
	}
}
