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
