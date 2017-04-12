package com.tle.core.freetext.extracter.standard;

import javax.inject.Singleton;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.guice.Bind;

/**
 * @author aholland
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class Office2007DocxExtracter extends AbstractOffice2007Extracter
{
	@Override
	public boolean isSupportedByDefault(MimeEntry mimeEntry)
	{
		return mimeEntry.getType().startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.");
	}

	@Override
	public String getFileToIndex()
	{
		return "word/document.xml";
	}

	@Override
	public String getNameOfElementToIndex()
	{
		return "w:t";
	}

	@Override
	public boolean isMimeTypeSupported(String mimeType)
	{
		return mimeType.toLowerCase().contains("wordprocessingml");
	}

	@Override
	public boolean multipleFiles()
	{
		return false;
	}
}
