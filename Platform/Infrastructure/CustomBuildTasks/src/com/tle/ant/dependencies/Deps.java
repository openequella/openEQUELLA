package com.tle.ant.dependencies;

import java.util.List;
import java.util.Map;

public class Deps
{
	public Deps()
	{

	}

	private List<String> exclusions;
	private List<String> ignoredArtifacts;
	private List<Dep> dependencies;
	private Map<String, String> versions;

	public List<Dep> getDependencies()
	{
		return dependencies;
	}

	public void setDependencies(List<Dep> dependencies)
	{
		this.dependencies = dependencies;
	}

	public List<String> getExclusions()
	{
		return exclusions;
	}

	public void setExclusions(List<String> exclusions)
	{
		this.exclusions = exclusions;
	}

	public Map<String, String> getVersions()
	{
		return versions;
	}

	public void setVersions(Map<String, String> versions)
	{
		this.versions = versions;
	}

	public List<String> getIgnoredArtifacts()
	{
		return ignoredArtifacts;
	}

	public void setIgnoredArtifacts(List<String> ignoredArtifacts)
	{
		this.ignoredArtifacts = ignoredArtifacts;
	}
}