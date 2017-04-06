package com.tle.core.mimetypes.institution;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.xstream.XmlService;

/**
 * See http://dev.equella.com/issues/5963
 * 
 * @author Aaron
 */
@Bind
@Singleton
public class RerunMimeMigrator extends MimeMigrator
{
	@Inject
	private XmlService xmlService;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		List<MimeEntry> defaultTypes = getDefaultMimeEntries();
		SubTemporaryFile mimeFolder = MimeEntryConverter.getMimeFolder(staging);

		for( MimeEntry mimeEntry : defaultTypes )
		{
			// Don't overwrite any existing one
			String filename = MimeEntryConverter.getFilenameForEntry(mimeEntry);
			if( !fileExists(mimeFolder, filename) )
			{
				xmlHelper.writeFile(mimeFolder, filename, xmlService.serialiseToXml(mimeEntry));
			}
		}
	}
}
