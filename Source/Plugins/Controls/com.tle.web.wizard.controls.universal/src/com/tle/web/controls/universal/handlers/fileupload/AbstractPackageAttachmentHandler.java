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

package com.tle.web.controls.universal.handlers.fileupload;

import javax.inject.Inject;

import com.google.common.base.Throwables;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.services.FileSystemService;
import com.tle.web.controls.universal.handlers.FileUploadHandler;
import com.tle.web.sections.SectionInfo;
import com.tle.web.wizard.PackageTreeBuilder.PackageInfo;
import com.tle.web.wizard.impl.WebRepository;

/**
 * @author Aaron
 */
public abstract class AbstractPackageAttachmentHandler implements PackageAttachmentHandler
{
	@Inject
	private FileSystemService fileSystem;

	/**
	 * @param repo
	 * @param file
	 * @return file path to the manifest folder
	 */
	protected String extractPackage(WebRepository repo, UploadedFile file)
	{
		try
		{
			if( fileSystem.isArchive(new StagingFile(repo.getStagingid()), file.getFilepath()) )
			{
				final String unzippedPath = FileUploadHandler.getUploadFilepath("_pkg");
				repo.unzipFile(file.getFilepath(), unzippedPath, false);
				file.setExtractedPath(unzippedPath);
				return unzippedPath;
			}
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
		return file.getFilepath();
	}

	protected PackageInfo readPackage(SectionInfo info, WebRepository repo, String packageExtractedFolder)
	{
		return repo.readPackageInfo(info, packageExtractedFolder);
	}

	protected void move(StagingFile staging, String filepath, String destpath)
	{
		move(staging, filepath, staging, destpath);
	}

	protected void move(StagingFile staging, String filepath, StagingFile stagingDest, String destpath)
	{
		if( !fileSystem.move(staging, filepath, stagingDest, destpath) )
		{
			throw new RuntimeException("Couldn't move file from '" + filepath + "' to '" + destpath + "'");
		}
	}
}
