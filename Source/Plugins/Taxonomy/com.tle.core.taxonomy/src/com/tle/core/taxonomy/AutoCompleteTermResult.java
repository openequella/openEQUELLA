package com.tle.core.taxonomy;

public class AutoCompleteTermResult
{
	private final String label;
	private final String value;

	public AutoCompleteTermResult(String label, String value)
	{
		this.label = label;
		this.value = value;
	}

	public String getLabel()
	{
		return label;
	}

	public String getValue()
	{
		return value;
	}
}
