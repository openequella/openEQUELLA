package com.tle.mypages.parse;

import java.io.Reader;
import java.util.List;

import com.tle.core.util.HrefCallback;
import com.tle.core.util.HtmlContentHandler;
import com.tle.mypages.parse.conversion.HrefConversion;

/**
 * @author Aaron
 */
public interface ConvertHtmlService
{
	String convert(Reader reader, boolean fullUrl, List<HrefConversion> conversions);

	String convert(Reader reader, boolean fullUrl, HrefConversion... conversions);

	String modifyXml(Reader reader, HtmlContentHandler writer);

	/**
	 * Modifies all recognised href values using the callback supplied.
	 * Recognised href values are defined by FindHrefHandler.
	 * 
	 * @param pageHtml The original html
	 * @param callback When an href is found, use this callback to optionally
	 *            modify the href
	 * @return The modified html
	 */
	String modifyXml(Reader reader, HrefCallback callback);
}