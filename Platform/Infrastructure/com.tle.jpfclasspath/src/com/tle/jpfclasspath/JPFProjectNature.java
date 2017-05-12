package com.tle.jpfclasspath;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;

@SuppressWarnings("nls")
public class JPFProjectNature implements IProjectNature
{
	public static final String NATURE_ID = JPFClasspathPlugin.PLUGIN_ID + ".jpfproject";

	private IProject project;

	@Override
	public void configure() throws CoreException
	{
		IProjectDescription description = project.getDescription();
		JPFManifestBuilder.addBuilderToProject(description);
		project.setDescription(description, null);
		JPFClasspathContainer.addToProject(JavaCore.create(project));
		new Job("Check JPF Manifest")
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					project.build(IncrementalProjectBuilder.FULL_BUILD, JPFManifestBuilder.BUILDER_ID, null, monitor);
				}
				catch( CoreException e )
				{
					JPFClasspathLog.logError(e);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	@Override
	public void deconfigure() throws CoreException
	{
		IProjectDescription description = project.getDescription();
		JPFManifestBuilder.removeBuilderFromProject(description);
		JPFClasspathContainer.removeFromProject(JavaCore.create(project));
		project.setDescription(description, null);
		JPFManifestBuilder.deleteMarkers(project);
	}

	@Override
	public IProject getProject()
	{
		return project;
	}

	@Override
	public void setProject(IProject project)
	{
		this.project = project;
	}

	public static void addNature(IProjectDescription description)
	{
		Set<String> newIds = new LinkedHashSet<>();
		newIds.addAll(Arrays.asList(description.getNatureIds()));
		if( newIds.add(NATURE_ID) )
		{
			description.setNatureIds(newIds.toArray(new String[newIds.size()]));
		}
	}

	public static boolean hasNature(IProject project)
	{
		try
		{
			return project.isOpen() && project.hasNature(NATURE_ID);
		}
		catch( CoreException e )
		{
			JPFClasspathLog.logError(e);
			return false;
		}
	}

	public static void removeNature(IProjectDescription description)
	{
		Set<String> newIds = new LinkedHashSet<>();
		newIds.addAll(Arrays.asList(description.getNatureIds()));
		if( newIds.remove(NATURE_ID) )
		{
			description.setNatureIds(newIds.toArray(new String[newIds.size()]));
		}
	}

}
