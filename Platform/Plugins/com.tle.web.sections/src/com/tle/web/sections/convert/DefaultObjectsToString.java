package com.tle.web.sections.convert;

import java.util.Calendar;
import java.util.Date;

import net.entropysoft.transmorph.converters.ImmutableIdentityConverter;
import net.entropysoft.transmorph.converters.MultiConverter;
import net.entropysoft.transmorph.converters.ObjectToString;

public class DefaultObjectsToString extends MultiConverter
{
	public DefaultObjectsToString()
	{
		ObjectToString objectToString = new ObjectToString();
		objectToString.setHandledSourceClasses(new Class<?>[]{Number.class, Boolean.class, Calendar.class, Date.class});
		addConverter(new ImmutableIdentityConverter());
		addConverter(objectToString);
		addConverter(new EnumConverter());
	}
}
