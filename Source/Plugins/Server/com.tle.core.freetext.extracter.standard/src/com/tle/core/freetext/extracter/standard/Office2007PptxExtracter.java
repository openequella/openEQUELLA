package com.tle.core.freetext.extracter.standard;

import javax.inject.Singleton;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.guice.Bind;

@Bind
@Singleton
@SuppressWarnings("nls")
public class Office2007PptxExtracter extends AbstractOffice2007Extracter
{
	@Override
	public boolean isSupportedByDefault(MimeEntry mimeEntry)
	{
		return mimeEntry.getType().startsWith("application/vnd.openxmlformats-officedocument.presentationml.");
	}

	@Override
	public String getFileToIndex()
	{
		return "ppt/slides/slide";
	}

	@Override
	public String getNameOfElementToIndex()
	{
		return "a:t";
	}

	@Override
	public boolean isMimeTypeSupported(String mimeType)
	{
		return mimeType.toLowerCase().contains("presentationml");
	}

	@Override
	public boolean multipleFiles()
	{
		return true;
	}
}
