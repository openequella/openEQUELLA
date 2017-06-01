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

package com.tle.web.wizard.controls;

/**
 * Bean for storing values for controls.
 */
public class Item
{
	private String name;
	private String value;
	private String defaultValue;
	private boolean selected;
	private boolean defaultSelected;

	public Item(String name, String value)
	{
		this(name, value, false);
	}

	public Item(String name, String value, boolean selected)
	{
		setName(name);
		setValue(value);
		setDefaultValue(value);
		setSelected(selected);
		setDefaultSelected(selected);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String szVal)
	{
		this.value = szVal;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public boolean isDefaultSelected()
	{
		return defaultSelected;
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public void setDefaultSelected(boolean defaultSelected)
	{
		this.defaultSelected = defaultSelected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}
}
