package com.tle.core.events.listeners;

import com.tle.core.events.WaitForItemIndexEvent;

public interface WaitForItemIndexListener extends ApplicationListener
{

	void waitForItem(WaitForItemIndexEvent event);
}
