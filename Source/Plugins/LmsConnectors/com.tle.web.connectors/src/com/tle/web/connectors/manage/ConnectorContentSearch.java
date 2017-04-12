package com.tle.web.connectors.manage;

import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;

public class ConnectorContentSearch
{
	private ExternalContentSortType sort;
	private String course;
	private String folder;
	private boolean archived;
	private boolean reverse;

	public ExternalContentSortType getSort()
	{
		return sort;
	}

	public void setSort(ExternalContentSortType sort)
	{
		this.sort = sort;
	}

	public String getCourse()
	{
		return course;
	}

	public void setCourse(String course)
	{
		this.course = course;
	}

	public boolean isArchived()
	{
		return archived;
	}

	public void setArchived(boolean archived)
	{
		this.archived = archived;
	}

	public String getFolder()
	{
		return folder;
	}

	public void setFolder(String folder)
	{
		this.folder = folder;
	}

	public boolean isReverse()
	{
		return reverse;
	}

	public void setReverse(boolean reverse)
	{
		this.reverse = reverse;
	}
}
