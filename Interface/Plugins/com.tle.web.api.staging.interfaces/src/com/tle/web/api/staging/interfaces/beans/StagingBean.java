package com.tle.web.api.staging.interfaces.beans;

import java.util.List;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import com.tle.web.api.interfaces.beans.BlobBean;

public class StagingBean extends AbstractExtendableBean
{
	private String uuid;
	private String directUrl;
	private List<BlobBean> files;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getDirectUrl()
	{
		return directUrl;
	}

	public void setDirectUrl(String directUrl)
	{
		this.directUrl = directUrl;
	}

	public List<BlobBean> getFiles()
	{
		return files;
	}

	public void setFiles(List<BlobBean> files)
	{
		this.files = files;
	}
}