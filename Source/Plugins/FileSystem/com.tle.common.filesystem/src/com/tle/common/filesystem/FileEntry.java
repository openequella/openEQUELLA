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

package com.tle.common.filesystem;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A serialisable version of java.io.File.
 */
public class FileEntry implements Serializable
{
	private static final long serialVersionUID = 1L;
	private boolean folder;
	private List<FileEntry> files = new ArrayList<FileEntry>();
	private String name;
	private long length;

	public FileEntry(boolean folder)
	{
		this.folder = folder;
	}

	public FileEntry(File file)
	{
		this.name = file.getName();
		this.folder = file.isDirectory();
		this.length = (this.folder ? 0 : file.length());
	}

	public List<FileEntry> getFiles()
	{
		return files;
	}

	public void setFiles(List<FileEntry> files)
	{
		this.files = files;
	}

	public boolean isFolder()
	{
		return folder;
	}

	public void setFolder(boolean folder)
	{
		this.folder = folder;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public long getLength()
	{
		return length;
	}

	public void setLength(long length)
	{
		this.length = length;
	}

	public List<String> foldToPaths()
	{
		final List<String> paths = new ArrayList<String>();
		foldToPathsHelper(this, "", paths, false); //$NON-NLS-1$
		return paths;
	}

	@SuppressWarnings("nls")
	private void foldToPathsHelper(final FileEntry file, String basePath, List<String> paths, boolean includeThis)
	{
		String fileName = null;
		if( includeThis )
		{
			fileName = file.getName();
		}
		String filePath = basePath + (fileName != null ? fileName : "");
		if( file.isFolder() )
		{
			if( fileName != null )
			{
				filePath += '/';
			}
			for( FileEntry child : file.getFiles() )
			{
				foldToPathsHelper(child, filePath, paths, true);
			}
		}
		else
		{
			paths.add(filePath);
		}
	}
}
