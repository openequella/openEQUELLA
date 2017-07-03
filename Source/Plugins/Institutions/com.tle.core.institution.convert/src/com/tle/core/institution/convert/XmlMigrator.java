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
