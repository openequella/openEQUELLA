package com.tle.web.sections.events;

import java.util.EventListener;

import com.tle.web.sections.SectionResult;

public interface RenderResultListener extends EventListener
{
	void returnResult(SectionResult result, String fromId);
}
