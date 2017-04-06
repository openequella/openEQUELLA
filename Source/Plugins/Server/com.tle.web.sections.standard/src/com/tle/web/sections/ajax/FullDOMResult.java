package com.tle.web.sections.ajax;

import java.util.List;
import java.util.Map;

public class FullDOMResult extends AbstractDOMResult
{
	private Map<String, FullAjaxCaptureResult> html;
	private Map<String, List<FullAjaxCaptureResult>> lists;

	public Map<String, FullAjaxCaptureResult> getHtml()
	{
		return html;
	}

	public void setHtml(Map<String, FullAjaxCaptureResult> html)
	{
		this.html = html;
	}

	public Map<String, List<FullAjaxCaptureResult>> getLists()
	{
		return lists;
	}

	public void setLists(Map<String, List<FullAjaxCaptureResult>> lists)
	{
		this.lists = lists;
	}
}
