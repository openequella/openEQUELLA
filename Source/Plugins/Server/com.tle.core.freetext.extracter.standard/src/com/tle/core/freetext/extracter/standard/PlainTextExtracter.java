package com.tle.core.freetext.extracter.standard;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.edge.common.Constants;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.guice.Bind;

/**
 * @author aholland
 */
@Bind
@Singleton
public class PlainTextExtracter extends AbstractTextExtracterExtension
{
	private static final Logger LOGGER = Logger.getLogger(MsExcelExtracter.class);

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
		String s = new String(filebytes, 0, done, Constants.UTF8);
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
