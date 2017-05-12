package com.tle.jpfclasspath.model;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.tle.jpfclasspath.JPFClasspathPlugin;

@SuppressWarnings("nls")
public class ProjectPluginModelImpl extends AbstractPluginModel implements IPreferenceChangeListener
{
	private IEclipsePreferences prefs;
	private WorkspacePluginModelManager manager;
	private IProject project;

	public ProjectPluginModelImpl(final WorkspacePluginModelManager manager, IProject project)
	{
		super(JPFProject.getManifest(project));
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
	public void preferenceChange(PreferenceChangeEvent event)
	{
		if( event.getKey().equals(JPFClasspathPlugin.PREF_REGISTRY_NAME) )
		{
			manager.modelChanged(this);
		}
	}

	public void stop()
	{
		try
		{
			prefs.removePreferenceChangeListener(this);
		}
		catch( IllegalStateException ise )
		{
			// no longer exists
		}
	}

	@Override
	public boolean isJarModel()
	{
		return false;
	}

	@Override
	protected InputStream getInputStream() throws CoreException
	{
		return ((IFile) underlyingResource).getContents(true);
	}

	@Override
	public String toString()
	{
		return "Project plugin:" + underlyingResource.toString();
	}

	@Override
	public List<IClasspathEntry> createClasspathEntries()
	{
		return Arrays.asList(JavaCore.newProjectEntry(underlyingResource.getProject().getFullPath()));
	}

	@Override
	public IJavaProject getJavaProject()
	{
		return JavaCore.create(underlyingResource.getProject());
	}

	@Override
	public String getRegistryName()
	{
		try
		{
			return prefs.get(JPFClasspathPlugin.PREF_REGISTRY_NAME, IPluginModel.DEFAULT_REGISTRY);
		}
		catch( IllegalStateException ise )
		{
			refreshPrefs();
			return prefs.get(JPFClasspathPlugin.PREF_REGISTRY_NAME, IPluginModel.DEFAULT_REGISTRY);
		}
	}
}
