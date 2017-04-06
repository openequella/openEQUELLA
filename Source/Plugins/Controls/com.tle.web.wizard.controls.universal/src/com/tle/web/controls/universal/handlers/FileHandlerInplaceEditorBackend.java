package com.tle.web.controls.universal.handlers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.filesystem.FileHandle;
import com.tle.common.URLUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.inplaceeditor.InPlaceEditorServerBackend;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.UrlService;

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
	private UrlService urlService;

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
		return urlService.institutionalise("file/" + stagingId + "/$/" + URLUtils.urlEncode(filename, false));
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
			throw new RuntimeException(CurrentLocale.get(
				"com.tle.web.wizard.controls.universal.handlers.file.inplacebackend.error.write", filename), ex);
		}
	}
}
