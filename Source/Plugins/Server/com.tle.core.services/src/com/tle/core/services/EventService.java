package com.tle.core.services;

import java.util.Collection;

import com.tle.beans.Institution;
import com.tle.core.events.ApplicationEvent;

public interface EventService
{
	void publishApplicationEvent(ApplicationEvent<?> event);

	/**
	 * This will publish the event once for each institution. This only makes
	 * sense from "server" tasks, etc.. that then need to notify all
	 * institutions of a change. It will also fail if you attempt to post a
	 * synchronous event.
	 */
	void publishApplicationEvent(Collection<Institution> institutions, ApplicationEvent<?> event);
}
