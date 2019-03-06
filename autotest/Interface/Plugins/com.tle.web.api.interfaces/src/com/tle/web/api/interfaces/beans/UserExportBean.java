package com.tle.web.api.interfaces.beans;

public class UserExportBean
{
	@SuppressWarnings("nls")
	private String exportVersion = "1.0";
	private String passwordHash;

	public String getExportVersion()
	{
		return exportVersion;
	}

	public void setExportVersion(String exportVersion)
	{
		this.exportVersion = exportVersion;
	}

	public String getPasswordHash()
	{
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash)
	{
		this.passwordHash = passwordHash;
	}
}
