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

import javax.inject.Singleton;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.guice.Bind;

/**
 * @author aholland
 */
@Bind
@Singleton
public class MsWordExtracter extends AbstractTextExtracterExtension
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MsWordExtracter.class);

	@Override
	public boolean isSupportedByDefault(MimeEntry mimeEntry)
	{
		String mimeType = mimeEntry.getType();
		return mimeType.startsWith("application/vnd.ms-word.") //$NON-NLS-1$
			|| mimeType.equals("application/msword"); //$NON-NLS-1$
	}

	@Override
	public void extractText(String mimeType, InputStream input, StringBuilder outputText, int maxSize)
		throws IOException
	{
		try
		{
			Metadata meta = new Metadata();
			ContentHandler handler = new BodyContentHandler();
			Parser parser = new AutoDetectParser(new TikaConfig(getClass().getClassLoader()));
			parser.parse(input, handler, meta, new ParseContext());

			String content = handler.toString();

			if( content.length() > maxSize )
			{
				content = content.substring(0, maxSize);
			}
			outputText.append(content);
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("Word Summary:" + content); //$NON-NLS-1$
			}
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isMimeTypeSupported(String mimeType)
	{
		return mimeType.toLowerCase().contains("msword") || mimeType.toLowerCase().contains("ms-word"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
