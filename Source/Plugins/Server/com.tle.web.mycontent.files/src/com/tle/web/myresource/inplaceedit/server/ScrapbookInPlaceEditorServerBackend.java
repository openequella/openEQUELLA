package com.tle.web.myresource.inplaceedit.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.ItemId;
import com.tle.common.URLUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.inplaceeditor.InPlaceEditorServerBackend;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.UrlService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ScrapbookInPlaceEditorServerBackend implements InPlaceEditorServerBackend
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private UrlService urlService;

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
			final ItemFile itemFile = new ItemFile(itemId);
			fileSystemService.copy(itemFile, filename, stagingFile, filename);
		}
		return urlService.institutionalise("file/" + stagingId + "/$/" + URLUtils.urlEncode(filename, false));
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
			throw new RuntimeException(CurrentLocale.get(
				"com.tle.web.wizard.controls.universal.handlers.file.inplacebackend.error.write", filename), ex);
		}
	}
}
