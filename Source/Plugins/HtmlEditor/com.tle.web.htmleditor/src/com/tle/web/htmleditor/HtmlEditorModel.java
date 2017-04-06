package com.tle.web.htmleditor;

import com.tle.common.scripting.ScriptContextFactory;

/**
 * @author aholland
 */
public class HtmlEditorModel
{
	protected String width;
	protected String height;
	protected int rows;
	protected ScriptContextFactory scriptContextFactory;

	public String getWidth()
	{
		return width;
	}

	public void setWidth(String width)
	{
		this.width = width;
	}

	public String getHeight()
	{
		return height;
	}

	public void setHeight(String height)
	{
		this.height = height;
	}

	public int getRows()
	{
		return rows;
	}

	public void setRows(int rows)
	{
		this.rows = rows;
	}

	public ScriptContextFactory getScriptContextFactory()
	{
		return scriptContextFactory;
	}

	public void setScriptContextFactory(ScriptContextFactory scriptContextFactory)
	{
		this.scriptContextFactory = scriptContextFactory;
	}
}
