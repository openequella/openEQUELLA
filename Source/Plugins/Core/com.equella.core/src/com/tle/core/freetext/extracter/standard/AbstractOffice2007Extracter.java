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

package com.tle.core.freetext.extracter.standard;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.google.common.base.Throwables;
import com.tle.core.util.archive.ArchiveEntry;
import com.tle.core.util.archive.ArchiveExtractor;
import com.tle.core.util.archive.ArchiveType;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public abstract class AbstractOffice2007Extracter extends AbstractTextExtracterExtension
{
	public abstract String getFileToIndex();

	public abstract boolean multipleFiles();

	public abstract String getNameOfElementToIndex();

	@Override
	public void extractText(String mimeType, InputStream input, StringBuilder outputText, int maxSize)
		throws IOException
	{
		try
		{
			ArchiveExtractor extractor = ArchiveType.ZIP.createExtractor(input);

			ArchiveEntry entry = extractor.getNextEntry();
			while( entry != null )
			{
				if( !entry.isDirectory() && entry.getName().startsWith(getFileToIndex()) )
				{
					gatherValuesForTagName(getNameOfElementToIndex(), new InputStreamReader(extractor.getStream()),
						outputText, maxSize);
					if( !multipleFiles() )
					{
						return;
					}
				}

				entry = extractor.getNextEntry();
			}
		}
		catch( XmlPullParserException e )
		{
			Throwables.propagate(e);
		}
	}

	private static void gatherValuesForTagName(String tagName, Reader xml, StringBuilder gatherer, int maxSize)
		throws XmlPullParserException, IOException
	{
		XmlPullParser parser = new MXParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(xml);

		int event = parser.getEventType();
		while( event != XmlPullParser.END_DOCUMENT )
		{
			if( event == XmlPullParser.START_TAG && parser.getName().equals(tagName) )
			{
				while( parser.next() == XmlPullParser.TEXT )
				{
					String s = parser.getText();
					if( s != null )
					{
						// Removal all tabs, newlines, returns, etc.. and trim
						// white space
						s = s.replaceAll("\\cM?\r?\r\n\t", "").trim();
						if( s.length() > 0 )
						{
							gatherer.append(s);
							gatherer.append(' ');
						}
					}
				}

				if( gatherer.length() >= maxSize )
				{
					return;
				}
			}
			event = parser.next();
		}
	}
}
