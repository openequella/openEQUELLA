package com.tle.core.qti.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

import com.google.common.base.Throwables;
import com.tle.beans.filesystem.FileHandle;
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
