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

package com.tle.core.services.html;

import static com.google.common.collect.Sets.newHashSet;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.ccil.cowan.tagsoup.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class FindHrefHandler extends AbstractHtmlContentHandler
{
	@SuppressWarnings("nls")
	private static final Set<String> RECOGNISED_ATTRIBUTES = newHashSet("href", "src", "background", "value");

	/**
	 * If not outputting full html (as defined by property fullHtmlOutput) then
	 * do not output these tags
	 */
	@SuppressWarnings("nls")
	private static final Set<String> IGNORE_OUTPUT_TAGS = newHashSet("html", "body");

	private final HrefCallback callback;
	private final boolean parseText;
	private final boolean scanAllAttributes;
	private boolean outputFullHtml;

	/**
	 * @param w
	 * @param callback
	 * @param parseText You might want to set this to false since it's not
	 *            terribly efficient.
	 */
	public FindHrefHandler(StringWriter w, HrefCallback callback, boolean parseText, boolean scanAllAttributes)
	{
		super(w);
		this.callback = callback;
		this.parseText = parseText;
		this.scanAllAttributes = scanAllAttributes;
	}

	@Override
	public void characters(char[] text, int start, int length) throws SAXException
	{
		if( parseText )
		{
			char[] chars = checkText(new String(text, start, length)).toCharArray();
			super.characters(chars, 0, chars.length);
		}
		else
		{
			super.characters(text, start, length);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		if( !ignoreTag(localName) )
		{
			final Set<String> lookAt;
			if( scanAllAttributes )
			{
				lookAt = new HashSet<String>();
				for( int i = 0; i < atts.getLength(); i++ )
				{
					lookAt.add(atts.getLocalName(i));
				}
			}
			else
			{
				lookAt = RECOGNISED_ATTRIBUTES;
			}

			for( String attr : lookAt )
			{
				checkHref((AttributesImpl) atts, attr);
			}

			super.startElement(outputNamespaces ? uri : "", localName, qName, atts); //$NON-NLS-1$
		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException
	{
		if( !ignoreTag(localName) )
		{
			super.endElement(uri, localName, name);
		}
	}

	private String checkText(String text)
	{
		return callback.textFound(text);
	}

	private void checkHref(AttributesImpl atts, String tag)
	{
		int index = atts.getIndex(tag);
		if( index == -1 )
		{
			return;
		}
		String val = atts.getValue(index);
		String origval = val;
		int popupind = val.indexOf("popup_percent"); //$NON-NLS-1$
		int ustart = -1;
		int uend = -1;
		if( popupind != -1 )
		{
			ustart = val.indexOf('\'', popupind) + 1;
			uend = val.indexOf('\'', ustart);
			val = val.substring(ustart, uend);
		}
		else
		{
			if( tag.equals("onclick") )
			{
				return;
			}
		}
		String newatt = callback.hrefFound(tag, val, atts);
		if( newatt != null )
		{
			if( ustart != -1 )
			{
				newatt = origval.substring(0, ustart) + newatt + origval.substring(uend);
			}
			atts.setValue(index, newatt);
		}
	}

	private boolean ignoreTag(String tag)
	{
		return (!outputFullHtml && IGNORE_OUTPUT_TAGS.contains(tag.toLowerCase()));
	}

	public boolean isOutputFullHtml()
	{
		return outputFullHtml;
	}

	public void setOutputFullHtml(boolean outputFullHtml)
	{
		this.outputFullHtml = outputFullHtml;
	}
}
