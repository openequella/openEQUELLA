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

package com.tle.web.qti.migration;

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
import com.tle.core.qti.QtiConstants;
import com.tle.core.xml.service.XmlService;

@Bind
@Singleton
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
