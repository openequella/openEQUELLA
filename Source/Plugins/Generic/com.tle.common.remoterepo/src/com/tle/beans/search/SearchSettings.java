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

package com.tle.beans.search;

import java.util.Map;

import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Check;

public abstract class SearchSettings
{
	private int timeout;
	private LanguageBundle name;
	private LanguageBundle description;
	private Map<String, String> attributes;
	private String advancedSearchFields;

	private String collectionUuid;

	private boolean disabled;

	public SearchSettings()
	{
		super();
	}

	public SearchSettings(FederatedSearch gateway)
	{
		this();
		load(gateway);
	}

	public void load(FederatedSearch gateway1)
	{
		this.attributes = gateway1.getAttributes();
		this.name = gateway1.getName();
		this.description = gateway1.getDescription();
		this.timeout = gateway1.getTimeout();
		this.collectionUuid = gateway1.getCollectionUuid();
		this.disabled = gateway1.isDisabled();
		this.advancedSearchFields = gateway1.getAdvancedSearchFields();
		_load();
	}

	public void save(FederatedSearch gateway1)
	{
		gateway1.setType(getType());
		gateway1.setTimeout(timeout);
		gateway1.setName(name);
		gateway1.setDescription(description);
		gateway1.setCollectionUuid(collectionUuid);
		gateway1.setDisabled(disabled);
		gateway1.setAdvancedSearchFields(advancedSearchFields);
		this.attributes = gateway1.getAttributes();
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

	public String getNonEmpty(String key, String defaultValue)
	{
		String value = attributes.get(key);
		if( Check.isEmpty(value) )
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

	public int getTimeout()
	{
		return timeout;
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	public LanguageBundle getName()
	{
		return name;
	}

	public void setName(LanguageBundle name)
	{
		this.name = name;
	}

	public String getCollectionUuid()
	{
		return collectionUuid;
	}

	public void setCollectionUuid(String collectionUuid)
	{
		this.collectionUuid = collectionUuid;
	}

	public boolean isDisabled()
	{
		return disabled;
	}

	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}

	public void setDescription(LanguageBundle description)
	{
		this.description = description;
	}

	public LanguageBundle getDescription()
	{
		return description;
	}

	public String getAdvancedSearchFields()
	{
		return advancedSearchFields;
	}

	public void setAdvancedSearchFields(String advancedSearchFields)
	{
		this.advancedSearchFields = advancedSearchFields;
	}
}
