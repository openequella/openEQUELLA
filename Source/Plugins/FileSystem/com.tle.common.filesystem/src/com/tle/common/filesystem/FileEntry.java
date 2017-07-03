/*
 * Created on Jul 13, 2005
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
