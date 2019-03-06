package com.tle.web.api.item.interfaces.beans;

import com.tle.web.api.item.interfaces.beans.GenericFileBean;

@SuppressWarnings("nls")
public class FileBean extends GenericFileBean
{
	@SuppressWarnings("hiding")
	public static final String TYPE = "file";

	private long size;

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}
}
