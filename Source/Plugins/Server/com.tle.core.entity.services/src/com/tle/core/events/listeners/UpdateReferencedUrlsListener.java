package com.tle.core.events.listeners;

import com.tle.core.events.UpdateReferencedUrlsEvent;

/**
 * @author Nicholas Read
 */
public interface UpdateReferencedUrlsListener extends ApplicationListener
{
	void updateReferencedUrlsEvent(UpdateReferencedUrlsEvent event);
}
