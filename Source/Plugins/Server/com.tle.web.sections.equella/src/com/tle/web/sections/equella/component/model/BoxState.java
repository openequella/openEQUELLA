package com.tle.web.sections.equella.component.model;

import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.model.HtmlComponentState;

/**
 * @author aholland
 */
public class BoxState extends HtmlComponentState
{
	private boolean minimised;
	private JSCallable toggleMinimise;
	private boolean noMinMaxOnHeader;

	public boolean isMinimised()
	{
		return minimised;
	}

	public void setMinimised(boolean minimised)
	{
		this.minimised = minimised;
	}

	public JSCallable getToggleMinimise()
	{
		return toggleMinimise;
	}

	public void setToggleMinimise(JSCallable toggleMinimise)
	{
		this.toggleMinimise = toggleMinimise;
	}

	public boolean isNoMinMaxOnHeader()
	{
		return noMinMaxOnHeader;
	}

	public void setNoMinMaxOnHeader(boolean noMinMaxOnHeader)
	{
		this.noMinMaxOnHeader = noMinMaxOnHeader;
	}
}
