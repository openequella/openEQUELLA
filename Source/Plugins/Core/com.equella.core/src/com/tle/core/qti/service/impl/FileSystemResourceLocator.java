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

package com.tle.core.qti.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

import com.google.common.base.Throwables;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.PathUtils;
import com.tle.core.services.FileSystemService;

/**
 * @author Aaron
 */
public class FileSystemResourceLocator implements ResourceLocator
{
	private final FileSystemService fileSystemService;
	private final FileHandle handle;
	private final String basePath;

	public FileSystemResourceLocator(FileSystemService fileSystemService, FileHandle handle, String basePath)
	{
		this.fileSystemService = fileSystemService;
		this.handle = handle;
		this.basePath = basePath;
	}

	@Override
	public InputStream findResource(URI systemId)
	{
		try
		{
			String path = systemId.toString();
			if( !path.startsWith(basePath) )
			{
				path = PathUtils.filePath(basePath, path);
			}
			return fileSystemService.read(handle, path);
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}
}
