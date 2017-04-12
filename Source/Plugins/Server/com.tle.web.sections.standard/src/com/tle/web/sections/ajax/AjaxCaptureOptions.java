package com.tle.web.sections.ajax;

import java.util.Map;

public class AjaxCaptureOptions
{
	private final String ajaxId;
	private final boolean includeTag;
	private final boolean collection;
	private final Map<String, Object> params;

	public AjaxCaptureOptions(String ajaxId, boolean includeTag, boolean collection, Map<String, Object> params)
	{
		this.ajaxId = ajaxId;
		this.includeTag = includeTag;
		this.collection = collection;
		this.params = params;
	}

	public String getAjaxId()
	{
		return ajaxId;
	}

	public boolean isIncludeTag()
	{
		return includeTag;
	}

	public boolean isCollection()
	{
		return collection;
	}

	public Map<String, Object> getParams()
	{
		return params;
	}
}
