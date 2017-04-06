package com.tle.web.scorm.migration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.core.mimetypes.institution.MimeEntryConverter;
import com.tle.core.xstream.XmlService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AddScormMimeTypeXmlMigration extends XmlMigrator
{
	@Inject
	private XmlService xmlService;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		SubTemporaryFile mimeFolder = MimeEntryConverter.getMimeFolder(staging);
		MimeEntry mimeEntry = new MimeEntry();
		mimeEntry.setDescription("SCORM Package");
		mimeEntry.setType("equella/scorm-package");
		mimeEntry.setAttribute("enabledViewers", "[\"downloadIms\"]");
		mimeEntry.setAttribute("PluginIconPath", "icons/ims.png");

		String filename = MimeEntryConverter.getFilenameForEntry(mimeEntry);
		if( !fileExists(mimeFolder, filename) )
		{
			xmlHelper.writeFile(mimeFolder, filename, xmlService.serialiseToXml(mimeEntry));
		}
	}
}
