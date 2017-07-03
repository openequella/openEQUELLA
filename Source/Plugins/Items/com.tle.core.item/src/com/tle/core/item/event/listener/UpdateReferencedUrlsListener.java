package com.tle.core.item.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.item.event.UpdateReferencedUrlsEvent;

/**
 * @author Nicholas Read
 */
public interface UpdateReferencedUrlsListener extends ApplicationListener
{
	void updateReferencedUrlsEvent(UpdateReferencedUrlsEvent event);
}
