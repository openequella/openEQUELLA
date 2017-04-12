package com.tle.reporting.oda.ui;

import java.util.Properties;

import org.eclipse.datatools.connectivity.oda.design.Property;

public class MetadataProvider
{
	// private final Driver driver;
	// private Connection connection;
	// private LearningEdgeOdaDelegate delegate;
	// private Map<String, ?> datasourceMetadata;
	//
	// public MetadataProvider(Driver driver)
	// {
	// this.driver = driver;
	// }
	//
	// @SuppressWarnings("unchecked")
	// public List<String[]> getAlltables(String namePattern, String[]
	// tableType)
	// {
	// List<String[]> tables = new ArrayList<String[]>();
	// if( tableType == null )
	// {
	// tableType = new String[]{"TABLE", "VIEW"};
	// }
	// for( String type : tableType )
	// {
	// Map<String, List<String>> tableMap = null;
	// if( type.equals("TABLE") )
	// {
	// tableMap = (Map<String, List<String>>) datasourceMetadata
	// .get(Constants.JDBC_TABLE_MAP);
	// }
	// if( type.equals("VIEW") )
	// {
	// tableMap = (Map<String, List<String>>) datasourceMetadata
	// .get(Constants.JDBC_VIEW_MAP);
	// }
	// if( tableMap != null )
	// {
	// for( String tableName : tableMap.keySet() )
	// {
	// tables.add(new String[]{tableName, type});
	// }
	// }
	// }
	// Collections.sort(tables, new Comparator<String[]>()
	// {
	// @Override
	// public int compare(String[] o1, String[] o2)
	// {
	// return o1[0].compareTo(o2[0]);
	// }
	// });
	// return tables;
	// }
	//
	// public void connect(DataSourceDesign dataSourceDesign)
	// {
	// try
	// {
	// connection = (Connection) driver.getConnection(dataSourceDesign
	// .getOdaExtensionDataSourceId());
	// connection.open(convert(dataSourceDesign.getPublicProperties()));
	// delegate = connection.getDelegate();
	// datasourceMetadata = delegate.getDatasourceMetadata("JDBC");
	// }
	// catch( Exception e )
	// {
	// throw new RuntimeException(e);
	// }
	// }
	//
	// public void closeConnection()
	// {
	// try
	// {
	// connection.close();
	// }
	// catch( OdaException e )
	// {
	// throw new RuntimeException(e);
	// }
	// }
	//
	// @SuppressWarnings("unchecked")
	// public ArrayList getColumns(String tableName)
	// {
	// Map<String, List<String>> tableMap = (Map<String, List<String>>)
	// datasourceMetadata
	// .get(Constants.JDBC_TABLE_MAP);
	// List<String> cols = tableMap.get(tableName);
	// if( cols == null )
	// {
	// tableMap = (Map<String, List<String>>)
	// datasourceMetadata.get(Constants.JDBC_TABLE_MAP);
	// cols = tableMap.get(tableName);
	// }
	//
	// return new ArrayList(cols);
	// }
	//
	// public String getIdentifierQuoteString()
	// {
	// return (String)
	// datasourceMetadata.get(Constants.JDBC_IDENTIFIER_QUOTE_STRING);
	// }

	public static Properties convert(org.eclipse.datatools.connectivity.oda.design.Properties orig)
	{
		Properties results = new Properties();
		for( Object name2 : orig.getProperties() )
		{
			Property prop = (Property) name2;
			String name = prop.getName();
			String val = prop.getValue();
			if( val != null )
			{
				results.put(name, val);
			}
		}
		return results;
	}

}
