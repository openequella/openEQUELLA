package com.dytech.edge.common;

import java.io.Serializable;

/**
 * Bean for holding information about a file.
 */
public class FileInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final long length;
	private final String filename;
	private final String md5CheckSum;

	public FileInfo(final long length, final String filename)
	{
		this(length, filename, null);
	}

	public FileInfo(final long length, final String filename, final String md5CheckSum)
	{
		this.length = length;
		this.filename = filename;
		this.md5CheckSum = md5CheckSum;
	}

	/**
	 * @return Returns the length.
	 */
	public long getLength()
	{
		return length;
	}

	/**
	 * @return Returns the filename (which may be different to the filename
	 *         requested)
	 */
	public String getFilename()
	{
		return filename;
	}

	/**
	 * @return Returns the md5CheckSum
	 */
	public String getMd5CheckSum()
	{
		return md5CheckSum;
	}
}
