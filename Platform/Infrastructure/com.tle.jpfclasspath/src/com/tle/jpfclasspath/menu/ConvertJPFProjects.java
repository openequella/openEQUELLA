package com.tle.jpfclasspath.menu;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.tle.jpfclasspath.JPFClasspathLog;
import com.tle.jpfclasspath.JPFProjectNature;
import com.tle.jpfclasspath.model.JPFProject;

public class ConvertJPFProjects extends ContributionItem
{

	public ConvertJPFProjects()
	{
		// nothing
	}

	public ConvertJPFProjects(String id)
	{
		super(id);
	}

	@Override
	public void fill(Menu menu, int index)
	{
		final MenuItem menuItem = new MenuItem(menu, SWT.NONE, index);
		menuItem.setText("Convert existing JPF Projects to new format");
		menuItem.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				run();
			}
		});
	}

	protected void run()
	{
		new Job("Convert JPF projects")
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				for( IProject project : projects )
				{
					if( project.isOpen() && !JPFProjectNature.hasNature(project)
						&& JPFProject.getManifest(project).exists() )
					{
						IProjectDescription description;
						try
						{
							description = project.getDescription();
							JPFProjectNature.addNature(description);
							project.setDescription(description, monitor);
						}
						catch( CoreException e )
						{
							JPFClasspathLog.logError(e);
						}
					}
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}
}
