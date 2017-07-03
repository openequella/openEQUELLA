/*
 * Created on May 24, 2005
 */
package com.tle.core.xstream.mapping;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * 
 */
public class DateMapping extends NodeMapping
{
	private final DateFormat format;

	public DateMapping(String name, String node, DateFormat format)
	{
		super(name, node);
		this.format = format;
	}

	public DateMapping(String name, String node, String format)
	{
		super(name, node);
		this.format = new SimpleDateFormat(format);
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Object value = super.getUnmarshalledValue(object, reader, context);
		try
		{
			if( value != null )
			{
				value = format.parseObject(value.toString());
			}
		}
		catch( ParseException e )
		{
			value = null;
		}
		return value;
	}

	@Override
	protected Object getMarshalledValue(Object object)
	{
		Object value = super.getMarshalledValue(object);
		if( value != null )
		{
			value = format.format((Date) value);
		}
		return value;
	}
}
