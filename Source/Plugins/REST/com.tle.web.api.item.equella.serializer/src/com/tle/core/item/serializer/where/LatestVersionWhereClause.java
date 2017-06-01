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

package com.tle.core.item.serializer.where;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.tle.beans.item.ItemStatus;
import com.tle.core.item.serializer.ItemSerializerState;
import com.tle.core.item.serializer.ItemSerializerWhere;

@SuppressWarnings("nls")
public class LatestVersionWhereClause implements ItemSerializerWhere
{
	private static final String PROPERTY_VERSION = "i.version";
	public static final String ALIAS_VERSION = "version";

	private String uuid;
	private boolean live;

	public LatestVersionWhereClause(String uuid, boolean live)
	{
		this.uuid = uuid;
		this.live = live;
	}

	@Override
	public void addWhere(ItemSerializerState state)
	{
		DetachedCriteria criteria = state.getItemQuery();
		ProjectionList projections = state.getItemProjection();
		criteria.add(Restrictions.eq("uuid", uuid));
		if( live )
		{
			criteria.add(Restrictions.eq("status", ItemStatus.LIVE.name()));
		}
		criteria.addOrder(Order.desc(PROPERTY_VERSION));
		projections.add(Projections.property(PROPERTY_VERSION), ALIAS_VERSION);
		state.setMaxResults(1);
	}
}
