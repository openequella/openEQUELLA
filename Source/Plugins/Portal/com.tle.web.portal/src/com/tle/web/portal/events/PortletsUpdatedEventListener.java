package com.tle.web.portal.events;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.web.sections.SectionInfo;

public interface PortletsUpdatedEventListener extends ApplicationListener
{
	/**
	 * @param thisInfo Will only be set for the node where the portlet was
	 *            deleted from.
	 */
	void portletsUpdated(PortletsUpdatedEvent event, SectionInfo thisInfo);
}
