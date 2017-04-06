package com.tle.reporting;

import java.io.Serializable;
import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

/**
 * @author nread
 */
public class MetadataBean implements Serializable, IResultSetMetaData, IParameterMetaData
{
	private static final long serialVersionUID = -10102102;

	private final List<Definition> definitions = new ArrayList<Definition>();

	public MetadataBean()
	{
		super();
	}

	public MetadataBean(ResultSetMetaData rsmd) throws SQLException
	{
		if( rsmd != null )
		{
			final int count = rsmd.getColumnCount();
			for( int i = 1; i <= count; i++ )
			{
				Definition def = new Definition();
				def.setDiplayLength(rsmd.getColumnDisplaySize(i));
				def.setLabel(rsmd.getColumnLabel(i));
				def.setName(rsmd.getColumnName(i));
				def.setNullable(rsmd.isNullable(i));
				def.setPrecision(rsmd.getPrecision(i));
				def.setScale(rsmd.getScale(i));
				def.setType(rsmd.getColumnType(i));
				def.setTypename(rsmd.getColumnTypeName(i));

				addDefinition(def);
			}
		}
	}

	public MetadataBean(ParameterMetaData pmd) throws SQLException
	{
		if( pmd != null )
		{
			final int count = pmd.getParameterCount();
			for( int i = 1; i <= count; i++ )
			{
				Definition def = new Definition();
				try
				{
					def.setMode(pmd.getParameterMode(i));
					def.setNullable(pmd.isNullable(i));
					def.setPrecision(pmd.getPrecision(i));
					def.setScale(pmd.getScale(i));
					def.setType(pmd.getParameterType(i));
					def.setTypename(pmd.getParameterTypeName(i));
				}
				catch( SQLException sqle )
				{
					// ignore errors for oracle
				}

				addDefinition(def);
			}
		}
	}

	private Definition getDefinition(int index)
	{
		// Indexes are 1-based in the land of ODA.
		return definitions.get(index - 1);
	}

	public void addDefinition(Definition d)
	{
		definitions.add(d);
	}

	public static class Definition implements Serializable
	{
		private static final long serialVersionUID = -10102103;

		private int diplayLength;
		private String label;
		private String name;
		private int mode;
		private int type;
		private String typename;
		private int precision;
		private int scale;
		private int nullable;

		public Definition()
		{
			super();
		}

		public int getDiplayLength()
		{
			return diplayLength;
		}

		public void setDiplayLength(int diplayLength)
		{
			this.diplayLength = diplayLength;
		}

		public String getLabel()
		{
			return label;
		}

		public void setLabel(String label)
		{
			this.label = label;
		}

		public int getMode()
		{
			return mode;
		}

		public void setMode(int mode)
		{
			this.mode = mode;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public int getNullable()
		{
			return nullable;
		}

		public void setNullable(int nullable)
		{
			this.nullable = nullable;
		}

		public int getPrecision()
		{
			return precision;
		}

		public void setPrecision(int precision)
		{
			this.precision = precision;
		}

		public int getScale()
		{
			return scale;
		}

		public void setScale(int scale)
		{
			this.scale = scale;
		}

		public int getType()
		{
			return type;
		}

		public void setType(int type)
		{
			this.type = type;
		}

		public String getTypename()
		{
			return typename;
		}

		public void setTypename(String typename)
		{
			this.typename = typename;
		}
	}

	public int getColumnCount() throws OdaException
	{
		return definitions.size();
	}

	public int getColumnDisplayLength(int index) throws OdaException
	{
		return getDefinition(index).getDiplayLength();
	}

	public String getColumnLabel(int index) throws OdaException
	{
		return getDefinition(index).getLabel();
	}

	public String getColumnName(int index) throws OdaException
	{
		return getDefinition(index).getName();
	}

	public int getColumnType(int index) throws OdaException
	{
		return getDefinition(index).getType();
	}

	public String getColumnTypeName(int index) throws OdaException
	{
		return getDefinition(index).getTypename();
	}

	public int getPrecision(int index) throws OdaException
	{
		return getDefinition(index).getPrecision();
	}

	public int getScale(int index) throws OdaException
	{
		return getDefinition(index).getScale();
	}

	public int isNullable(int index) throws OdaException
	{
		return getDefinition(index).getNullable();
	}

	public int getParameterCount() throws OdaException
	{
		return definitions.size();
	}

	public int getParameterMode(int index) throws OdaException
	{
		return getDefinition(index).getMode();
	}

	public String getParameterName(int index) throws OdaException
	{
		return getDefinition(index).getName();
	}

	public int getParameterType(int index) throws OdaException
	{
		return getDefinition(index).getType();
	}

	public String getParameterTypeName(int index) throws OdaException
	{
		return getDefinition(index).getTypename();
	}
}
