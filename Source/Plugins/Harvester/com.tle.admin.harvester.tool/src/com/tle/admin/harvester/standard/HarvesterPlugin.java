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
