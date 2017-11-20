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

package com.tle.core.filesystem.convert;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.beans.Institution;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileCallback;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.filesystem.AbstractTemplateFile;
import com.tle.core.filesystem.CustomisationFile;
import com.tle.core.filesystem.InstitutionFile;
import com.tle.core.filesystem.LanguagesFile;
import com.tle.core.filesystem.PublicFile;
import com.tle.core.filesystem.SystemFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.DefaultMessageCallback;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;

@Bind
@Singleton
@SuppressWarnings("nls")
public class FilestoreConverter<T> extends AbstractConverter<T>
{
	@Override
	public void importIt(TemporaryFileHandle staging, Institution institution, ConverterParams params, String cid)
		throws IOException
	{
		if( cid.equals(ConverterId.FILES.name()) )
		{
			doImport(staging, institution, params);
		}
		else if( cid.equals(ConverterId.CLEANUPFILES.name()) )
		{
			doFileDelete(staging, params);
		}
	}

	public boolean doFileDelete(FileHandle handle, ConverterParams params)
	{
		final DefaultMessageCallback message = new DefaultMessageCallback(
			"institutions.converter.filestore.deletecalculate");
		params.setMessageCallback(message);
		message.setTotal(fileSystemService.countFiles(handle, ""));
		message.setKey("institutions.converter.filestore.deletemsg");
		return fileSystemService.removeFile(handle, "", new FileCallback()
		{
			@Override
			public void fileProcessed(File file, File file2)
			{
				message.incrementCurrent();
			}
		});
	}

	@Override
	public void exportIt(TemporaryFileHandle staging, Institution institution, ConverterParams callback, String cid)
		throws IOException
	{
		if( cid.equals(ConverterId.FILES.name()) )
		{
			doExport(staging, institution, callback);
		}
	}

	@Override
	public void deleteIt(TemporaryFileHandle staging, Institution institution, ConverterParams params, String cid)
	{
		if( cid.equals(ConverterId.FILES.name()) )
		{
			FileHandle handle = new InstitutionFile(institution);
			if( !doFileDelete(handle, params) )
			{
				throw new RuntimeApplicationException("Could not delete filestore for unknown reasons");
			}
		}
		else if( cid.equals(ConverterId.CLEANUPFILES.name()) )
		{
			doFileDelete(staging, params);
		}
	}

	@Override
	public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params)
	{
		if( type == ConvertType.DELETE )
		{
			tasks.addAfter(getStandardTask(ConverterId.FILES));
		}
		else
		{
			tasks.add(getStandardTask(ConverterId.FILES));
			tasks.add(getStandardTask(ConverterId.CLEANUPFILES));
		}
	}

	@Override
	public ConverterId getConverterId()
	{
		return ConverterId.FILES;
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		copyToStagingIfExists(new GlobalActWizardFile(), staging, "filestore/GlobalActWizard");
		copyToStagingIfExists(new CustomisationFile(), staging, "filestore/Custom2");
		copyToStagingIfExists(new SystemFile(), staging, "filestore/System");
		copyToStagingIfExists(new LanguagesFile(), staging, "filestore/Languages");
		copyToStagingIfExists(new PublicFile("htmleditorstyles"), staging, "filestore/Public");
	}

	private void copyToStagingIfExists(FileHandle handle, TemporaryFileHandle staging, String dest) throws IOException
	{
		if( fileSystemService.fileExists(handle) )
		{
			fileSystemService.copyToStaging(handle, "", staging, dest, false);
		}
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		fileSystemService.commitFiles(staging, "filestore/GlobalActWizard", new GlobalActWizardFile());
		fileSystemService.commitFiles(staging, "filestore/Custom2", new CustomisationFile());
		fileSystemService.commitFiles(staging, "filestore/System", new SystemFile());
		fileSystemService.commitFiles(staging, "filestore/Languages", new LanguagesFile());
		fileSystemService.commitFiles(staging, "filestore/Public", new PublicFile("htmleditorstyles"));
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		throw new Error();
	}

	public class GlobalActWizardFile extends AbstractTemplateFile
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected String createAbsolutePath()
		{
			return PathUtils.filePath(super.createAbsolutePath(), "/Global/ActivityWizard");
		}
	}
}