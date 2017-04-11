package com.tle.jpfclasspath.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class JPFProjectSelectionDialog extends SelectionStatusDialog
{
	private TableViewer listViewer;
	private IProject initialSelection;

	public JPFProjectSelectionDialog(Shell parentShell, IProject selected)
	{
		super(parentShell);
		this.initialSelection = selected;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(dialogArea, SWT.NONE);
		createMessageArea(composite);
		listViewer = new TableViewer(composite, SWT.TOP | SWT.BORDER);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 400;
		data.heightHint = 400;
		listViewer.getTable().setLayoutData(data);
		listViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		listViewer.setContentProvider(new JarProjectContentProvider(null));
		listViewer.setComparator(new ViewerComparator());
		listViewer.setInput(ResourcesPlugin.getWorkspace());
		applyDialogFont(composite);

		listViewer.setSelection(new StructuredSelection(initialSelection));
		GridLayoutFactory.fillDefaults().generateLayout(composite);
		return dialogArea;
	}

	@Override
	protected void computeResult()
	{
		setResult(((StructuredSelection) listViewer.getSelection()).toList());
	}

	@Override
	protected void cancelPressed()
	{
		setResult(null);
		super.cancelPressed();
	}

}
