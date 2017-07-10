package com.tle.web.scripting.advanced.guice;

import com.tle.core.config.guice.OptionalConfigModule;

public class AscGuiceModule extends OptionalConfigModule {

    @Override
    protected void configure()
    {
        bindBoolean("httpProxyServlet.enabled");
    }
}
