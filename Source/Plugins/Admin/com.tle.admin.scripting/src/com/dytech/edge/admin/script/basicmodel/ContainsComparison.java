package com.dytech.edge.admin.script.basicmodel;

import com.dytech.edge.admin.script.ifmodel.Comparison;
import com.dytech.edge.admin.script.ifmodel.IfModel;

public class ContainsComparison implements Comparison
{
	protected String xpath;
	protected String value;

	public ContainsComparison(String xpath, String value)
	{
		this.xpath = xpath;
		this.value = value;
	}

	public String getXpath()
	{
		return xpath;
	}

	public void setXpath(String xpath)
	{
		this.xpath = xpath;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public String toScript()
	{
		return "xml.contains('" + xpath + "', '" + IfModel.encode(value) + "')";
	}

	@Override
	public String toEasyRead()
	{
		// We want to make contains look like equals, so we don't want
		// the following anymore.
		// return xpath + " <b>contains</b> '" + value + "'";

		return xpath + " <b>=</b> '" + value + "'";
	}
}
