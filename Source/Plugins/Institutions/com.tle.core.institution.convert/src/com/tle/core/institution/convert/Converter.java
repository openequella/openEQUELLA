package com.tle.core.institution.convert;

import java.io.IOException;

import com.tle.beans.Institution;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;

public interface Converter
{
	/**
	 * Use a string value
	 */
	@Deprecated
	public enum ConverterId
	{
		ITEMS, USERS, ENTITIES, CONFIGURATION, FILES, GROUPS, HIERARCHY, TAXONOMIES, PREFERENCES, COURSES, LANGUAGES,
		ITEMSATTACHMENTS, ACLS, AUDITLOGS, MIGRATION, SYNCHRONIZEITEMS, CLEANUPFILES, ZIPFILES
	}

	void doInTransaction(Runnable runnable);

	void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params);

	void deleteIt(TemporaryFileHandle staging, Institution institution, ConverterParams params, String task);

	void clone(TemporaryFileHandle staging, Institution newInstitution, ConverterParams params, String task)
		throws IOException;

	void exportIt(TemporaryFileHandle staging, Institution institution, ConverterParams params, String cid)
		throws IOException;

	void importIt(TemporaryFileHandle staging, Institution newInstitution, ConverterParams params, String cid)
		throws IOException;
}
