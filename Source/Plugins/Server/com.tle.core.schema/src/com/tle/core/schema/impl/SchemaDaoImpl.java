/*
 * Created on Oct 26, 2005
 */
package com.tle.core.schema.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.schema.SchemaDao;
import com.tle.core.user.CurrentInstitution;

@Bind(SchemaDao.class)
@Singleton
@SuppressWarnings("nls")
public class SchemaDaoImpl extends AbstractEntityDaoImpl<Schema> implements SchemaDao
{
	public SchemaDaoImpl()
	{
		super(Schema.class);
	}

	/**
	 * @deprecated Use an event to ask for reference
	 */
	@Override
	@Deprecated
	public List<Class<?>> getReferencingClasses(long id)
	{
		List<Class<?>> usage = new ArrayList<Class<?>>();
		if( (Long) getHibernateTemplate().findByNamedParam(
			"select count(*) from ItemDefinition i where i.schema.id = :id", "id", id).get(0) > 0 )
		{
			usage.add(ItemDefinition.class);
		}
		if( (Long) getHibernateTemplate().findByNamedParam(
			"select count(*) from PowerSearch p where p.schema.id = :id", "id", id).get(0) > 0 )
		{
			usage.add(PowerSearch.class);
		}
		return usage;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getExportSchemaTypes()
	{
		return getHibernateTemplate()
			.find(
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
