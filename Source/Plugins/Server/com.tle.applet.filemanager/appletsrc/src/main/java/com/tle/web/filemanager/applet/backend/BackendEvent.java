package com.tle.web.filemanager.applet.backend;

import com.tle.web.filemanager.common.FileInfo;

public class BackendEvent
{
	private FileInfo info;
	private String newName;
	private FileInfo destinationFile;

	public BackendEvent(FileInfo info)
	{
		this.info = info;
	}

	public BackendEvent(FileInfo info, String newName)
	{
		this(info);
		this.newName = newName;
	}

	public BackendEvent(FileInfo source, FileInfo destinationFile)
	{
		this(source);
		this.destinationFile = destinationFile;
	}

	public FileInfo getInfo()
	{
		return info;
	}

	public String getNewName()
	{
		return newName;
	}

	public FileInfo getDestinationFile()
	{
		return destinationFile;
	}
}
