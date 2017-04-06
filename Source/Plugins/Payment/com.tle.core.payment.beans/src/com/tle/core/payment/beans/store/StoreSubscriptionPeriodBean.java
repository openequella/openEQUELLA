package com.tle.core.payment.beans.store;

import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Aaron
 */
public class StoreSubscriptionPeriodBean
{
	private String uuid;
	private int duration;
	private String durationUnit;
	private String name;
	private Map<String, String> nameStrings;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public int getDuration()
	{
		return duration;
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
	}

	public String getDurationUnit()
	{
		return durationUnit;
	}

	public void setDurationUnit(String durationUnit)
	{
		this.durationUnit = durationUnit;
	}

	@JsonIgnore
	public void setNameFromBundle(LanguageBundle name, String defaultName)
	{
		if( name != null && !name.isEmpty() )
		{
			setName(CurrentLocale.get(name));
			this.nameStrings = Maps.newHashMap();
			for( Entry<String, LanguageString> entry : name.getStrings().entrySet() )
			{
				nameStrings.put(entry.getKey(), entry.getValue().getText());
			}
		}
		else
		{
			setName(defaultName);
		}
	}

	public String getName()
	{
		return name;
	}

	/**
	 * Please use setNameFromBundle instead
	 * 
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
		if( nameStrings == null )
		{
			nameStrings = Maps.newHashMap();
		}
		nameStrings.put(CurrentLocale.getLocale().toString(), name);
	}

	public Map<String, String> getNameStrings()
	{
		return nameStrings;
	}

	public void setNameStrings(Map<String, String> nameStrings)
	{
		this.nameStrings = nameStrings;
	}
}
