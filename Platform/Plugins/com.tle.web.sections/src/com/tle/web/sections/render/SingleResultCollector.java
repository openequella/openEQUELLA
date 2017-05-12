package com.tle.web.sections.render;

import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderResultListener;

public class SingleResultCollector implements RenderResultListener
{
	private SectionResult result;

	@Override
	public void returnResult(SectionResult result, String fromId)
	{
		this.result = result;
	}

	public SectionResult getResult()
	{
		return result;
	}

}
