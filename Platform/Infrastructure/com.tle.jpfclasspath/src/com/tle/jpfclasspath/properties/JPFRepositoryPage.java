package com.tle.jpfclasspath.properties;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.tle.jpfclasspath.JPFClasspathLog;
import com.tle.jpfclasspath.JPFClasspathPlugin;

public class JPFRepositoryPage extends PropertyPage
{
	private IProject project;

	private boolean modified = false;

	// widgets
	private TableViewer listViewer;

	/*
	 * @see PreferencePage#createContents
	 */
	@Override
	protected Control createContents(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);

		initialize();

		createDescriptionLabel(composite);

		listViewer = new TableViewer(composite, SWT.TOP | SWT.BORDER);

		if( !project.isOpen() )
			listViewer.getControl().setEnabled(false);

		listViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		listViewer.setContentProvider(new JarProjectContentProvider(project));
		listViewer.setComparator(new ViewerComparator());
		listViewer.setInput(project.getWorkspace());

		String regName = getPreferenceStore().getString(JPFClasspathPlugin.PREF_REGISTRY_NAME);
		if( !regName.isEmpty() )
		{
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			listViewer.setSelection(new StructuredSelection(root.getProject(regName)));
		}
		listViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				modified = true;
			}
		});
		applyDialogFont(composite);

		GridLayoutFactory.fillDefaults().generateLayout(composite);

		return composite;
	}

	private void initialize()
	{
		project = (IProject) getElement().getAdapter(IResource.class);
		noDefaultAndApplyButton();
		setDescription(String.format("Choose compilation dependency project for %s", project.getName()));
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
		StructuredSelection selection = (StructuredSelection) listViewer.getSelection();
		IProject project = (IProject) selection.getFirstElement();
		if( project != null )
		{
			IPreferenceStore prefs = getPreferenceStore();
			prefs.setValue(JPFClasspathPlugin.PREF_REGISTRY_NAME, project.getName());
			try
			{
				((IPersistentPreferenceStore) prefs).save();
			}
			catch( IOException e )
			{
				JPFClasspathLog.logError(e);
			}
		}
		return true;
	}

}