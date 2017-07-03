package com.tle.core.i18n.event.listener;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.i18n.event.LanguagePackChangedEvent;

public interface LanguagePackChangedListener extends ApplicationListener
{
	void languageChangedEvent(LanguagePackChangedEvent event);
}
