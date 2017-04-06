package com.tle.web.sections.ajax;

public class SimpleDOMResult extends AbstractDOMResult
{
	private final String html;
	private final String readyScript;

	public SimpleDOMResult(AbstractDOMResult result, String html, String readyScript)
	{
		super(result);
		this.html = html;
		this.readyScript = readyScript;
	}

	public String getHtml()
	{
		return html;
	}

	public String getReadyScript()
	{
		return readyScript;
	}
}
