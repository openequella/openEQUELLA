package com.tle.core.services.http;

import com.tle.core.config.guice.OptionalConfigModule;

public class HttpConfigModule extends OptionalConfigModule {
    @Override
    protected void configure()
    {
        bindBoolean("can.access.internet");
    }
}
