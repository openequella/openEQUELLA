package com.tle.mypages.web.event;

import java.util.EventListener;

import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public interface SavePageEventListener extends EventListener
{
	void doSavePageEvent(SectionInfo info, SavePageEvent event);
}
