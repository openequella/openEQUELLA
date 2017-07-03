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
import java.util.List;

import com.tle.core.services.html.HrefCallback;
import com.tle.core.services.html.HtmlContentHandler;
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