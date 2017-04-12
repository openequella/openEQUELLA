package com.tle.web.qti.migration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.institution.MimeEntryConverter;
import com.tle.core.qti.QtiConstants;
import com.tle.core.xstream.XmlService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AddQTIMimeTypeXmlMigration extends XmlMigrator
{
	public static final String TEST_MIME_TYPE_DESCRIPTION = "QTI quiz";

	@Inject
	private XmlService xmlService;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		SubTemporaryFile mimeFolder = MimeEntryConverter.getMimeFolder(staging);
		MimeEntry mimeEntry = new MimeEntry();
		mimeEntry.setType(QtiConstants.TEST_MIME_TYPE);
		mimeEntry.setDescription(TEST_MIME_TYPE_DESCRIPTION);
		mimeEntry.setAttribute(MimeTypeConstants.KEY_DEFAULT_VIEWERID, "qtiTestViewer");
		mimeEntry.setAttribute(MimeTypeConstants.KEY_ENABLED_VIEWERS, "[\"qtiTestViewer\"]");
		mimeEntry.setAttribute(MimeTypeConstants.KEY_DISABLE_FILEVIEWER, "true");
		mimeEntry.setAttribute(MimeTypeConstants.KEY_ICON_PLUGINICON, QtiConstants.MIME_ICON_PATH);

		String filename = MimeEntryConverter.getFilenameForEntry(mimeEntry);
		if( !fileExists(mimeFolder, filename) )
		{
			xmlHelper.writeFile(mimeFolder, filename, xmlService.serialiseToXml(mimeEntry));
		}
	}
}
