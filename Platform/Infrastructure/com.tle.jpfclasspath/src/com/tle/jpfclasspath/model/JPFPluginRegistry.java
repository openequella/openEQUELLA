package com.tle.jpfclasspath.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.jpfclasspath.JPFClasspathLog;
import com.tle.jpfclasspath.parser.ModelPluginFragment;
import com.tle.jpfclasspath.parser.ModelPluginManifest;
import com.tle.jpfclasspath.parser.ModelPrerequisite;

@SuppressWarnings("nls")
public class JPFPluginRegistry
{
	private final String registryName;

	private List<JPFPluginRegistry> childRegistries = new ArrayList<>();

	private Map<IPluginModel, ResolvedPlugin> model2Resolved = new HashMap<>();
	private Map<String, ResolvedPlugin> resolvedPlugins = new HashMap<>();
	private Set<IPluginModel> ownedModels = new HashSet<>();

	private Set<String> parentRepos;

	public JPFPluginRegistry(String registryName)
	{
		this.registryName = registryName;
		JPFClasspathLog.logInfo("Registry '" + registryName + "' created");
	}

	public IResolvedPlugin getResolvedPlugin(IPluginModel model)
	{
		return model2Resolved.get(model);
	}

	public void addModel(IPluginModel model, PluginUpdateTracker tracker)
	{
		if( model.getParsedManifest() == null )
		{
			return;
		}
		if( !model.isJarModel() )
		{
			for( JPFPluginRegistry child : childRegistries )
			{
				child.addModelInternal(model, tracker);
			}
		}
		addModelInternal(model, tracker);
		ownedModels.add(model);
	}

	private void addModelInternal(IPluginModel model, PluginUpdateTracker tracker)
	{
		ModelPluginManifest parsedManifest = model.getParsedManifest();
		String id = parsedManifest.getId();
		ResolvedPlugin resolved = getOrCreate(id);
		IModel pluginModel = resolved.getPluginModel();
		if( pluginModel != null )
		{
			JPFClasspathLog.logInfo(registryName + ": Plugin already exists for id:" + id);
			return;
		}
		Set<IModel> alreadySeen = new HashSet<>();
		resolved.setPluginModel(model);
		model2Resolved.put(model, resolved);
		if( model.isFragmentModel() )
		{
			ModelPluginFragment fragment = (ModelPluginFragment) parsedManifest;
			String pluginId = fragment.getPluginId();
			ResolvedPlugin parentPlugin = getOrCreate(pluginId);
			resolved.setHostPlugin(parentPlugin);
			parentPlugin.addFragment(resolved);
			addChanges(parentPlugin, tracker, alreadySeen);
		}
		List<ResolvedImport> resolvedImports = new ArrayList<>();
		List<ModelPrerequisite> prerequisites = parsedManifest.getPrerequisites();
		for( ModelPrerequisite modelPrerequisite : prerequisites )
		{
			ResolvedPlugin resolvedDep = getOrCreate(modelPrerequisite.getPluginId());
			resolvedDep.addDependant(resolved);
			resolvedImports.add(new ResolvedImport(modelPrerequisite, resolvedDep));
		}
		addChanges(resolved, tracker, alreadySeen);
		resolved.setResolvedImports(resolvedImports);
		resolved.setParsedManifest(parsedManifest);
	}

	public void removeModel(IPluginModel model, PluginUpdateTracker tracker)
	{
		if( !model.isJarModel() )
		{
			for( JPFPluginRegistry child : childRegistries )
			{
				child.removeModelInternal(model, tracker);
			}
		}
		removeModelInternal(model, tracker);
	}

	private void removeModelInternal(IPluginModel model, PluginUpdateTracker tracker)
	{
		ownedModels.remove(model);
		ResolvedPlugin resolvedPlugin = model2Resolved.remove(model);
		if( resolvedPlugin == null )
		{
			return;
		}
		Set<IModel> alreadySeen = new HashSet<>();
		ResolvedPlugin hostPlugin = (ResolvedPlugin) resolvedPlugin.getHostPlugin();
		if( hostPlugin != null )
		{
			addChanges(hostPlugin, tracker, alreadySeen);
		}
		addChanges(resolvedPlugin, tracker, alreadySeen);
		List<ResolvedImport> imports = resolvedPlugin.getImports();
		for( ResolvedImport resolvedImport : imports )
		{
			ResolvedPlugin resolvedDep = (ResolvedPlugin) resolvedImport.getResolved();
			resolvedDep.removeDependency(resolvedPlugin);
		}
		if( hostPlugin != null )
		{
			hostPlugin.removeFragment(resolvedPlugin);
		}
		resolvedPlugin.clearPlugin();
	}

	private void addChanges(ResolvedPlugin resolved, PluginUpdateTracker tracker, Set<IModel> alreadySeen)
	{
		IPluginModel dmodel = resolved.getPluginModel();
		if( dmodel == null || alreadySeen.contains(dmodel) )
		{
			return;
		}
		alreadySeen.add(dmodel);
		tracker.addChange(dmodel);
		for( ResolvedPlugin dependant : resolved.getDependants() )
		{
			addChanges(dependant, tracker, alreadySeen);
		}
	}

	private ResolvedPlugin getOrCreate(String id)
	{
		ResolvedPlugin resolved = resolvedPlugins.get(id);
		if( resolved == null )
		{
			resolved = new ResolvedPlugin(id, registryName);
			resolvedPlugins.put(id, resolved);
		}
		return resolved;
	}

	public void removeChildRegistry(JPFPluginRegistry registry, PluginUpdateTracker tracker)
	{
		if( childRegistries.remove(registry) )
		{
			for( IPluginModel pluginModel : ownedModels )
			{
				if( !pluginModel.isJarModel() )
				{
					registry.removeModelInternal(pluginModel, tracker);
				}
			}
		}
	}

	public void addChildRegistry(JPFPluginRegistry registry, PluginUpdateTracker tracker)
	{
		childRegistries.add(registry);
		for( IPluginModel pluginModel : ownedModels )
		{
			if( !pluginModel.isJarModel() )
			{
				registry.addModelInternal(pluginModel, tracker);
			}
		}
	}

	public Set<String> getParentRepos()
	{
		return parentRepos;
	}

	public void setParentRepos(Set<String> parentRepos)
	{
		this.parentRepos = parentRepos;
	}

}
