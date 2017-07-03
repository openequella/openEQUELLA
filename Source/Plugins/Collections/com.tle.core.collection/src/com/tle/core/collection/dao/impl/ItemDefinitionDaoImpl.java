/*
 * Created on Oct 26, 2005
 */
package com.tle.core.collection.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.collection.dao.ItemDefinitionDao;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.remoting.RemoteItemDefinitionService;

/**
 * @author Nicholas Read
 */
@Bind(ItemDefinitionDao.class)
@Singleton
@SuppressWarnings("nls")
public class ItemDefinitionDaoImpl extends AbstractEntityDaoImpl<ItemDefinition> implements ItemDefinitionDao
{
	public ItemDefinitionDaoImpl()
	{
		super(ItemDefinition.class);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.dao.ItemDefinitionDao#findByType(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ItemDefinition> findByType(String type)
	{
		return getHibernateTemplate().findByNamedParam("from ItemDefinition where type = :type", "type", type);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.dao.ItemDefinitionDao#listAllForSchema(java.lang.String)
	 */
	@Override
	public List<BaseEntityLabel> listAllForSchema(final long schemaID)
	{
		return listAll(RemoteItemDefinitionService.ENTITY_TYPE, new ListCallback()
		{
			@Override
			public String getAdditionalWhere()
			{
				return "be.schema.id = :schemaID";
			}

			@Override
			public void processQuery(Query query)
			{
				query.setParameter("schemaID", schemaID);
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

	/**
	 * @deprecated Use an event to ask for reference
	 */
	@Override
	@Deprecated
	@SuppressWarnings("unchecked")
	public List<Class<?>> getReferencingClasses(long id)
	{
		List<Class<?>> usage = new ArrayList<Class<?>>();
		if( ((List<Long>) getHibernateTemplate().find("select count(*) from Item where itemDefinition.id = ?", id))
			.get(0) != 0 )
		{
			usage.add(Item.class);
		}
		return usage;
	}

	@Override
	public ItemDefinition getByItemId(ItemKey itemId)
	{
		List<Object> results = getHibernateTemplate().findByNamedParam(
			"select i.itemDefinition from Item i where i.uuid = :uuid and i.version = :version and i.institution = :institution",
			new String[]{"uuid", "version", "institution"},
			new Object[]{itemId.getUuid(), itemId.getVersion(), CurrentInstitution.get()});
		int size = results.size();
		if( size == 0 )
		{
			return null;
		}
		if( size > 1 )
		{
			throw new Error("Expected a unique result for item ID " + itemId);
		}
		return (ItemDefinition) results.get(0);
	}
}
