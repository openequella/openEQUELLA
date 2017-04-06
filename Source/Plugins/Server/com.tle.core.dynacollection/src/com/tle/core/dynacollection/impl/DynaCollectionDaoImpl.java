package com.tle.core.dynacollection.impl;

import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;

import com.tle.beans.entity.DynaCollection;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.dynacollection.DynaCollectionDao;
import com.tle.core.guice.Bind;

@Bind(DynaCollectionDao.class)
@Singleton
@SuppressWarnings("nls")
public class DynaCollectionDaoImpl extends AbstractEntityDaoImpl<DynaCollection> implements DynaCollectionDao
{
	public DynaCollectionDaoImpl()
	{
		super(DynaCollection.class);
	}

	@Override
	public List<DynaCollection> enumerateForUsage(final String usage)
	{
		return enumerateAll(new ListCallback()
		{
			@Override
			public String getAdditionalWhere()
			{
				return ":usage in elements(usageIds)";
			}

			@Override
			public void processQuery(Query query)
			{
				query.setParameter("usage", usage);
			}

			@Override
			public String getAdditionalJoins()
			{
				return null;
			}

			@Override
			public boolean isDistinct()
			{
				return false;
			}

			@Override
			public String getOrderBy()
			{
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<DynaCollection> getDynaCollectionsReferencingItemDefinition(ItemDefinition itemDefinition)
	{
		return getHibernateTemplate().findByNamedParam(
			"FROM DynaCollection d JOIN d.itemDefs i WHERE i.entity = :entity", "entity", itemDefinition);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<DynaCollection> getDynaCollectionsReferencingSchema(Schema schema)
	{
		return getHibernateTemplate().findByNamedParam(
			"FROM DynaCollection d JOIN d.schemas s WHERE s.entity = :entity", "entity", schema);
	}
}
