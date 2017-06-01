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

package com.tle.admin.harvester.standard;

import javax.swing.JComboBox;

import com.tle.admin.Driver;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.JNameValuePanel;
import com.tle.common.EntityPack;
import com.tle.common.NameValue;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.harvester.HarvesterProfileSettings;

public abstract class HarvesterPlugin<T extends HarvesterProfileSettings>
{
	private final Class<T> settingsClass;
	protected JNameValuePanel panel;
	protected Driver driver;

	public HarvesterPlugin(Class<T> settingsClass)
	{
		this.settingsClass = settingsClass;
	}

	public void setPanel(JNameValuePanel panel)
	{
		this.panel = panel;
	}

	public void setDriver(Driver driver)
	{
		this.driver = driver;
	}

	public Driver getDriver()
	{
		return driver;
	}

	public T newInstance()
	{
		T settings;
		try
		{
			settings = settingsClass.newInstance();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		return settings;
	}

	@SuppressWarnings("unchecked")
	public void loadSettings(EntityPack<HarvesterProfile> gateway, HarvesterProfileSettings settings)
	{
		load((T) settings);
	}

	@SuppressWarnings("unchecked")
	public void saveSettings(HarvesterProfileSettings settings)
	{
		save((T) settings);
	}

	public abstract void initGUI();

	public void validation() throws EditorException
	{
		// Do nothing
	}

	public abstract void load(T settings);

	public abstract void save(T settings);

	public abstract void validateSchema(JComboBox<NameValue> collections) throws EditorException;
}
