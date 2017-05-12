package com.tle.web.sections.header;

import java.util.Collections;
import java.util.Map;

public class SimpleFormAction implements FormAction
{
	private String action;

	public SimpleFormAction(String action)
	{
		this.action = action;
	}

	@Override
	public String getFormAction()
	{
		return action;
	}

	@Override
	public Map<String, String[]> getHiddenState()
	{
		return Collections.emptyMap();
	}

}
