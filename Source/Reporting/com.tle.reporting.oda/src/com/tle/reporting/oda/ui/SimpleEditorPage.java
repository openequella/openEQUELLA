package com.tle.reporting.oda.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Types;
import java.util.Iterator;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDriver;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.design.DataSetDesign;
import org.eclipse.datatools.connectivity.oda.design.DataSetParameters;
import org.eclipse.datatools.connectivity.oda.design.DesignFactory;
import org.eclipse.datatools.connectivity.oda.design.ParameterDefinition;
import org.eclipse.datatools.connectivity.oda.design.ParameterMode;
import org.eclipse.datatools.connectivity.oda.design.ResultSetColumns;
import org.eclipse.datatools.connectivity.oda.design.ResultSetDefinition;
import org.eclipse.datatools.connectivity.oda.design.ui.designsession.DesignSessionUtil;
import org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSetWizardPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.tle.reporting.oda.Driver;

public abstract class SimpleEditorPage extends DataSetWizardPage
{
	private DataSetDesign dataSetDesign;
	protected ComboViewer typeComboViewer;
	protected Text textField;
	protected static final String DIVIDER = "|";
	private static final String DIVIDER_REGEX = "\\|";

	public SimpleEditorPage(String arg0, String arg1, ImageDescriptor arg2)
	{
		super(arg0, arg1, arg2);
	}

	public SimpleEditorPage(String arg0)
	{
		super(arg0);
	}

	protected abstract void setTypeCombo(ComboViewer typeCombo);

	protected String createQueryText(DataSetDesign design)
	{
		if( typeComboViewer != null )
		{
			StringBuilder sbuf = new StringBuilder();
			IStructuredSelection selection = (IStructuredSelection) typeComboViewer.getSelection();
			if( selection.size() > 0 )
			{
				PrefixType firstElement = (PrefixType) selection.getFirstElement();
				sbuf.append(firstElement.getPrefix());
				String[] queries = getQueryStrings();
				try
				{
					boolean first = true;
					for( String queryText : queries )
					{
						if( !first )
						{
							sbuf.append(DIVIDER);
						}
						else
						{
							first = false;
						}
						sbuf.append(URLEncoder.encode(queryText, "UTF-8"));
					}
				}
				catch( UnsupportedEncodingException e )
				{
					throw new RuntimeException(e);
				}
				return sbuf.toString();
			}
		}

		// if the control hasn't been loaded, use whatever is already in the
		// report!
		String designQuery = design.getQueryText();
		if( designQuery != null && designQuery.length() > 0 )
		{
			return designQuery;
		}

		return getDefaultPrefix();
	}

	protected String[] getQueryStrings()
	{
		return new String[]{textField.getText()};
	}

