/*
 * Created on Oct 26, 2005
 */
package com.tle.core.schema.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import com.tle.beans.entity.Schema;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.schema.dao.SchemaDao;

@Bind(SchemaDao.class)
@Singleton
@SuppressWarnings("nls")
public class SchemaDaoImpl extends AbstractEntityDaoImpl<Schema> implements SchemaDao
{
	public SchemaDaoImpl()
	{
		super(Schema.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getExportSchemaTypes()
	{
		return getHibernateTemplate().find(
			"SELECT DISTINCT t.type FROM Schema s INNER JOIN s.expTransforms AS t WHERE s.institution = ? ORDER BY t.type",
			CurrentInstitution.get());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getImportSchemaTypes(long id)
	{
		return getHibernateTemplate().find(
			"select distinct t.type from Schema s inner join s.impTransforms as t where s.id = ? order by t.type", id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Schema> getSchemasForExportSchemaType(String type)
	{
		return getHibernateTemplate().find(
			"SELECT s FROM Schema s INNER JOIN s.expTransforms t WHERE s.institution = ? AND LOWER(t.type) = ?",
			new Object[]{CurrentInstitution.get(), type.toLowerCase()});
	}
}
