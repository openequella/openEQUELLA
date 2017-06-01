/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.engine.Mapping;
import org.hibernate.mapping.Column;
import org.hibernate.type.BasicType;

public interface ExtendedDialect
{
	String getDefaultSchema(Connection connection) throws SQLException;

	/**
	 * @param mapping
	 * @param column
	 * @param changeNotNull
	 * @param nullable Pass in null to use the annotation on the column. This is
	 *            not always possible (see Redmine #3329).
	 * @param changeType
	 * @return
	 */
	String getModifyColumnSql(Mapping mapping, Column column, boolean changeNotNull, boolean changeType);

	String getDropColumnSql(String table, Column column);

	String getDropIndexSql(String table, String indexName);

	boolean supportsAutoIndexForUniqueColumn();

	String getAddNotNullSql(Mapping mapping, Column column);

	String getRenameColumnSql(String table, Column column, String name);

	boolean supportsModifyWithConstraints();

	String getNameForMetadataQuery(String name, boolean quoted);

	boolean requiresAliasOnSubselect();

	boolean canRollbackSchemaChanges();

	String getDropConstraintsForColumnSql(String table, String columnName);

	boolean requiresNoConstraintsForModify();

	String getRandomIdentifier();

	String getDisplayNameForUrl(String url);

	/**
	 * This method is here because of a bug in hibernate. Types are cached
	 * before the dialect has a say in it, thus buggering up some of our Lob
	 * types.
	 * 
	 * @return
	 */
	Iterable<? extends BasicType> getExtraTypeOverrides();

	/**
	 * THe first element in the indexes array is the index name, all elements
	 * beyond are the column names to which the function applies
	 * 
	 * @param tableName
	 * @param indexes
	 * @return
	 */
	List<String> getCreateFunctionalIndex(String tableName, String function, String[]... indexes);
}
