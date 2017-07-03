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

package com.tle.web.controls.universal.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.URLUtils;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.inplaceeditor.InPlaceEditorServerBackend;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.FileSystemService;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class FileHandlerInplaceEditorBackend implements InPlaceEditorServerBackend
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private InstitutionService institutionService;

	@Override
	public String getDownloadUrl(String wizardStagingId, int itemVersion, String stagingId, String filename)
	{
		// if the file doesn't exist in staging then copy it across
		// NOTE: this is completely different staging to the Wizard staging
		// (which has hijacked the itemUuid parameter)
		final FileHandle stagingFile = new StagingFile(stagingId);
		if( !fileSystemService.fileExists(stagingFile, filename) )
		{
			fileSystemService.copy(new StagingFile(wizardStagingId), filename, stagingFile, filename);
		}
		return institutionService.institutionalise("file/" + stagingId + "/$/" + URLUtils.urlEncode(filename, false));
	}

	@Override
	public void write(String stagingId, String filename, boolean append, byte[] upload)
	{
		try
		{
			final FileHandle stagingFile = new StagingFile(stagingId);
			fileSystemService.write(stagingFile, filename, new ByteArrayInputStream(upload), append);
		}
		catch( IOException ex )
		{
			throw new RuntimeException(CurrentLocale
				.get("com.tle.web.wizard.controls.universal.handlers.file.inplacebackend.error.write", filename), ex);
		}
	}
}
