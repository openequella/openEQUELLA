package com.tle.core.services.impl;

import java.io.Serializable;

import com.tle.core.plugins.PluginService;

public interface ClusteredTask extends Serializable
{
	boolean isTransient();

	Serializable[] getArgs();

	boolean isGlobal();

	String getGlobalId();

	Task createTask(PluginService pluginService, Serializable[] args);

	String getInternalId();

	boolean isPriority();
}