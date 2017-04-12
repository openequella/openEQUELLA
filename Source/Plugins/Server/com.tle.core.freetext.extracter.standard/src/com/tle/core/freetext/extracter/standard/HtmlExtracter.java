package com.tle.core.freetext.extracter.standard;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Singleton;

import org.apache.log4j.Logger;

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
	private static final Logger LOGGER = Logger.getLogger(HtmlExtracter.class);

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
