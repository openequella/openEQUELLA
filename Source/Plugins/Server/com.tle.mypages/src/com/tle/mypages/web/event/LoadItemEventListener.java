package com.tle.mypages.web.event;

import java.util.EventListener;

import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public interface LoadItemEventListener extends EventListener
{
	void doLoadItemEvent(SectionInfo info, LoadItemEvent event);
}
