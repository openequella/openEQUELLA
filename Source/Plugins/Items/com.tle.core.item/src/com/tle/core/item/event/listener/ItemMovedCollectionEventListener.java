package com.tle.core.item.event.listener;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.item.event.ItemMovedCollectionEvent;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
public interface ItemMovedCollectionEventListener extends ApplicationListener
{
	void itemMovedCollection(ItemMovedCollectionEvent event);
}