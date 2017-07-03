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
import org.apache.tika.sax.WriteOutContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;

import com.google.common.base.Throwables;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.guice.Bind;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class PdfExtracter extends AbstractTextExtracterExtension
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PdfExtracter.class);

	@Override
	public boolean isSupportedByDefault(MimeEntry mimeEntry)
	{
		return mimeEntry.getType().equals("application/pdf");
	}

	@Override
	public void extractText(String mimeType, InputStream input, StringBuilder outputText, int maxSize)
		throws IOException
	{
		WriteOutContentHandler wrapped = new WriteOutContentHandler(maxSize);
		ContentHandler handler = new BodyContentHandler(wrapped);
		try
		{
			Metadata meta = new Metadata();
			Parser parser = new AutoDetectParser(new TikaConfig(getClass().getClassLoader()));
			parser.parse(input, handler, meta, new ParseContext());

			appendText(handler, outputText, maxSize);
		}
		catch( Exception t )
		{
			if( wrapped.isWriteLimitReached(t) )
			{
				// keep going
				LOGGER.debug("PDF size limit reached.  Indexing truncated text");
				appendText(handler, outputText, maxSize);
				return;
			}
			throw Throwables.propagate(t);
		}
	}

	private void appendText(ContentHandler handler, StringBuilder outputText, int maxSize)
	{
		String pdfSummary = handler.toString();

		if( pdfSummary.length() > maxSize )
		{
			pdfSummary = pdfSummary.substring(0, maxSize);
		}
		outputText.append(pdfSummary);

		if( LOGGER.isTraceEnabled() )
		{
			LOGGER.trace("PDF Summary:" + pdfSummary);
		}
	}

	@Override
	public boolean isMimeTypeSupported(String mimeType)
	{
		return mimeType.toLowerCase().contains("pdf");
	}
}
