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

package com.tle.web.myresource.inplaceedit.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.ItemId;
import com.tle.common.URLUtils;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.inplaceeditor.InPlaceEditorServerBackend;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.services.FileSystemService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ScrapbookInPlaceEditorServerBackend implements InPlaceEditorServerBackend
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private ItemFileService itemFileService;

	/**
	 * @param itemUuid
	 * @param itemVersion Should always be 1 for scrapbook items
	 * @param stagingId
	 * @param filename
	 * @return
	 */
	@Override
	public String getDownloadUrl(String itemUuid, int itemVersion, String stagingId, String filename)
	{
		// if the file doesn't exist in staging then copy it across
		final FileHandle stagingFile = new StagingFile(stagingId);

		if( !fileSystemService.fileExists(stagingFile, filename) )
		{
			final ItemId itemId = new ItemId(itemUuid, itemVersion);
			final ItemFile itemFile = itemFileService.getItemFile(itemId, null);
			fileSystemService.copy(itemFile, filename, stagingFile, filename);
		}
		return institutionService.institutionalise("file/" + stagingId + "/$/" + URLUtils.urlEncode(filename, false));
	}

	@Override
	public void write(String stagingId, String filename, boolean append, byte[] upload)
	{
		try
		{
			FileHandle stagingFile = new StagingFile(stagingId);
			fileSystemService.write(stagingFile, filename, new ByteArrayInputStream(upload), append);
		}
		catch( IOException ex )
		{
			throw new RuntimeException(CurrentLocale
				.get("com.tle.web.wizard.controls.universal.handlers.file.inplacebackend.error.write", filename), ex);
		}
	}
}
