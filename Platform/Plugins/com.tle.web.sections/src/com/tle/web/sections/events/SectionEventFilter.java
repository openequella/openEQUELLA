package com.tle.web.sections.events;

import java.util.EventListener;

import com.tle.web.sections.SectionId;

public interface SectionEventFilter
{
	boolean shouldFire(SectionId sectionId, SectionEvent<? extends EventListener> event, EventListener listener);
}
