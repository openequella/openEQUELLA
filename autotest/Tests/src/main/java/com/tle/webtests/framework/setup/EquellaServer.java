package com.tle.webtests.framework.setup;

import java.io.File;

public class EquellaServer
{
	private String url;
	private String password;
	private File configFolder;

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public File getConfigFolder()
	{
		return configFolder;
	}

	public void setConfigFolder(File configFolder)
	{
		this.configFolder = configFolder;
	}

}
