package com.tle.web.lti.migration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.externaltools.constants.ExternalToolConstants;
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

/**
 * @author larry
 */
@Bind
@Singleton
public class AddLtiMimeTypeXmlMigration extends XmlMigrator
{
	@Inject
	private XmlService xmlService;
	@Inject
	private MimeTypeService mimeService;

	/**
	 * @see com.tle.core.institution.migration.Migrator#execute(com.tle.core.filesystem.TemporaryFileHandle,
	 *      com.tle.core.institution.convert.InstitutionInfo,
	 *      com.tle.core.institution.convert.ConverterParams)
	 */
	@Override
	@SuppressWarnings("nls")
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		SubTemporaryFile mimeFolder = MimeEntryConverter.getMimeFolder(staging);
		MimeEntry mimeEntry = new MimeEntry();
		mimeEntry.setType(ExternalToolConstants.MIME_TYPE);
		mimeEntry.setAttribute(MimeTypeConstants.KEY_DEFAULT_VIEWERID, ExternalToolConstants.VIEWER_ID);
		// Only one viewer ...?
		mimeEntry.setAttribute(MimeTypeConstants.KEY_ENABLED_VIEWERS, "[\"" + ExternalToolConstants.VIEWER_ID + "\"]");
		mimeEntry.setAttribute(MimeTypeConstants.KEY_DISABLE_FILEVIEWER, "true");
		mimeEntry.setAttribute(MimeTypeConstants.KEY_ICON_PLUGINICON, ExternalToolConstants.MIME_ICON_PATH);

		ResourceViewerConfig rvc = new ResourceViewerConfig();
		rvc.setThickbox(false);
		rvc.setWidth("800");
		rvc.setHeight("600");
		rvc.setOpenInNewWindow(true);

		mimeService.setBeanAttribute(mimeEntry, "viewerConfig-" + ExternalToolConstants.VIEWER_ID, rvc);

		String filename = MimeEntryConverter.getFilenameForEntry(mimeEntry);
		if( !fileExists(mimeFolder, filename) )
		{
			xmlHelper.writeFile(mimeFolder, filename, xmlService.serialiseToXml(mimeEntry));
		}
	}

}
