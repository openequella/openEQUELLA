package com.tle.hibernate.dialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.hibernate.dialect.Oracle9iDialect;
import org.hibernate.engine.Mapping;
import org.hibernate.mapping.Column;
import org.hibernate.type.BasicType;
import org.hibernate.type.StandardBasicTypes;

import com.tle.core.hibernate.ExtendedDialect;

public class ExtendedOracle9iDialect extends Oracle9iDialect implements ExtendedDialect
{

	private final ExtendedOracle10gDialect tenG;

	public ExtendedOracle9iDialect()
	{
		tenG = new ExtendedOracle10gDialect();
		registerHibernateType(Types.CLOB, StandardBasicTypes.STRING.getName());
	}

	@Override
	public boolean canRollbackSchemaChanges()
	{
		return false;
	}

	@Override
	public String getModifyColumnSql(Mapping mapping, Column column, boolean changeNotNull, boolean changeType)
	{
		return tenG.getModifyColumnSql(mapping, column, changeNotNull, changeType);
	}

	@Override
	public String getDropColumnSql(String table, Column column)
	{
		return tenG.getDropColumnSql(table, column);
	}

	@Override
	public String getDropIndexSql(String table, String indexName)
	{
		return tenG.getDropIndexSql(table, indexName);
	}

	@Override
	public String getAddNotNullSql(Mapping mapping, Column column)
	{
		return tenG.getAddNotNullSql(mapping, column);
	}

	@Override
	public String getRenameColumnSql(String table, Column column, String name)
	{
		return tenG.getRenameColumnSql(table, column, name);
	}

	@Override
	public boolean supportsAutoIndexForUniqueColumn()
	{
		return true;
	}

	@Override
	public boolean supportsModifyWithConstraints()
	{
		return true;
	}

	@Override
	public String getNameForMetadataQuery(String name, boolean quoted)
	{
		return tenG.getNameForMetadataQuery(name, quoted);
	}

	@Override
	public boolean requiresAliasOnSubselect()
	{
		return false;
	}

	@Override
	public boolean requiresNoConstraintsForModify()
	{
		return false;
	}

	@Override
	public String getDropConstraintsForColumnSql(String table, String columnName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRandomIdentifier()
	{
		return tenG.getRandomIdentifier();
	}

	@Override
	public String getDefaultSchema(Connection connection) throws SQLException
	{
		return tenG.getDefaultSchema(connection);
	}

	@Override
	public String getDisplayNameForUrl(String url)
	{
		return tenG.getDisplayNameForUrl(url);
	}

	@Override
	public Iterable<? extends BasicType> getExtraTypeOverrides()
	{
		return tenG.getExtraTypeOverrides();
	}

	@Override
	public List<String> getCreateFunctionalIndex(String tableName, String function, String[]... indexes)
	{
		return tenG.getCreateFunctionalIndex(tableName, function, indexes);
	}

}
