package com.tle.core.item.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.item.event.WaitForItemIndexEvent;

public interface WaitForItemIndexListener extends ApplicationListener
{

	void waitForItem(WaitForItemIndexEvent event);
}
