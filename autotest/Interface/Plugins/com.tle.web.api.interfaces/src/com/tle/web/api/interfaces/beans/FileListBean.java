package com.tle.web.api.interfaces.beans;

import java.util.List;


public class FileListBean extends AbstractExtendableBean
{
	private List<BlobBean> files;

	public List<BlobBean> getFiles()
	{
		return files;
	}

	public void setFiles(List<BlobBean> files)
	{
		this.files = files;
	}
}
