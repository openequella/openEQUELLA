package com.tle.jpfclasspath.model;

import java.util.HashSet;
import java.util.Set;

public class PluginUpdateTracker
{
	private Set<IPluginModel> changes = new HashSet<>();

	public void addChange(IPluginModel change)
	{
		changes.add(change);
	}

	public Set<IPluginModel> getChanges()
	{
		return changes;
	}
}
