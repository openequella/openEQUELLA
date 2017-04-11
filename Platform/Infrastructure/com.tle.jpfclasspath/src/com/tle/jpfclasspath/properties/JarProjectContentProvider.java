package com.tle.jpfclasspath.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.model.WorkbenchContentProvider;

import com.tle.jpfclasspath.JPFJarNature;

public class JarProjectContentProvider extends WorkbenchContentProvider
{
	private final IProject project;

	public JarProjectContentProvider(IProject project)
	{
		this.project = project;
	}

	@Override
	public Object[] getChildren(Object o)
	{
		if( !(o instanceof IWorkspace) )
		{
			return new Object[0];
		}

		// Collect all the projects in the workspace except the given
		// project
		IProject[] projects = ((IWorkspace) o).getRoot().getProjects();
		List<IProject> referenced = new ArrayList<>(projects.length);
		boolean found = false;
		for( int i = 0; i < projects.length; i++ )
		{
			IProject otherProject = projects[i];
			if( !found && otherProject.equals(project) )
			{
				found = true;
				continue;
			}
			try
			{
				if( otherProject.isOpen() && otherProject.hasNature(JPFJarNature.NATURE_ID) )
				{
					referenced.add(otherProject);
				}
			}
			catch( CoreException e )
			{
				// ignore
			}
		}

		return referenced.toArray();
	}
}