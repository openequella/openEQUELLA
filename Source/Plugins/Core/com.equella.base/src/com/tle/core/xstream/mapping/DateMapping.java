/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
