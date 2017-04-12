package com.tle.web.discoverability.scripting.impl;

import org.apache.commons.lang.StringEscapeUtils;

import com.tle.web.discoverability.scripting.objects.MetaScriptObject;
import com.tle.web.sections.events.PreRenderContext;

/**
 * @author aholland
 */
public class MetaScriptWrapper implements MetaScriptObject
{
	private static final long serialVersionUID = 1L;
	private PreRenderContext render;

	public MetaScriptWrapper(PreRenderContext render)
	{
		this.render = render;
	}

	@SuppressWarnings("nls")
	@Override
	public void add(String name, String content)
	{
		StringBuilder tag = new StringBuilder();
		tag.append("<meta name=\"");
		tag.append(StringEscapeUtils.escapeHtml(name));
		tag.append("\" content=\"");
		tag.append(StringEscapeUtils.escapeHtml(content));
		tag.append("\">\n");
		render.addHeaderMarkup(tag.toString());
	}

	@Override
	public void scriptEnter()
	{
		// Nothing by default
	}

	@Override
	public void scriptExit()
	{
		// Nothing by default
	}

}