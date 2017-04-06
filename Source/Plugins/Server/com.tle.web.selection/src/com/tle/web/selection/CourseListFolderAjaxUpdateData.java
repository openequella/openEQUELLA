package com.tle.web.selection;

import java.util.Set;

public class CourseListFolderAjaxUpdateData
{
	private String folderId;
	private Set<String> ajaxIds;
	private String[] event;

	public String getFolderId()
	{
		return folderId;
	}

	public void setFolderId(String folderId)
	{
		this.folderId = folderId;
	}

	public Set<String> getAjaxIds()
	{
		return ajaxIds;
	}

	public void setAjaxIds(Set<String> ajaxIds)
	{
		this.ajaxIds = ajaxIds;
	}

	public String[] getEvent()
	{
		return event;
	}

	public void setEvent(String[] event)
	{
		this.event = event;
	}
}