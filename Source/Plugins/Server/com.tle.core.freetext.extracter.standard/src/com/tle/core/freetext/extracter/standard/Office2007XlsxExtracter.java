package com.tle.core.freetext.extracter.standard;

import javax.inject.Singleton;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.guice.Bind;

@Bind
@Singleton
@SuppressWarnings("nls")
public class Office2007XlsxExtracter extends AbstractOffice2007Extracter
{
	@Override
	public boolean isSupportedByDefault(MimeEntry mimeEntry)
	{
		return mimeEntry.getType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	}

	@Override
	public String getFileToIndex()
	{
		return "xl/sharedStrings.xml";
	}

	@Override
	public String getNameOfElementToIndex()
	{
		return "t";
	}

	@Override
	public boolean isMimeTypeSupported(String mimeType)
	{
		return mimeType.toLowerCase().contains("spreadsheetml");
	}

	@Override
	public boolean multipleFiles()
	{
		return false;
	}
}
