package com.tle.web.sections.equella.component.model;

import java.util.Map;

import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlValueState;

/**
 * @author Andrew Gibb
 */
public class MultiEditBoxState extends HtmlComponentState
{
	private int size;
	private Map<String, HtmlValueState> localeMap;

	public MultiEditBoxState()
	{
		super("multieditbox"); //$NON-NLS-1$
	}

	public Map<String, HtmlValueState> getLocaleMap()
	{
		return localeMap;
	}

	public void setLocaleMap(Map<String, HtmlValueState> localeMap)
	{
		this.localeMap = localeMap;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

}
