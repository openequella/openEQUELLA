package com.tle.core.mimetypes.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.institution.MimeEntryConverter;
import com.tle.core.xml.service.XmlService;

@Bind
@Singleton
public class AddMoreImageMimeTypeXmlMigration extends XmlMigrator
{
	public static final List<String> imageMimeList = Arrays.asList("arw", "x3f");

	@Inject
	private XmlService xmlService;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		for( String mime : imageMimeList )
		{
			SubTemporaryFile mimeFolder = MimeEntryConverter.getMimeFolder(staging);
			MimeEntry mimeEntry = new MimeEntry();
			mimeEntry.setDescription("Image");
			mimeEntry.setType("image/" + mime);
			ArrayList<String> extensions = new ArrayList<String>();
			extensions.add(mime);
			mimeEntry.setExtensions(extensions);
			mimeEntry.setAttribute(MimeTypeConstants.KEY_DEFAULT_VIEWERID, MimeTypeConstants.VAL_DEFAULT_VIEWERID);
			mimeEntry.setAttribute(MimeTypeConstants.KEY_ENABLED_VIEWERS, "['livNavTreeViewer', 'toimg']");
			mimeEntry.setAttribute(MimeTypeConstants.KEY_ICON_PLUGINICON, "icons/image.png");

			String filename = MimeEntryConverter.getFilenameForEntry(mimeEntry);
			if( !fileExists(mimeFolder, filename) )
			{
				xmlHelper.writeFile(mimeFolder, filename, xmlService.serialiseToXml(mimeEntry));
			}
		}
	}
}
