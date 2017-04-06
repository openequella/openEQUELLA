package com.tle.web.sections.ajax;

import java.util.Map;

public class FullAjaxCaptureResult extends AjaxCaptureResult
{
	private final Map<String, Object> params;

	public FullAjaxCaptureResult(String html, String script, String divId, Map<String, Object> params)
	{
		super(html, script, divId);
		this.params = params;
	}

	public Map<String, Object> getParams()
	{
		return params;
	}
}
