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

package com.tle.admin.usermanagement;

import org.java.plugin.registry.Extension;

import com.tle.admin.plugin.PluginSetting;

public class UMPConfig extends PluginSetting
{
	private String settingsClass;

	public UMPConfig(String className, String settingsClass, String name, int width, int height, Extension extension)
	{
		this.setPluginClass(className);
		this.setName(name);
		this.settingsClass = settingsClass;
		setExtension(extension);
		setWidth(width);
		setHeight(height);
	}

	public String getSettingsClass()
	{
		return settingsClass;
	}

	public void setSettingsClass(String settingsClass)
	{
		this.settingsClass = settingsClass;
	}

}
