package com.tle.jpfclasspath.properties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.tle.jpfclasspath.JPFClasspathLog;
import com.tle.jpfclasspath.JPFClasspathPlugin;

public class JPFParentRepositoriesPage extends PropertyPage
{
	private IProject project;

	private boolean modified = false;

	// widgets
	private CheckboxTableViewer listViewer;

	/*
	 * @see PreferencePage#createContents
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);

		initialize();

		createDescriptionLabel(composite);

		listViewer = CheckboxTableViewer.newCheckList(composite, SWT.TOP | SWT.BORDER);

		if( !project.isOpen() )
			listViewer.getControl().setEnabled(false);

		listViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		listViewer.setContentProvider(new JarProjectContentProvider(project));
		listViewer.setComparator(new ViewerComparator());
		listViewer.setInput(project.getWorkspace());

		String parents = getPreferenceStore().getString(JPFClasspathPlugin.PREF_PARENT_REGISTRIES);
		Path path = new Path(parents);
		String[] segments = path.segments();
		List<IProject> projects = new ArrayList<>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for( String projectName : segments )
		{
			IProject project = root.getProject(projectName);
			projects.add(project);
		}
		listViewer.setCheckedElements(projects.toArray());
		listViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				modified = true;
			}
		});

		applyDialogFont(composite);

		GridLayoutFactory.fillDefaults().generateLayout(composite);

		return composite;
	}


	/**
	 * Initializes a ProjectReferencePage.
	 */
	private void initialize()
	{
		project = (IProject) getElement().getAdapter(IResource.class);
		noDefaultAndApplyButton();
		setDescription(String.format("Choose parent repositories for %s", project.getName()));
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore()
	{
		return new ScopedPreferenceStore(new ProjectScope(project), JPFClasspathPlugin.PLUGIN_ID);
	}

	/**
	 * @see PreferencePage#performOk
	 */
	@Override
	public boolean performOk()
	{
		if( !modified )
		{
			return true;
		}
		IPath path = Path.EMPTY;
		Object[] checked = listViewer.getCheckedElements();
		for( Object elements : checked )
		{
			path = path.append(((IProject) elements).getName());
		}
		IPreferenceStore prefs = getPreferenceStore();
		prefs.setValue(JPFClasspathPlugin.PREF_PARENT_REGISTRIES, path.toString());
		try
		{
			((IPersistentPreferenceStore) prefs).save();
		}
		catch( IOException e )
		{
			JPFClasspathLog.logError(e);
		}
		return true;
	}

}