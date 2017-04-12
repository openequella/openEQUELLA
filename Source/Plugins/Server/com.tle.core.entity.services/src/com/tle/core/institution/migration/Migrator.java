/*
 * Created on 4/05/2006
 */
package com.tle.core.institution.migration;

import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.services.InstitutionImportService.ConvertType;
import com.tle.core.services.impl.InstitutionImportServiceImpl.ConverterTasks;

/**
 * @author nread
 */
public abstract class Migrator
{
	public abstract void execute(final TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
		throws Exception;

	public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params)
	{
		// nothing
	}
}
