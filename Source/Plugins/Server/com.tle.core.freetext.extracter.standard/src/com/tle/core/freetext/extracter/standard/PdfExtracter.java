package com.tle.core.freetext.extracter.standard;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;
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
	private static final Logger LOGGER = Logger.getLogger(PdfExtracter.class);

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
