package com.tle.common;

/**
 * @author Nicholas Read
 */
public class NameValue extends Pair<String, String>
{
	private static final long serialVersionUID = 1;

	public NameValue()
	{
		super();
	}

	public NameValue(String name, String value)
	{
		super(name, value);
	}

	public String getName()
	{
		return getFirst();
	}

	public void setName(String name)
	{
		setFirst(name);
	}

	public String getLabel()
	{
		return getName();
	}

	public void setLabel(String label)
	{
		setName(label);
	}

	public String getValue()
	{
		return getSecond();
	}

	public void setValue(String value)
	{
		setSecond(value);
	}

	@Override
	public boolean checkFields(Pair<String, String> rhs)
	{
		// Only check the value of this object type
		return Check.bothNullOrEqual(rhs.getSecond(), getSecond());
	}
}
