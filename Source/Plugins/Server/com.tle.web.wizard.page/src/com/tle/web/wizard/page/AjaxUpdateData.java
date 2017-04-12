package com.tle.web.wizard.page;

import java.util.Set;

public class AjaxUpdateData
{
	private String controlId;
	private Set<String> visibleIds;
	private Set<String> ajaxIds;
	private String[] event;

	public Set<String> getVisibleIds()
	{
		return visibleIds;
	}

	public void setVisibleIds(Set<String> visibleIds)
	{
		this.visibleIds = visibleIds;
	}

	public Set<String> getAjaxIds()
	{
		return ajaxIds;
	}

	public void setAjaxIds(Set<String> ajaxIds)
	{
		this.ajaxIds = ajaxIds;
	}

	public String getControlId()
	{
		return controlId;
	}

	public void setControlId(String controlId)
	{
		this.controlId = controlId;
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