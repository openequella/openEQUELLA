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

package com.tle.core.institution.convert;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.services.FileSystemService;

public abstract class XmlMigrator extends Migrator
{
	@Inject
	protected FileSystemService fileSystemService;
	@Inject
	protected XmlHelper xmlHelper;

	protected boolean fileExists(TemporaryFileHandle staging, String filename)
	{
		return fileSystemService.fileExists(staging, filename);
	}

	/**
	 * @param folder
	 * @return true if the format file exists and the format is set to bucketed
	 */
	protected boolean isBucketed(final SubTemporaryFile folder)
	{
		if( fileSystemService
			.fileExists(new SubTemporaryFile(folder, InstitutionImportExportConstants.EXPORT_FORMAT_FILE)) )
		{
			PropBagEx ff = xmlHelper.readToPropBagEx(folder, InstitutionImportExportConstants.EXPORT_FORMAT_FILE);
			return Boolean.valueOf(ff.getNode("bucketed")); //$NON-NLS-1$
		}
		return false;
	}
}
