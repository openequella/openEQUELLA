package com.tle.web.sections.convert;

import net.entropysoft.transmorph.converters.ImmutableIdentityConverter;
import net.entropysoft.transmorph.converters.MultiConverter;
import net.entropysoft.transmorph.converters.StringToBoolean;
import net.entropysoft.transmorph.converters.StringToCalendar;
import net.entropysoft.transmorph.converters.StringToDate;
import net.entropysoft.transmorph.converters.StringToNumber;
import net.entropysoft.transmorph.converters.enums.StringToEnum;

public class DefaultFromStringConverter extends MultiConverter
{
	private StringToEnum stringToEnum = new StringToEnum();
	private StringToBoolean stringToBoolean = new StringToBoolean();
	private StringToCalendar stringToCalendar = new StringToCalendar();
	private StringToDate stringToDate = new StringToDate();
	private StringToNumber stringToNumber = new StringToNumber();
	private ImmutableIdentityConverter immutable = new ImmutableIdentityConverter();

	public DefaultFromStringConverter()
	{
		super(false);
		addConverter(immutable);
		addConverter(stringToEnum);
		addConverter(stringToBoolean);
		addConverter(stringToCalendar);
		addConverter(stringToDate);
		addConverter(stringToNumber);
	}
}
