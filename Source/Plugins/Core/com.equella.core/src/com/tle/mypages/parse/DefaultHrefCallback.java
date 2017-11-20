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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.ccil.cowan.tagsoup.AttributesImpl;

import com.dytech.edge.common.Constants;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.html.HrefCallback;
import com.tle.mypages.parse.conversion.HrefConversion;

@SuppressWarnings("nls")
public class DefaultHrefCallback implements HrefCallback
{
	private static final Logger LOGGER = Logger.getLogger(DefaultHrefCallback.class);

	protected static final List<String> PROTOCOLS = Arrays.asList(new String[]{"http", "https", "ftp", "file", "jar"});

	private final InstitutionService institutionService;
	private final boolean fullUrl;
	private final boolean deinstitutionFoundUrls;
	private final HrefConversion[] conversions;

	/**
	 * @param fullUrl
	 * @param deinstitutionFoundUrls Removes the institution base from found
	 *            hrefs before passing to converters
	 * @param urlService
	 * @param conversions
	 */
	public DefaultHrefCallback(boolean fullUrl, boolean deinstitutionFoundUrls, InstitutionService institutionService,
		HrefConversion... conversions)
	{
		this.fullUrl = fullUrl;
		this.deinstitutionFoundUrls = deinstitutionFoundUrls;
		this.institutionService = institutionService;
		this.conversions = conversions;
	}

	@Override
	public String hrefFound(final String tag, final String url, AttributesImpl atts)
	{
		try
		{
			// Assume all links have basehref of institution
			if( recognisedProtocol(url) )
			{
				final boolean anchor = isNamedAnchor(url);
				final boolean institutionUrl = institutionService.isInstitutionUrl(url);
				if( anchor || !URLUtils.isAbsoluteUrl(url) || institutionUrl )
				{
					String newUrl = url;

					// Remove the institution part (if there is one).
					if( institutionUrl && deinstitutionFoundUrls )
					{
						newUrl = institutionService.removeInstitution(newUrl);
					}

					// we need to change the path ('items/UUID' to
					// 'preview/STAGING_ID' or vice-versa)
					for( HrefConversion conversion : conversions )
					{
						newUrl = conversion.convert(newUrl, atts);
					}

					if( fullUrl && !isNamedAnchor(newUrl) )
					{
						// Add the institution part again if it's going to
						// be displayed in an HTML editor
						return institutionService.institutionalise(newUrl);
					}
					return newUrl;
				}
			}
		}
		catch( RuntimeException e )
		{
			// Ignore - possibly parsed incorrectly or contains bad characters.
			// In any case, we don't want to stop things from working because of
			// some dodgy text.

			LOGGER.warn("Error parsing URL: " + url);
		}

		return null;
	}

	protected boolean isNamedAnchor(String url)
	{
		return url.startsWith("#");
	}

	@Override
	public String textFound(String text)
	{
		for( String proto : PROTOCOLS )
		{
			String protoText = proto + "://";
			int protoIndex = 0;
			protoIndex = text.toLowerCase().indexOf(protoText, protoIndex);
			while( protoIndex >= 0 )
			{
				// look for end of url
				int urlend = protoIndex;
				while( urlend < text.length() && !ws(text.charAt(urlend)) )
				{
					urlend++;
				}

				String url = text.substring(protoIndex, urlend);
				String newurl = hrefFound(Constants.BLANK, url, null);
				if( newurl != null )
				{
					text = text.replace(url, newurl);
				}

				protoIndex = text.toLowerCase().indexOf(protoText, urlend);
			}
		}
		return text;
	}

	private boolean ws(char c)
	{
		return c == ' ' || c == '\t' || c == '\r' || c == '\n';
	}

	/**
	 * Only work on the Java URL default protocols http, https, ftp, file, and
	 * jar otherwise things like javascript:XX will blow up the URL class
	 * 
	 * @param url
	 * @return Starts with any recognised protocol
	 */
	private boolean recognisedProtocol(String url)
	{
		if( !Check.isEmpty(url) )
		{
			if( isNamedAnchor(url) )
			{
				return true;
			}

			try
			{
				URL u = new URL(url);
				return PROTOCOLS.contains(u.getProtocol());
			}
			catch( MalformedURLException dontCare )
			{
				// need to assume 'yes', as this is probably a URL of the form:
				// items/3a6767-fda67-67f86-a788/2
				return true;
			}
		}
		return false;
	}
}
