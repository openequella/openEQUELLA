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

package com.tle.core.mimetypes.institution;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.xml.service.XmlService;

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
