/*
 * Copyright 2019 Apereo
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

package com.dytech.common.log4j;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Format of CSV (quoting, commas, etc) follow the MS Excel and Python standard.
 * 
 * @author Nicholas Read
 */
public class CsvLayout1 extends Layout
{
	private final DateFormat DATE_FORMATTER = new TimeFormat();

	private List<String> contexts;

	public CsvLayout1()
	{
		super();
	}

	public void setContexts(final String contexts)
	{
		this.contexts = new ArrayList<String>();
		for( String context : contexts.split(",") )
		{
			context = context.trim();
			if( context.length() > 0 )
			{
				this.contexts.add(context);
			}
		}

		if( this.contexts.isEmpty() )
		{
			this.contexts = null;
		}
	}

	@Override
	public void activateOptions()
	{
		// Nothing to do here
	}

	@Override
	public String format(LoggingEvent event)
	{
		StringBuilder sbuf = new StringBuilder(60);

		sbuf.append(DATE_FORMATTER.format(new Date(event.timeStamp)));
		sbuf.append(',');

		sbuf.append(event.getLevel());
		sbuf.append(',');

		appendLoggerName(event, sbuf);
		sbuf.append(',');

		// The following appends it's own ending comma.
		appendContextText(event, sbuf);

		sbuf.append(escapeForCsv(event.getRenderedMessage()));
		sbuf.append(',');

		String s[] = event.getThrowableStrRep();
		if( s != null )
		{
			appendThrowableAsHTML(s, sbuf);
		}

		sbuf.append(Layout.LINE_SEP);

		return sbuf.toString();
	}

	protected void appendLoggerName(LoggingEvent event, StringBuilder sbuf)
	{
		String n = event.getLoggerName();
		int end = n.lastIndexOf('.', n.length() - 2);
		if( end >= 0 )
		{
			n = n.substring(end + 1);
		}
		sbuf.append(escapeForCsv(n));
	}

	protected void appendContextText(LoggingEvent event, StringBuilder sbuf)
	{
		if( contexts != null )
		{
			for( String context : contexts )
			{
				Object value = event.getMDC(context);
				if( value != null )
				{
					sbuf.append(escapeForCsv(value.toString()));
				}
				sbuf.append(',');
			}
		}
	}

	void appendThrowableAsHTML(String s[], StringBuilder sbuf)
	{
		if( s != null && s.length > 0 )
		{
			sbuf.append(escapeForCsv(s[0]));
			sbuf.append(Layout.LINE_SEP);
			for( int i = 1; i < s.length; i++ )
			{
				sbuf.append("    ");
				sbuf.append(escapeForCsv(s[i]));
				sbuf.append(Layout.LINE_SEP);
			}
		}
	}

	@Override
	public boolean ignoresThrowable()
	{
		return false;
	}

	@Override
	public String getHeader()
	{
		StringBuilder sbuf = new StringBuilder("Time,Level,Category,");
		if( contexts != null )
		{
			for( String context : contexts )
			{
				sbuf.append(context);
				sbuf.append(',');
			}
		}
		return sbuf.append("Message,Stacktrace").append(Layout.LINE_SEP).toString();
	}

	public static String escapeForCsv(String value)
	{
		value = value.replaceAll("\"", "\"\"");
		if( value.indexOf(',') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('"') >= 0 )
		{
			value = '"' + value + '"';
		}
		return value;
	}
}
