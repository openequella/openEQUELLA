package com.tle.web.sections.standard.model;

public class SimpleOption<T> implements Option<T>
{
	private String name;
	private String value;
	private boolean disabled;
	private T object;

	public SimpleOption(String name, String value)
	{
		this(name, value, null, false);
	}

	public SimpleOption(String name, String value, T object)
	{
		this(name, value, object, false);
	}

	public SimpleOption(String name, String value, T object, boolean disabled)
	{
		this.name = name;
		this.value = value;
		this.object = object;
		this.disabled = disabled;
	}

	@Override
	public T getObject()
	{
		return object;
	}

	@Override
	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean isNameHtml()
	{
		return false;
	}

	@Override
	public String getValue()
	{
		return value;
	}

	@Override
	public boolean isDisabled()
	{
		return disabled;
	}

	@Override
	public boolean hasAltTitleAttr()
	{
		return false;
	}

	@Override
	public String getAltTitleAttr()
	{
		return null;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof Option<?>) )
		{
			return false;
		}

		return ((Option<?>) obj).getValue().equals(value);
	}

	@Override
	public int hashCode()
	{
		return value.hashCode();
	}

	@Override
	public String getGroupName()
	{
		return null;
	}
}
