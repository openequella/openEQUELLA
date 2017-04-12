package com.tle.reporting.oda.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FreetextEditorPage extends SimpleEditorPage
{
	private static final String QUERY_PREFIX = "q:";
	private static final String COUNT_PREFIX = "count:";
	private static final String MATRIX_PREFIX = "m:";
	private static final String MATRIX_COUNT_PREFIX = "mc:";
	private static final String FILES_PREFIX = "f:";
	private Text whereClause;
	private Text fields;

	public FreetextEditorPage(String arg0)
	{
		super(arg0);
	}

	public FreetextEditorPage(String arg0, String arg1, ImageDescriptor arg2)
	{
		super(arg0, arg1, arg2);
	}

	@Override
	protected void createExtraFields(Composite parent)
	{
		Label queryLabel = new Label(parent, SWT.LEFT);
		queryLabel.setText(TLEOdaPlugin.getResourceString("freetext.label.whereclause"));
		whereClause = new Text(parent, SWT.BORDER | SWT.SINGLE);
		whereClause.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label fieldsLabel = new Label(parent, SWT.LEFT);
		fieldsLabel.setText(TLEOdaPlugin.getResourceString("freetext.label.fields"));
		fields = new Text(parent, SWT.BORDER | SWT.SINGLE);
		fields.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	@Override
	protected String[] getQueryStrings()
	{
		if( isQueryType() )
		{
			return new String[] { textField.getText(), whereClause.getText(), fields.getText() };
		}
		return new String[] { fields.getText(), textField.getText(), whereClause.getText() };
	}

	@Override
	protected void setQueryFields(String[] queryStrings)
	{
		if( isQueryType() )
		{
			setupSearchFromStrings(queryStrings, 0);
			if( queryStrings.length > 2 )
			{
				fields.setText(queryStrings[2]);
			}
		}
		else
		{
			setupSearchFromStrings(queryStrings, 1);
			if( queryStrings.length > 0 )
			{
				fields.setText(queryStrings[0]);
			}
		}
	}

	private void setupSearchFromStrings(String[] queryStrings, int offset)
	{
		if( queryStrings.length > offset )
		{
			textField.setText(queryStrings[offset]);
		}
		if( queryStrings.length > (offset + 1) )
		{
			whereClause.setText(queryStrings[offset + 1]);
		}

	}

	@Override
	public void createPageCustomControl(Composite parent)
	{
		setControl(createPageControl(parent, "freetext"));
	}

	@Override
	protected void setTypeCombo(ComboViewer typeCombo)
	{
		typeCombo.add(new Object[] { new PrefixType(QUERY_PREFIX, "Query items"),
				new PrefixType(COUNT_PREFIX, "Count of items"), new PrefixType(FILES_PREFIX, "List files"),
				new PrefixType(MATRIX_PREFIX, "Matrix Search"), new PrefixType(MATRIX_COUNT_PREFIX, "Matrix Count"), });
		typeCombo.setSelection(new StructuredSelection(new PrefixType(getPrefixText(), "")));
	}

	@Override
	protected String getDefaultPrefix()
	{
		return QUERY_PREFIX;
	}

	private boolean isQueryType()
	{
		IStructuredSelection selection = (IStructuredSelection) typeComboViewer.getSelection();
		PrefixType prefix = (PrefixType) selection.getFirstElement();
		if( prefix != null )
		{
			String pfx = prefix.getPrefix();
			return pfx.equals(QUERY_PREFIX) || pfx.equals(COUNT_PREFIX) || pfx.equals(FILES_PREFIX);
		}
		return false;
	}
}