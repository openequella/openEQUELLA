package com.tle.mypages.web.event;

import java.util.EventListener;

import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public interface ChangePageEventListener extends EventListener
{
	void changePage(SectionInfo info, ChangePageEvent event);
}
