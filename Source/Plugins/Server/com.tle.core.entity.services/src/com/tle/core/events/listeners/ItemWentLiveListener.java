package com.tle.core.events.listeners;

import com.tle.core.events.ItemWentLiveEvent;

public interface ItemWentLiveListener extends ApplicationListener
{
	void itemWentLiveEvent(ItemWentLiveEvent event);
}
