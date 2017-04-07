package com.tle.jpfclasspath.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tle.jpfclasspath.parser.ModelPluginManifest;

public class ResolvedPlugin implements IResolvedPlugin
{
	private String id;
	private IPluginModel pluginModel;

	private IResolvedPlugin hostPlugin;
	private List<IResolvedPlugin> fragments = new ArrayList<>();
	private Set<ResolvedPlugin> dependants = new HashSet<>();

	private ModelPluginManifest parsedManifest;

	private List<ResolvedImport> imports;
	private String registryName;

	public ResolvedPlugin(String uniqueId, String registryName)
	{
		this.id = uniqueId;
		this.registryName = registryName;
	}

	public void addDependant(ResolvedPlugin dependant)
	{
		dependants.add(dependant);
	}

	public void setResolvedImports(List<ResolvedImport> resolvedImports)
	{
		this.imports = resolvedImports;
	}

	public ModelPluginManifest getParsedManifest()
	{
		return parsedManifest;
	}

	public void setParsedManifest(ModelPluginManifest parsedManifest)
	{
		this.parsedManifest = parsedManifest;
	}

	@Override
	public List<ResolvedImport> getImports()
	{
		return imports;
	}

	@Override
	public IPluginModel getPluginModel()
	{
		return pluginModel;
	}

	public void setPluginModel(IPluginModel pluginModel)
	{
		this.pluginModel = pluginModel;
	}

	public Set<ResolvedPlugin> getDependants()
	{
		return dependants;
	}

	public void clearPlugin()
	{
		parsedManifest = null;
		pluginModel = null;
		imports = null;
		hostPlugin = null;
	}

	public void removeDependency(ResolvedPlugin resolvedPlugin)
	{
		dependants.remove(resolvedPlugin);
	}

	public void addFragment(ResolvedPlugin resolved)
	{
		fragments.add(resolved);
	}

	@Override
	public List<IResolvedPlugin> getFragments()
	{
		return fragments;
	}

	@Override
	public IResolvedPlugin getHostPlugin()
	{
		return hostPlugin;
	}

	public void setHostPlugin(IResolvedPlugin hostPlugin)
	{
		this.hostPlugin = hostPlugin;
	}

	@Override
	public String toString()
	{
		return "ResolvedPlugin for uniqueId:" + id + ":" + pluginModel;
	}

	public void removeFragment(ResolvedPlugin resolvedPlugin)
	{
		fragments.remove(resolvedPlugin);
	}

	@Override
	public String getRegistryName()
	{
		return registryName;
	}
}
