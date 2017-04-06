package com.tle.core.item.serializer.where;

import org.hibernate.criterion.Restrictions;

import com.tle.beans.item.ItemKey;
import com.tle.core.item.serializer.ItemSerializerState;
import com.tle.core.item.serializer.ItemSerializerWhere;

@SuppressWarnings("nls")
public class SingleItemWhereClause implements ItemSerializerWhere
{
	private ItemKey itemKey;

	public SingleItemWhereClause(ItemKey itemKey)
	{
		this.itemKey = itemKey;
	}

	@Override
	public void addWhere(ItemSerializerState state)
	{
		state.getItemQuery().add(
			Restrictions.and(Restrictions.eq("uuid", itemKey.getUuid()),
				Restrictions.eq("version", itemKey.getVersion())));
	}
}
