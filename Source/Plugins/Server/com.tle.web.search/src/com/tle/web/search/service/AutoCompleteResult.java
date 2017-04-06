package com.tle.web.search.service;

public class AutoCompleteResult
{
	private final String label;
	private final String value;
	private final boolean term;

	public AutoCompleteResult(String label, String value, boolean term)
	{
		this.label = label;
		this.value = value;
		this.term = term;
	}

	public String getLabel()
	{
		return label;
	}

	public String getValue()
	{
		return value;
	}

	public boolean isTerm()
	{
		return term;
	}

}