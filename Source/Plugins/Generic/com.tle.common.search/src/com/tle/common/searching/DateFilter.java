package com.tle.common.searching;

import java.util.Date;

/**
 * @author aholland
 */
public class DateFilter
{
	public enum Format
	{
		ISO, LONG
	}

	private final Format format;
	private final String indexFieldName;
	private final Date[] value;

	public DateFilter(String indexFieldName, Date[] value)
	{
		this(indexFieldName, value, Format.ISO);
	}

	public DateFilter(String indexFieldName, Date[] value, Format format)
	{
		this.indexFieldName = indexFieldName;
		this.value = value;
		this.format = format;
	}

	public String getIndexFieldName()
	{
		return indexFieldName;
	}

	public Date[] getRange()
	{
		return value;
	}

	public Format getFormat()
	{
		return format;
	}
}
