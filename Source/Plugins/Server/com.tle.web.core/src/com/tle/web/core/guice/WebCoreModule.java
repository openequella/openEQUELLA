package com.tle.web.core.guice;

import com.tle.core.config.guice.OptionalConfigModule;

public class WebCoreModule extends OptionalConfigModule
{
    @Override
    protected void configure()
    {
        bindInt("strictTransportSecurity.maxage", -1);
    }
}
