/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.lti.migration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.mimetypes.institution.MimeEntryConverter;
import com.tle.core.xml.service.XmlService;
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
	 * @see com.tle.core.institution.convert.Migrator#execute(com.tle.common.filesystem.handle.TemporaryFileHandle,
	 *      com.tle.core.institution.convert.InstitutionInfo,
	 *      com.tle.core.institution.convert.ConverterParams)
	 */
	@Override
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
