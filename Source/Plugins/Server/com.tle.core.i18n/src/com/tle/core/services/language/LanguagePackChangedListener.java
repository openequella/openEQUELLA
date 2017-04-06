package com.tle.core.services.language;

import com.tle.core.events.listeners.ApplicationListener;

public interface LanguagePackChangedListener extends ApplicationListener
{
	void languageChangedEvent(LanguagePackChangedEvent event);
}
