package com.tle.web.echo.migration;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.sf.json.JSONArray;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.mimetypes.institution.MimeEntryConverter;
import com.tle.core.xstream.XmlService;
import com.tle.web.echo.EchoUtils;
import com.tle.web.viewurl.ResourceViewerConfig;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AddEchoMimeTypeXmlMigration extends XmlMigrator
{
	@Inject
	private XmlService xmlService;
	@Inject
	private MimeTypeService mimeService;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		SubTemporaryFile mimeFolder = MimeEntryConverter.getMimeFolder(staging);
		MimeEntry mimeEntry = new MimeEntry();
		mimeEntry.setType(EchoUtils.MIME_TYPE);
		mimeEntry.setDescription(EchoUtils.MIME_DESC);
		mimeEntry.setAttribute(MimeTypeConstants.KEY_DEFAULT_VIEWERID, "echoCenterViewer");
		mimeEntry.setAttribute(MimeTypeConstants.KEY_ENABLED_VIEWERS, JSONArray.fromObject(EchoUtils.VIEWERS)
			.toString());
		mimeEntry.setAttribute(MimeTypeConstants.KEY_DISABLE_FILEVIEWER, "true");
		mimeEntry.setAttribute(MimeTypeConstants.KEY_ICON_PLUGINICON, EchoUtils.MIME_ICON_PATH);

		ResourceViewerConfig rvc = new ResourceViewerConfig();
		rvc.setThickbox(false);
		rvc.setWidth("800");
		rvc.setHeight("600");
		rvc.setOpenInNewWindow(true);

		for( String viewer : EchoUtils.VIEWERS )
		{
			mimeService.setBeanAttribute(mimeEntry, "viewerConfig-" + viewer, rvc);
		}

		String filename = MimeEntryConverter.getFilenameForEntry(mimeEntry);
		if( !fileExists(mimeFolder, filename) )
		{
			xmlHelper.writeFile(mimeFolder, filename, xmlService.serialiseToXml(mimeEntry));
		}
	}
}