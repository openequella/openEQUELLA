package com.tle.jpfclasspath;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.tle.jpfclasspath.model.IPluginModel;
import com.tle.jpfclasspath.model.WorkspacePluginModelManager;

public class JPFPluginRebuilder implements IResourceChangeListener
{

	private Set<String> fProjectNames = new HashSet<String>();

	private boolean fTouchWorkspace = false;

	private static JPFPluginRebuilder instance;

	public void start()
	{
		JavaCore.addPreProcessingResourceChangedListener(this, IResourceChangeEvent.PRE_BUILD);
		instance = this;
	}

	public void stop()
	{
		JavaCore.removePreProcessingResourceChangedListener(this);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event)
	{
		if( event.getType() == IResourceChangeEvent.PRE_BUILD )
		{
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			if( fTouchWorkspace )
			{
				IProject[] projects = root.getProjects();
				for( int i = 0; i < projects.length; i++ )
				{
					touchProject(projects[i]);
				}
			}
			else
			{
				Iterator<String> iter = fProjectNames.iterator();
				while( iter.hasNext() )
				{
					touchProject(root.getProject(iter.next()));
				}
			}
			fTouchWorkspace = false;
			fProjectNames.clear();
		}
	}

	private void touchProject(IProject project)
	{
		if( WorkspacePluginModelManager.isPluginProject(project) )
		{
			try
			{
				// set session property on project
				// to be read and reset in ManifestConsistencyChecker
				project.setSessionProperty(JPFClasspathPlugin.TOUCH_PROJECT, Boolean.TRUE);
				// touch project so that ManifestConsistencyChecker#build(..)
				// gets invoked
				project.touch(new NullProgressMonitor());
			}
			catch( CoreException e )
			{
				JPFClasspathLog.logError(e);
			}
		}
	}

	public static JPFPluginRebuilder instance()
	{
		return instance;
	}

	public void pluginsChanged(Set<IPluginModel> changes)
	{
		for( IPluginModel model : changes )
		{
			IJavaProject project = model.getJavaProject();
			if( project != null )
			{
				fProjectNames.add(project.getProject().getName());
			}
		}
	}
}
