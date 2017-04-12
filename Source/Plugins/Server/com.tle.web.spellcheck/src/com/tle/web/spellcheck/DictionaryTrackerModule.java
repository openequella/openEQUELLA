package com.tle.web.spellcheck;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.spellcheck.dictionary.TLEDictionary;

public class DictionaryTrackerModule extends PluginTrackerModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bindTracker(TLEDictionary.class, "dictionary", "bean").orderByParameter("order");
	}
}
