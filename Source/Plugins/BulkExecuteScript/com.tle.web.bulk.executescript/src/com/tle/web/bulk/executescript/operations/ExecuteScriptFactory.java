package com.tle.web.bulk.executescript.operations;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;

@BindFactory
public interface ExecuteScriptFactory
{
	ExecuteScriptOperation executeScript(@Assisted("script") String script);
}
