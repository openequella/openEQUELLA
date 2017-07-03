/*
 * Created on 4/05/2006
 */
package com.tle.core.institution.convert;

import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;

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