	protected String[] getSavedQueryStrings()
	{
		String query = getQueryText();
		int i = query.indexOf(':');
		query = query.substring(i + 1);

		try
		{
			String[] queryStrings = query.split(DIVIDER_REGEX);
			i = 0;
			for( String string : queryStrings )
			{
				queryStrings[i++] = URLDecoder.decode(string, "UTF-8");
			}
			return queryStrings;
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
	}

	protected String getPrefixText()
	{
		String queryText = getQueryText();
		int i = queryText.indexOf(':');
		return queryText.substring(0, i + 1);
	}

	private String getQueryText()
	{
		String queryText = this.getDataSetDesign().getQueryText();
		if( queryText != null && queryText.trim().length() > 0 )
		{
			return queryText;
		}

		return getDefaultPrefix();
	}

	protected abstract String getDefaultPrefix();

	@Override
	protected DataSetDesign collectDataSetDesign(DataSetDesign design)
	{
		design.setQueryText(createQueryText(design));

		// obtain query's result set metadata, and update
		// the dataSetDesign with it
		IConnection conn = null;
		try
		{
			IDriver jdbcDriver = new Driver();

			conn = jdbcDriver.getConnection(design.getOdaExtensionDataSourceId());
			conn.open(MetadataProvider.convert(design.getDataSourceDesign().getPublicProperties()));

			IQuery query = conn.newQuery(design.getOdaExtensionDataSetId());
			query.setMaxRows(1);
			query.prepare(design.getQueryText());

			// set parameter metadata
			IParameterMetaData paramMetaData = query.getParameterMetaData();
			mergeParameterMetaData(design, paramMetaData);

			query.executeQuery();

			// set resultset metadata
			IResultSetMetaData metadata = query.getMetaData();
			setResultSetMetaData(design, metadata);
		}
		catch( OdaException e )
		{
			// no result set definition available, reset in dataSetDesign
			design.setResultSets(null);
		}
		finally
		{
			closeConnection(conn);
		}

		return design;
	}

	/**
	 * @param dataSetDesign
	 * @param md
	 * @throws OdaException
	 */
	private void setResultSetMetaData(DataSetDesign dataSetDesign, IResultSetMetaData md) throws OdaException
	{

		ResultSetColumns columns = DesignSessionUtil.toResultSetColumnsDesign(md);

		if( columns != null )
		{
			ResultSetDefinition resultSetDefn = DesignFactory.eINSTANCE.createResultSetDefinition();
			// jdbc does not support result set name
			resultSetDefn.setResultSetColumns(columns);
			// no exception; go ahead and assign to specified dataSetDesign
			dataSetDesign.setPrimaryResultSet(resultSetDefn);
			dataSetDesign.getResultSets().setDerivedMetaData(true);
		}
		else
		{
			dataSetDesign.setResultSets(null);
		}
	}

	/**
	 * close the connection
	 * 
	 * @param conn
	 */
	private void closeConnection(IConnection conn)
	{
		try
		{
			if( conn != null )
			{
				conn.close();
			}
		}
		catch( OdaException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private DataSetDesign getDataSetDesign()
	{
		if( dataSetDesign == null )
		{
			dataSetDesign = this.getInitializationDesign();
		}
		return dataSetDesign;
	}

	/**
	 * merge paramter meta data between dataParameter and datasetDesign's
	 * parameter.
	 * 
	 * @param dataSetDesign
	 * @param md
	 * @throws OdaException
	 */
	private void mergeParameterMetaData(DataSetDesign dataSetDesign, IParameterMetaData md) throws OdaException
	{
		DataSetParameters parameters = dataSetDesign.getParameters();
		DataSetParameters dataSetParameter = DesignSessionUtil.toDataSetParametersDesign(md, ParameterMode.IN_LITERAL);
		if( parameters == null || parameters.getParameterDefinitions().size() == 0 )
		{
			if( dataSetParameter != null )
			{
				Iterator iter = dataSetParameter.getParameterDefinitions().iterator();
				while( iter.hasNext() )
				{
					ParameterDefinition defn = (ParameterDefinition) iter.next();
					proccessParamDefn(defn, dataSetParameter);
				}
			}
			dataSetDesign.setParameters(dataSetParameter);
		}
		else
		{
			// FIXME: this logic looks bad... like... why is it using
			// dataParamSize inside the loop? It doesn't change

			if( dataSetParameter != null )
			{
				int designParamSize = dataSetParameter.getParameterDefinitions().size();
				int dataParamSize = parameters.getParameterDefinitions().size();
				while( designParamSize > dataParamSize )
				{
					ParameterDefinition defn = dataSetParameter.getParameterDefinitions().get(dataParamSize);

					proccessParamDefn(defn, parameters);
					parameters.getParameterDefinitions().add(defn);
					designParamSize--;
				}
			}
		}
	}

	/**
	 * Process the parameter definition for some special case
	 * 
	 * @param defn
	 * @param parameters
	 */
	protected void proccessParamDefn(ParameterDefinition defn, DataSetParameters parameters)
	{
		if( defn.getAttributes().getName() == null || defn.getAttributes().getName().trim().equals("") )
		{
			defn.getAttributes().setName(getUniqueName(parameters));
		}
		if( defn.getAttributes().getNativeDataTypeCode() == Types.NULL )
		{
			defn.getAttributes().setNativeDataTypeCode(Types.CHAR);
		}
	}

	/**
	 * Get a unique name for dataset parameter
	 * 
	 * @param parameters
	 * @return
	 */
	protected final String getUniqueName(DataSetParameters parameters)
	{
		int n = 1;
		String prefix = "param"; //$NON-NLS-1$
		StringBuffer buf = new StringBuffer();
		while( buf.length() == 0 )
		{
			buf.append(prefix).append(n++);
			if( parameters != null )
			{
				Iterator iter = parameters.getParameterDefinitions().iterator();
				if( iter != null )
				{
					while( iter.hasNext() && buf.length() > 0 )
					{
						ParameterDefinition parameter = (ParameterDefinition) iter.next();
						if( buf.toString().equalsIgnoreCase(parameter.getAttributes().getName()) )
						{
							buf.setLength(0);
						}
					}
				}
			}
		}
		return buf.toString();
	}

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
		Label selectTypeLabel = new Label(group, SWT.LEFT);
		selectTypeLabel.setText(TLEOdaPlugin.getResourceString(datatype + ".label.querytype"));
		typeComboViewer = new ComboViewer(group, SWT.READ_ONLY);
		typeComboViewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Query Textfield
		Label queryLabel = new Label(group, SWT.LEFT);
		queryLabel.setText(TLEOdaPlugin.getResourceString(datatype + ".label.query"));
		textField = new Text(group, SWT.BORDER | SWT.SINGLE);
		textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createExtraFields(group);
		setTypeCombo(typeComboViewer);
		setQueryFields(getSavedQueryStrings());
		return group;
	}

	protected void createExtraFields(Composite parent)
	{
		// nothing by default
	}

	protected void setQueryFields(String[] queryStrings)
	{
		textField.setText(queryStrings[0]);
	}

	public static class PrefixType
	{

		private final String prefix;
		private final String name;

		public PrefixType(String prefix, String name)
		{
			this.prefix = prefix;
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}

		public String getName()
		{
			return name;
		}

		public String getPrefix()
		{
			return prefix;
		}

		@Override
		public boolean equals(Object obj)
		{
			return ((PrefixType) obj).getPrefix().equals(prefix);
		}
	}

}
