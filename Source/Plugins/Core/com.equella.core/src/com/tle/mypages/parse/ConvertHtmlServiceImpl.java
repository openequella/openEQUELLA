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

package com.tle.mypages.parse;

import java.io.Reader;
import java.io.StringWriter;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.dytech.edge.common.Constants;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.html.FindHrefHandler;
import com.tle.core.services.html.HrefCallback;
import com.tle.core.services.html.HtmlContentHandler;
import com.tle.mypages.parse.conversion.HrefConversion;

@Bind(ConvertHtmlService.class)
@Singleton
public class ConvertHtmlServiceImpl implements ConvertHtmlService
{
	@Inject
	private InstitutionService institutionService;

	@Override
	public String convert(Reader reader, boolean fullUrl, List<HrefConversion> conversions)
	{
		return modifyXml(reader, new DefaultHrefCallback(fullUrl, true, institutionService,
			conversions.toArray(new HrefConversion[conversions.size()])));
	}

	/**
	 * @param pageHtml
	 * @param fullUrl
	 * @param conversions
	 * @return
	 */
	@Override
	public String convert(Reader reader, boolean fullUrl, HrefConversion... conversions)
	{
		return modifyXml(reader, new DefaultHrefCallback(fullUrl, true, institutionService, conversions));
	}

	@Override
	public String modifyXml(Reader reader, HtmlContentHandler writer)
	{
		InputSource s = new InputSource();
		s.setEncoding(Constants.UTF8);
		s.setCharacterStream(reader);
		try
		{
			XMLReader r = new Parser();
			r.setContentHandler(writer);
			r.parse(s);
			return writer.getOutput();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Modifies all recognised href values using the callback supplied.
	 * Recognised href values are defined by FindHrefHandler.
	 * 
	 * @param pageHtml The original html
	 * @param callback When an href is found, use this callback to optionally
	 *            modify the href
	 * @return The modified html
	 */
	@Override
	public String modifyXml(Reader reader, HrefCallback callback)
	{
		return modifyXml(reader, new FindHrefHandler(new StringWriter(), callback, true, false));
	}
}
