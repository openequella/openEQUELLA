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

package com.dytech.edge.ejb.helpers.metadata.mappers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.dytech.common.io.UnicodeReader;
import com.tle.common.Check;

/**
 * A mapper for HTML data.
 */
@SuppressWarnings("nls")
public class HtmlMapper
{
	private static final String TAG_TITLE = "title";
	private final Set<String> metaTags;
	private final Map<String, String> mappings = new HashMap<String, String>();

	public HtmlMapper(Set<String> metaTags)
	{
		this.metaTags = metaTags;
	}

	public void mapMetaTags(InputStream stream)
	{
		XMLReader r = new Parser();
		InputSource s = new InputSource();
		s.setCharacterStream(new UnicodeReader(stream, "UTF-8"));
		try
		{
			r.setContentHandler(new MetaHandler());
			r.parse(s);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	private void addMapping(String key, String value)
	{
		if( !Check.isEmpty(value) )
		{
			mappings.put(key, value);
		}
	}

	public class MetaHandler extends DefaultHandler
	{
		private static final String TITLE_PATH = "/html/head/title";
		private static final String META_PATH = "/html/head/meta";
		private boolean inTitle;
		private String path = "";
		private StringBuilder titleBuffer = new StringBuilder();

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			path = path + '/' + localName;
			if( !inTitle && path.equals(TITLE_PATH) && metaTags.contains(TAG_TITLE) )
			{
				inTitle = true;
			}
			else if( path.equals(META_PATH) )
			{
				String name = attributes.getValue("name");
				if( metaTags.contains(name) )
				{
					addMapping(name, attributes.getValue("content"));
				}
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			if( inTitle )
			{
				titleBuffer.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			if( inTitle && path.equals(TITLE_PATH) )
			{
				inTitle = false;
				addMapping(TAG_TITLE, titleBuffer.toString());
			}
			path = path.substring(0, path.lastIndexOf('/'));
		}
	}

	public Map<String, String> getMappings()
	{
		return mappings;
	}
}
