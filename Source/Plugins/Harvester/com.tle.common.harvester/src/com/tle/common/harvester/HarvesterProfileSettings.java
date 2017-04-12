package com.tle.common.harvester;

import java.util.Map;

import com.tle.beans.entity.LanguageBundle;

public abstract class HarvesterProfileSettings
{
	private LanguageBundle name;
	private Map<String, String> attributes;

	public HarvesterProfileSettings()
	{
		super();
	}

	public HarvesterProfileSettings(HarvesterProfile gateway)
	{
		this();
		load(gateway);
	}

	public void load(HarvesterProfile gateway1)
	{
		this.attributes = gateway1.getAttributes();
		this.name = gateway1.getName();
		_load();
	}

	public void save(HarvesterProfile gateway1)
	{
		gateway1.setType(getType());
		this.attributes = gateway1.getAttributes();
		gateway1.setName(name);
		_save();
		for( Map.Entry<String, String> entry : attributes.entrySet() )
		{
			gateway1.setAttribute(entry.getKey(), entry.getValue());
		}
	}

	public String get(String key, String defaultValue)
	{
		String value = attributes.get(key);
		if( value == null )
		{
			value = defaultValue;
		}
		return value;
	}

	public boolean get(String key, boolean defaultValue)
	{
		String value = attributes.get(key);
		boolean v;
		if( value == null )
		{
			v = defaultValue;
		}
		else
		{
			v = Boolean.valueOf(value);
		}
		return v;
	}

	public int get(String key, int defaultValue)
	{
		String value = attributes.get(key);
		int v;
		if( value != null )
		{
			try
			{
				v = Integer.parseInt(value);
			}
			catch( Exception e )
			{
				v = defaultValue;
			}
		}
		else
		{
			v = defaultValue;
		}
		return v;
	}

	public void put(String key, Object value)
	{
		attributes.put(key, value.toString());
	}

	public void put(String key, String value)
	{
		attributes.put(key, value);
	}

	protected abstract String getType();

	protected abstract void _load();

	protected abstract void _save();

	public LanguageBundle getName()
	{
		return name;
	}

	public void setName(LanguageBundle name)
	{
		this.name = name;
	}

}
