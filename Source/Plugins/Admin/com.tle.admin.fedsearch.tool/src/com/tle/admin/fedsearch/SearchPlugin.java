package com.tle.admin.fedsearch;

import javax.swing.JPanel;

import com.tle.admin.gui.EditorException;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.SearchSettings;
import com.tle.common.EntityPack;
import com.tle.common.applet.client.ClientService;

public abstract class SearchPlugin<T extends SearchSettings>
{
	private final Class<T> settingsClass;
	protected JPanel panel;
	private ClientService clientService;

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
