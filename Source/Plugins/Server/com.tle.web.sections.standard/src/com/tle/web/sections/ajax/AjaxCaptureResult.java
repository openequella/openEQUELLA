package com.tle.web.sections.ajax;

public class AjaxCaptureResult
{
	private final String html;
	private final String script;
	private final String divId;

	public AjaxCaptureResult(String html, String script, String divId)
	{
		this.html = html;
		this.script = script;
		this.divId = divId;
	}

	public AjaxCaptureResult(AjaxCaptureResult captured)
	{
		this.html = captured.html;
		this.script = captured.script;
		this.divId = captured.divId;
	}

	public String getHtml()
	{
		return html;
	}

	public String getScript()
	{
		return script;
	}

	public String getDivId()
	{
		return divId;
	}
}
