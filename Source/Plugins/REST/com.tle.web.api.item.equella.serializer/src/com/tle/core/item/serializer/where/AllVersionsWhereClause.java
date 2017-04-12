package com.tle.core.item.serializer.where;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.tle.core.item.serializer.ItemSerializerState;
import com.tle.core.item.serializer.ItemSerializerWhere;

@SuppressWarnings("nls")
public class AllVersionsWhereClause implements ItemSerializerWhere
{
	private static final String PROPERTY_VERSION = "i.version";
	public static final String ALIAS_VERSION = "version";

	private String uuid;

	public AllVersionsWhereClause(String uuid)
	{
		this.uuid = uuid;
	}

	@Override
	public void addWhere(ItemSerializerState state)
	{
		ProjectionList projections = state.getItemProjection();
		DetachedCriteria criteria = state.getItemQuery();
		criteria.add(Restrictions.eq("uuid", uuid));
		criteria.addOrder(Order.asc(PROPERTY_VERSION));
		projections.add(Projections.property(PROPERTY_VERSION), ALIAS_VERSION);
	}
}
