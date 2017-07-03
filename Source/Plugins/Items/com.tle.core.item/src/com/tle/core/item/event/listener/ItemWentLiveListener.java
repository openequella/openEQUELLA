package com.tle.core.item.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.item.event.ItemWentLiveEvent;

public interface ItemWentLiveListener extends ApplicationListener
{
	void itemWentLiveEvent(ItemWentLiveEvent event);
}
