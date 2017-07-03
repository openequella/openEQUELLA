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
import com.tle.freetext.htmlfilter.HTMLFilter;

/**
 * @author aholland
 */
@Bind
@Singleton
public class HtmlExtracter extends AbstractTextExtracterExtension
{
	private static final Logger LOGGER = LoggerFactory.getLogger(HtmlExtracter.class);

	@Override
	public boolean isSupportedByDefault(MimeEntry mimeEntry)
	{
		return mimeEntry.getType().equals("text/html"); //$NON-NLS-1$
	}

	@Override
	public void extractText(String mimeType, InputStream input, StringBuilder outputText, int maxSize)
		throws IOException
	{
		String summary = new HTMLFilter(input).getSummary(maxSize);
		outputText.append(summary);
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("HTML Summary:" + summary); //$NON-NLS-1$
		}
	}

	@Override
	public boolean isMimeTypeSupported(String mimeType)
	{
		return mimeType.toLowerCase().contains("html"); //$NON-NLS-1$
	}
}
