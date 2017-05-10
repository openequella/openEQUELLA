package com.tle.web.remoting.resteasy;

import java.text.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.tle.common.i18n.CurrentTimeZone;

public class ISO8061DateFormatWithTZ extends DateFormat
{

	private static final long serialVersionUID = 1L;

	// those classes are to try to allow a consistent behaviour for hashcode,
	// equals and other methods
	private static Calendar CALENDAR = new GregorianCalendar();
	private static NumberFormat NUMBER_FORMAT = new DecimalFormat();

	public ISO8061DateFormatWithTZ()
	{
		this.numberFormat = NUMBER_FORMAT;
		this.calendar = CALENDAR;
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition)
	{
		String value = ISO8601Utils.format(date, true, CurrentTimeZone.get());
		toAppendTo.append(value);
		return toAppendTo;
	}

	@Override
	public Date parse(String source, ParsePosition pos)
	{
		// index must be set to other than 0, I would swear this requirement is
		// not there in some version of jdk 6.
		String toParse = source;
		if( !toParse.toUpperCase().contains("T") )
		{
			toParse = toParse + "T00:00:00Z";
		}
		try {
			return ISO8601Utils.parse(toParse, pos);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	// Sonar has trouble determining that superclass implements cloneable
	//@formatter:off
	@Override // NOSONAR
	//@formatter:on
	public Object clone() // NOSONAR - no need to declare throw
	{
		// jackson calls clone every time. We are threadsafe so just returns the
		// instance
		return this; // NOSONAR
	}
}
