package com.tle.reporting.oda.connectors.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.datatools.connectivity.oda.design.DataSetDesign;
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
import com.tle.reporting.oda.ui.jdbc.ui.provider.JdbcMetaDataProvider;
import com.tle.reporting.oda.ui.jdbc.ui.util.ExceptionHandler;

/**
 * @author Aaron
 */
public class ConnectorsListEditorPage extends SimpleEditorPage
{
	private static final String CONNECTOR_ID = "ci2:";

	private DataSetDesign dataSetDesign;

	public ConnectorsListEditorPage(String arg0)
	{
		super(arg0);
	}

	public ConnectorsListEditorPage(String arg0, String arg1, ImageDescriptor arg2)
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
		groupLayout.verticalSpacing = 10;
		group.setLayout(groupLayout);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);

		createExtraFields(group);
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

	@Override
	protected void cleanup()
	{
		JdbcMetaDataProvider.release();
		dataSetDesign = null;
	}
}
