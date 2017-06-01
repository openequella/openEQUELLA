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

package com.tle.web.controls.flickr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.Size;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.NameValueExtra;

/**
 * @author Larry. Based on the YouTube plugin.
 */
@SuppressWarnings("nls")
public final class FlickrUtils
{
	public static final String ATTACHMENT_TYPE = "flickr";
	public static final String INSTITUTION_KEY = "institution";
	public static final String CREATIVE_COMMONS_LICENCES_KEY = "commonsLicences";
	public static final int CREATIVE_COMMONS_ELEMENTS_PER_LINE = 3;

	public static final String MIME_TYPE = "equella/attachment-flickr";

	public static final String PROPERTY_SHOW_URL = "showUrl";
	public static final String PROPERTY_MEDIUM_URL = "mediumUrl";
	public static final String PROPERTY_THUMB_URL = "thumbUrl";
	public static final String PROPERTY_ID = "photoId";
	public static final String PROPERTY_AUTHOR = "uploader";
	public static final String PROPERTY_DATE_POSTED = "uploaded";
	public static final String PROPERTY_DATE_TAKEN = "taken";
	public static final String PROPERTY_IMAGE_SIZE = "imagesize";
	public static final String PROPERTY_LICENCE_KEY = "licencekey";
	public static final String PROPERTY_LICENCE_CODE = "licencecode";
	public static final String PROPERTY_LICENCE_NAME = "licencename";

	public static final int PAGER_PER_PAGE = 10;
	public static final int DESCR_DISPLAY_LEN = 256;

	private FlickrUtils()
	{
		throw new Error();
	}

	/**
	 * Lines beginning with "#", or entirely blank/whitespace lines, are
	 * ignored. Each line is assumed to consist of a single token, (the flickr
	 * user ID of the institution, a single space, and then the rest of the line
	 * as institution name.
	 * 
	 * @param filename
	 * @return
	 */
	public static List<String[]> getKeyStringsAsPair(String filename, int elementsPerLine)
	{
		try
		{
			String rawKeyStrings = Resources.toString(FlickrUtils.class.getResource(filename), Charsets.UTF_8);
			List<String[]> nameStrings = new ArrayList<String[]>();
			for( String keyString : rawKeyStrings.split("\n") )
			{
				keyString = keyString.trim();
				if( !keyString.startsWith("#") && keyString.length() > 0 )
				{
					String[] line = new String[elementsPerLine];
					for( int i = 0; i < elementsPerLine; ++i )
					{
						if( i == elementsPerLine - 1 )
						{
							line[i] = keyString;
						}
						else
						{
							int spaceSeparator = keyString.indexOf(' ');
							// space can't be first or last, but the first space
							// is required
							if( spaceSeparator >= 1 )
							{
								line[i] = keyString.substring(0, spaceSeparator);
							}
							keyString = keyString.substring(spaceSeparator + 1);
						}
					}
					nameStrings.add(line);
				}
			}
			return nameStrings;
		}
		catch( IOException ioe )
		{
			throw Throwables.propagate(ioe);
		}
	}

	public static List<NameValue> getNameValuesFromFile(String filename, int elementsPerLine, boolean valueBeforeName)
	{
		// Only catering for 2 or 3 elements per line so far
		if( !(elementsPerLine == 2 || elementsPerLine == 3) )
		{
			throw new RuntimeException(elementsPerLine + ", not supported");
		}

		try
		{
			String rawKeyStrings = Resources.toString(FlickrUtils.class.getResource(filename), Charsets.UTF_8);
			List<NameValue> nameValues = new ArrayList<NameValue>();
			for( String keyString : rawKeyStrings.split("\n") )
			{
				keyString = keyString.trim();
				if( !keyString.startsWith("#") && keyString.length() > 0 )
				{
					String[] line = new String[elementsPerLine];
					for( int i = 0; i < elementsPerLine; ++i )
					{
						if( i == elementsPerLine - 1 )
						{
							line[i] = keyString;
						}
						else
						{
							int spaceSeparator = keyString.indexOf(' ');
							// space can't be first or last, but the first space
							// is required
							if( spaceSeparator >= 1 )
							{
								line[i] = keyString.substring(0, spaceSeparator);
							}
							keyString = keyString.substring(spaceSeparator + 1);
						}
					}
					NameValue aNameValue = null;
					String name = valueBeforeName ? line[1] : line[0];
					String value = valueBeforeName ? line[0] : line[1];
					if( elementsPerLine == 2 )
					{
						aNameValue = new NameValue(name, value);
					}
					else if( elementsPerLine == 3 )
					{
						aNameValue = new NameValueExtra(name, value, line[2]);
					}

					nameValues.add(aNameValue);
				}
			}
			return nameValues;
		}
		catch( IOException ioe )
		{
			throw Throwables.propagate(ioe);
		}
	}

	public static String formatToSize(String srcStr)
	{
		if( !Check.isEmpty(srcStr) )
		{
			srcStr = formatToSetSize(srcStr, DESCR_DISPLAY_LEN);
			// replace any newlines in the text with HTML break tag
			String[] splitOnLines = srcStr.split("\n");
			if( splitOnLines.length < 10 )
			{
				// turn any double-newlines into singles
				srcStr = srcStr.replace("\n\n", "\n");
				// turn the singles into break tags
				srcStr = srcStr.replace("\n", "<br/>");
			}
		}
		return srcStr;
	}

	public static String formatToSetSize(String srcStr, final int len)
	{
		// if the description is too long, truncate ...
		if( srcStr.length() > len )
		{
			// ... but rather than break in the middle of a word, look for the
			// a space in the last 3/4 of the original string
			int lastSpace = srcStr.lastIndexOf(' ', len);
			if( lastSpace > 3 * (len / 4) )
			{
				srcStr = srcStr.substring(0, lastSpace) + "...";
			}
			else
			{
				// some improbably long uninterrupted text stream, just truncate
				srcStr = srcStr.substring(0, len) + "...";
			}
		}
		return srcStr;
	}

	public static String describePhotoSize(Photo photo)
	{
		String hw = null;
		Size photoSize = photo.getOriginalSize();
		if( photoSize != null )
		{
			int h = photoSize.getHeight();
			int w = photoSize.getWidth();
			if( h > 0 && w > 0 ) // meaningful data?
			{
				hw = "" + h + " x " + w;
			}
		}
		if( Check.isEmpty(hw) )
		{
			// photo size sometimes returned as a Size field, otherwise possibly
			// as
			// separate originalHeight, originalWidth fields
			int h = photo.getOriginalHeight();
			int w = photo.getOriginalWidth();
			if( h > 0 && w > 0 ) // meaningful data?
			{
				hw = "" + h + " x " + w;
			}
		}
		return hw;
	}
}
