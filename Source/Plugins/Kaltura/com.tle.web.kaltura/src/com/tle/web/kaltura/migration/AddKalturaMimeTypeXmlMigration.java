package com.tle.web.kaltura.migration;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Maps;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.kaltura.KalturaUtils;
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
import com.tle.web.viewurl.ResourceViewerConfig;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AddKalturaMimeTypeXmlMigration extends XmlMigrator
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
		mimeEntry.setType(KalturaUtils.MIME_TYPE);
		mimeEntry.setDescription(KalturaUtils.MIME_DESC);
		mimeEntry.setAttribute(MimeTypeConstants.KEY_DEFAULT_VIEWERID, "kalturaViewer");
		mimeEntry.setAttribute(MimeTypeConstants.KEY_ENABLED_VIEWERS, "[\"kalturaViewer\"]");
		mimeEntry.setAttribute(MimeTypeConstants.KEY_DISABLE_FILEVIEWER, "true");

		ResourceViewerConfig rvc = new ResourceViewerConfig();
		rvc.setThickbox(true);
		rvc.setWidth("800");
		rvc.setHeight("600");
		rvc.setOpenInNewWindow(true);

		HashMap<String, Object> attrs = Maps.newHashMap();
		attrs.put("kalturaWidth", "800");
		attrs.put("kalturaHeight", "600");
		rvc.setAttr(attrs);

		mimeService.setBeanAttribute(mimeEntry, "viewerConfig-kalturaViewer", rvc);

		String filename = MimeEntryConverter.getFilenameForEntry(mimeEntry);
		if( !fileExists(mimeFolder, filename) )
		{
			xmlHelper.writeFile(mimeFolder, filename, xmlService.serialiseToXml(mimeEntry));
		}
	}
}
