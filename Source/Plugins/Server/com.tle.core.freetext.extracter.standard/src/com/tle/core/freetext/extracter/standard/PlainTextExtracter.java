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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.guice.Bind;

/**
 * @author aholland
 */
@Bind
@Singleton
public class PlainTextExtracter extends AbstractTextExtracterExtension
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MsExcelExtracter.class);

	@Override
	public boolean isSupportedByDefault(MimeEntry mimeEntry)
	{
		return mimeEntry.getType().equals("text/plain"); //$NON-NLS-1$
	}

	@Override
	public void extractText(String mimeType, InputStream input, StringBuilder outputText, int maxSize)
		throws IOException
	{
		int done = 0;
		byte[] filebytes = new byte[maxSize];
		while( done < maxSize )
		{
			int amount = input.read(filebytes, done, maxSize - done);
			if( amount == -1 )
			{
				break;
			}
			done += amount;
		}
		String s = new String(filebytes, 0, done, "UTF-8");
		outputText.append(s);
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Text Summary:" + s); //$NON-NLS-1$
		}
	}

	@Override
	public boolean isMimeTypeSupported(String mimeType)
	{
		return mimeType.toLowerCase().startsWith("text"); //$NON-NLS-1$
	}
}
