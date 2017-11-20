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

package com.tle.admin.fedsearch;

import javax.swing.JPanel;

import com.tle.admin.gui.EditorException;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.SearchSettings;
import com.tle.common.EntityPack;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.AbstractPluginService;

public abstract class SearchPlugin<T extends SearchSettings>
{
	private final Class<T> settingsClass;
	protected JPanel panel;
	private ClientService clientService;
	private String KEY_PFX = AbstractPluginService.getMyPluginId(getClass()) + ".";

	protected String getString(String key)
	{
		return CurrentLocale.get(KEY_PFX+key);
	}

	public SearchPlugin(Class<T> settingsClass)
	{
		this.settingsClass = settingsClass;
	}

	public void setPanel(JPanel panel)
	{
		this.panel = panel;
	}

	public void setClientService(ClientService clientService)
	{
		this.clientService = clientService;
	}

	public ClientService getClientService()
	{
		return clientService;
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

	@SuppressWarnings({"unchecked"})
	public void loadSettings(EntityPack<FederatedSearch> gateway, SearchSettings settings)
	{
		load((T) settings);
	}

	@SuppressWarnings({"unchecked"})
	public void saveSettings(SearchSettings settings)
	{
		save((T) settings);
	}

	protected abstract void initGUI();

	public void validation() throws EditorException
	{
		// Do nothing
	}

	public abstract void load(T settings);

	public abstract void save(T settings);
}
