package com.tle.core.freetext.convert;

import java.io.IOException;

import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;

@Bind
@Singleton
public class SynchronizeIndexConverter extends AbstractConverter<Object>
{
	@Override
	public void doDelete(Institution institution, ConverterParams params)
	{
		// nothing
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		// all
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		// freeTextService.indexAll();
	}

	@Override
	public ConverterId getConverterId()
	{
		return null;
	}

	@Override
	public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params)
	{
		if( !params.hasFlag(ConverterParams.NO_ITEMS) && (type == ConvertType.IMPORT || type == ConvertType.CLONE) )
		{
			tasks.addAfter(getStandardTask(ConverterId.SYNCHRONIZEITEMS));
		}
	}
}
