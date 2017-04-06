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
import com.tle.core.services.UrlService;
import com.tle.core.util.FindHrefHandler;
import com.tle.core.util.HrefCallback;
import com.tle.core.util.HtmlContentHandler;
import com.tle.mypages.parse.conversion.HrefConversion;

@Bind(ConvertHtmlService.class)
@Singleton
public class ConvertHtmlServiceImpl implements ConvertHtmlService
{
	@Inject
	private UrlService urlService;

	@Override
	public String convert(Reader reader, boolean fullUrl, List<HrefConversion> conversions)
	{
		return modifyXml(
			reader,
			new DefaultHrefCallback(fullUrl, true, urlService, conversions.toArray(new HrefConversion[conversions
				.size()])));
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
		return modifyXml(reader, new DefaultHrefCallback(fullUrl, true, urlService, conversions));
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
