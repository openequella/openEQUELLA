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

package com.tle.web.customlinks;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.tle.common.Check;

public class CustomLinkContentHandler implements ContentHandler
{
	private String baseUri;
	private String iconUrl;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		if( qName.equalsIgnoreCase("base") && Check.isEmpty(baseUri) )
		{
			baseUri = atts.getValue("href");
		}
		else if( qName.equalsIgnoreCase("link") && Check.isEmpty(iconUrl) )
		{
			if( atts.getValue("rel").equalsIgnoreCase("icon") )
			{
				iconUrl = atts.getValue("href");
			}
			else if( atts.getValue("rel").equalsIgnoreCase("shortcut icon") )
			{
				iconUrl = atts.getValue("href");
			}
		}
	}

	public void setBaseUri(String baseUri)
	{
		this.baseUri = baseUri;
	}

	public String getBaseUri()
	{
		return baseUri;
	}

	public void setIconUrl(String iconUrl)
	{
		this.iconUrl = iconUrl;
	}

	public String getIconUrl()
	{
		return iconUrl;
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException
	{

	}

	@Override
	public void startDocument() throws SAXException
	{

	}

	@Override
	public void skippedEntity(String name) throws SAXException
	{

	}

	@Override
	public void setDocumentLocator(Locator locator)
	{

	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException
	{

	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
	{

	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException
	{

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{

	}

	@Override
	public void endDocument() throws SAXException
	{

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{

	}

}