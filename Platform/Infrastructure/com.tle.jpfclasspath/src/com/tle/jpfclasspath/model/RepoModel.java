package com.tle.jpfclasspath.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

import com.tle.jpfclasspath.JPFClasspathPlugin;

@SuppressWarnings("nls")
public class RepoModel implements IRepoModel, IPreferenceChangeListener
{
	private IProject project;
	private List<JarPluginModelImpl> jarModels = new ArrayList<>();
	private IEclipsePreferences prefs;
	private WorkspaceJarModelManager manager;

	public RepoModel(WorkspaceJarModelManager manager, IProject project)
	{
		this.project = project;
		this.manager = manager;
		refreshPrefs();
	}

	private void refreshPrefs()
	{
		prefs = JPFProject.getPreferences(project);
		prefs.addPreferenceChangeListener(this);
	}

	@Override
	public void reload()
	{
		// nothing
	}

	@Override
	public IResource getUnderlyingResource()
	{
		return project;
	}

	@Override
	public Set<String> getParentRepos()
	{
		String parents;
		try
		{
			parents = prefs.get(JPFClasspathPlugin.PREF_PARENT_REGISTRIES, "");
		}
		catch( IllegalStateException ise )
		{
			refreshPrefs();
			parents = prefs.get(JPFClasspathPlugin.PREF_PARENT_REGISTRIES, "");
		}
		Path path = new Path(parents);
		Set<String> parentSet = new HashSet<>();
		for( String parentStr : path.segments() )
		{
			if( parentStr.length() > 0 )
			{
				parentSet.add(parentStr);
			}
		}
		return parentSet;
	}

	@Override
	public List<? extends IPluginModel> getJarModels()
	{
		return jarModels;
	}

	@Override
	public String getName()
	{
		return project.getName();
	}

	public void addJarModel(JarPluginModelImpl model)
	{
		jarModels.add(model);
	}

	public void removeJarModel(JarPluginModelImpl model)
	{
		jarModels.remove(model);
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event)
	{
		if( event.getKey().equals(JPFClasspathPlugin.PREF_PARENT_REGISTRIES) )
		{
			manager.modelChanged(this);
		}
	}

	@Override
	public String toString()
	{
		return "JPF Registry:" + project;
	}

}
