package com.tle.mypages.web.event;

import java.util.EventListener;

import com.tle.web.sections.SectionInfo;

/*
 * @author aholland
 */
public interface SaveItemEventListener extends EventListener
{
	void doSaveItemEvent(SectionInfo info, SaveItemEvent event);
}
