package com.tle.web.htmleditor;

import java.io.Serializable;

import com.tle.common.htmleditor.HtmlEditorConfiguration;

/**
 * @author Aaron
 */
public class HtmlEditorConfigurationEditingSession implements Serializable
{
	private String sessionId;
	private HtmlEditorConfiguration config;
	private boolean dirty;

	public String getSessionId()
	{
		return sessionId;
	}

	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}

	public HtmlEditorConfiguration getConfig()
	{
		return config;
	}

	public void setConfig(HtmlEditorConfiguration config)
	{
		this.config = config;
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public void setDirty(boolean dirty)
	{
		this.dirty = dirty;
	}
}
