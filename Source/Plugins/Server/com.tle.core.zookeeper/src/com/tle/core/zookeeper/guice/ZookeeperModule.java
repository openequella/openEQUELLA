package com.tle.core.zookeeper.guice;

import com.tle.core.config.guice.OptionalConfigModule;

@SuppressWarnings("nls")
public class ZookeeperModule extends OptionalConfigModule
{
	@Override
	protected void configure()
	{
		bindProp("zookeeper.instances");
		bindProp("zookeeper.prefix", "");
		bindProp("zookeeper.nodeId", "");
	}
}
