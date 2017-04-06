package com.tle.reporting.oda.connectors.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.datatools.connectivity.oda.design.DataElementAttributes;
import org.eclipse.datatools.connectivity.oda.design.DataSetDesign;
import org.eclipse.datatools.connectivity.oda.design.DataSetParameters;
import org.eclipse.datatools.connectivity.oda.design.ParameterDefinition;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import com.tle.reporting.oda.connectors.ConnectorMetaDataProvider;
import com.tle.reporting.oda.ui.SimpleEditorPage;
import com.tle.reporting.oda.ui.TLEOdaPlugin;
import com.tle.reporting.oda.ui.jdbc.ui.util.ExceptionHandler;

/**
 * @author Aaron
 */
public class ConnectorsEditorPage extends SimpleEditorPage
{
	private static final String CONNECTOR_ID = "ci:";

	private DataSetDesign dataSetDesign;

	public ConnectorsEditorPage(String arg0)
	{
		super(arg0);
	}

	public ConnectorsEditorPage(String arg0, String arg1, ImageDescriptor arg2)
	{
		super(arg0, arg1, arg2);
	}

	@Override
	public void createPageCustomControl(Composite parent)
	{
		this.dataSetDesign = this.getInitializationDesign();
		prepareConnectorMetaDataProvider(dataSetDesign);

		setControl(createPageControl(parent, "connector"));
	}

	@Override
	protected Control createPageControl(Composite parent, String datatype)
	{
		Composite group = new Composite(parent, SWT.FILL);

		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = 2;
		// groupLayout.horizontalSpacing = 10;
		groupLayout.verticalSpacing = 10;
		group.setLayout(groupLayout);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);

		// Type Combo
		// Label selectTypeLabel = new Label(group, SWT.LEFT);
		// selectTypeLabel.setText(TLEOdaPlugin.getResourceString(datatype +
		// ".label.querytype"));
		// typeComboViewer = new ComboViewer(group, SWT.READ_ONLY);
		// typeComboViewer.getControl().setLayoutData(new
		// GridData(GridData.FILL_HORIZONTAL));

		createExtraFields(group);
		// setTypeCombo(typeComboViewer);
		setQueryFields(getSavedQueryStrings());
		return group;
	}

	@Override
	protected void setTypeCombo(ComboViewer typeCombo)
	{
		Map<String, String> connectors = ConnectorMetaDataProvider.getInstance().getAllConnectors();
		List<Object> connectorList = new ArrayList<Object>();
		for( Map.Entry<String, String> connector : connectors.entrySet() )
		{
			connectorList.add(new PrefixType(CONNECTOR_ID + connector.getKey(), connector.getValue()));
		}

		typeCombo.add(connectorList.toArray());
		typeCombo.setSelection(new StructuredSelection(new PrefixType(getPrefixText(), null)));
	}

	@Override
	protected void setQueryFields(String[] queryStrings)
	{
	}

	@Override
	protected String createQueryText(DataSetDesign design)
	{
		// query,connectorId,courseId,folderId,hidden
		DataSetParameters parameters = design.getParameters();
		if( parameters != null )
		{
			StringBuilder query = new StringBuilder();
			boolean first = true;
			for( ParameterDefinition def : parameters.getParameterDefinitions() )
			{
				DataElementAttributes attributes = def.getAttributes();
				String name = attributes.getName();
				if( !first )
				{
					query.append(DIVIDER);
				}
				query.append(name);
				first = false;
			}
			return query.toString();
		}
		return "";
	}

	@Override
	protected String[] getQueryStrings()
	{
		return new String[0];
	}

	@Override
	protected String getDefaultPrefix()
	{
		return CONNECTOR_ID;
	}

	private void prepareConnectorMetaDataProvider(DataSetDesign dsd)
	{
		ConnectorMetaDataProvider.createInstance(dsd);
		try
		{
			ConnectorMetaDataProvider.getInstance().reconnect();
		}
		catch( Exception e )
		{
			ExceptionHandler.showException(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
				TLEOdaPlugin.getResourceString2("exceptionHandler.title.error"), e.getLocalizedMessage(), e);
		}
	}

	// @Override
	// protected DataSetDesign collectDataSetDesign(DataSetDesign design)
	// {
	// IParameterMetaData xxx = null;
	// DataSetParameters paramDesign;
	// try
	// {
	// paramDesign = DesignSessionUtil.toDataSetParametersDesign(xxx,
	// DesignSessionUtil.toParameterModeDesign(
	// IParameterMetaData.parameterModeIn ) );
	//
	// for( ParameterDefinition paramDef : paramDesign.getParameterDefinitions()
	// ) {
	// System.out.println("Parameter summary");
	// System.out.println("getDefaultScalarValue = " +
	// paramDef.getDefaultScalarValue() );
	// System.out.println("getDefaultValueCount  = " +
	// paramDef.getDefaultValueCount() );
	// System.out.println("toString              = " + paramDef.toString() );
	//
	// if( paramDef.getDefaultScalarValue() == null )
	// {
	// // Timestamp ts = new Timestamp(
	// GregorianCalendar.getInstance().getTimeInMillis() );
	// // paramDef.setDefaultScalarValue( ts.toGMTString() );
	// // System.out.println("Set default scalar value");
	//
	// }
	// }
	//
	// }
	// catch( OdaException e )
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// return super.collectDataSetDesign(design);
	// }

	@Override
	protected void proccessParamDefn(ParameterDefinition defn, DataSetParameters parameters)
	{
		DataElementAttributes attributes = defn.getAttributes();
		String name = attributes.getName();

		// see
		// com.tle.core.connectors.reporting.ConnectorQueryDelegate#getParameterMetadata
		String defaultValue = defn.getDefaultScalarValue();
		if( defaultValue == null || defaultValue.equals("") )
		{
			if( name.equals("query") )
			{
				defn.setDefaultScalarValue("*");
			}
			if( name.equals("connectorId") )
			{
				defn.setDefaultScalarValue("all");
			}
			if( name.equals("courseId") )
			{
				defn.setDefaultScalarValue("all");
			}
			if( name.equals("folderId") )
			{
				defn.setDefaultScalarValue("all");
			}
			if( name.equals("hidden") )
			{
				defn.setDefaultScalarValue("0");
			}
		}
	}

	@Override
	protected void cleanup()
	{
		ConnectorMetaDataProvider.release();
		dataSetDesign = null;
	}
}
