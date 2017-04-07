package com.tle.web.api.item.interfaces.beans;

/**
 * Equivalent of the staging folder, hence the UUID.
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
public class RootFolderBean extends FolderBean
{
	@SuppressWarnings("hiding")
	public static final String TYPE = "root";

	private String uuid;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}
}
