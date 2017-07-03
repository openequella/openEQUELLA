package com.tle.common.settings.standard;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;

public class DateFormatSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = 1;

	@Property(key = "date.format")
	private String dateFormat;

	public String getDateFormat()
	{
		return dateFormat;
	}

	public void setDateFormat(String dateFormat)
	{
		this.dateFormat = dateFormat;
	}
}
