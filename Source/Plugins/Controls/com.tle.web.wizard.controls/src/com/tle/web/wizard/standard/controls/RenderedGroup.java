package com.tle.web.wizard.standard.controls;

import java.util.List;

import com.tle.web.wizard.page.ControlResult;

public class RenderedGroup
{
	private final List<ControlResult> results;

	public RenderedGroup(List<ControlResult> results)
	{
		this.results = results;
	}

	public List<ControlResult> getResults()
	{
		return results;
	}
}