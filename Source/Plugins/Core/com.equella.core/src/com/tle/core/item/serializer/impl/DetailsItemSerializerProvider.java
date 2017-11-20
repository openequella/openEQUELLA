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

package com.tle.core.item.serializer.impl;

import static com.tle.core.item.serializer.ItemSerializerService.CATEGORY_DETAIL;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.core.guice.Bind;
import com.tle.core.item.serializer.ItemSerializerProvider;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.core.item.serializer.ItemSerializerState;
import com.tle.core.item.serializer.XMLStreamer;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.HistoryEventBean;
import com.tle.web.api.item.interfaces.beans.ItemExportBean;
import com.tle.web.api.item.interfaces.beans.ItemLockBean;

@SuppressWarnings("nls")
@Bind
@Singleton
public class DetailsItemSerializerProvider implements ItemSerializerProvider
{
	private static final String CREATED_PROPERTY = "dateCreated";
	private static final String MODIFIED_PROPERTY = "dateModified";
	private static final String RATING_PROPERTY = "rating";
	private static final String THUMBNAIL_PROPERTY = "thumb";

	@Inject
	private ItemSerializerService itemSerializerService;

	@Override
	public void prepareItemQuery(ItemSerializerState state)
	{
		if( state.hasCategory(CATEGORY_DETAIL) )
		{
			final ProjectionList projection = state.getItemProjection();
			state.addOwnerQuery();
			state.addStatusQuery();
			state.addCollectionQuery();
			projection.add(Projections.property(CREATED_PROPERTY), CREATED_PROPERTY);
			projection.add(Projections.property(MODIFIED_PROPERTY), MODIFIED_PROPERTY);
			projection.add(Projections.property(RATING_PROPERTY), RATING_PROPERTY);
			projection.add(Projections.property(THUMBNAIL_PROPERTY), THUMBNAIL_PROPERTY);
		}
	}

	@Override
	public void performAdditionalQueries(ItemSerializerState state)
	{
		// none - done in collabs
	}

	@Override
	public void writeXmlResult(XMLStreamer xml, ItemSerializerState state, long itemId)
	{
		if( state.hasCategory(CATEGORY_DETAIL) )
		{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void writeItemBeanResult(EquellaItemBean equellaItemBean, ItemSerializerState state, long itemId)
	{
		if( state.hasCategory(CATEGORY_DETAIL) )
		{
			equellaItemBean.setOwner(new UserBean((String) state.getData(itemId, ItemSerializerState.OWNER_ALIAS)));
			equellaItemBean.setStatus(((String) state.getData(itemId, ItemSerializerState.STATUS_ALIAS)).toLowerCase());
			equellaItemBean.setModifiedDate((Date) state.getData(itemId, MODIFIED_PROPERTY));
			equellaItemBean.setCreatedDate((Date) state.getData(itemId, CREATED_PROPERTY));
			equellaItemBean.setRating((Float) state.getData(itemId, RATING_PROPERTY));
			String collectionUuid = state.getData(itemId, ItemSerializerState.COLLECTIONUUID_ALIAS);
			equellaItemBean.setCollection(new BaseEntityReference(collectionUuid));
			equellaItemBean.setThumbnail((String) state.getData(itemId, THUMBNAIL_PROPERTY));

			final List<UserBean> collabBeans = Lists.newArrayList();
			final Collection<String> collabs = state.getData(itemId, ItemSerializerState.COLLAB_ALIAS);
			if( collabs != null )
			{
				for( String collab : collabs )
				{
					collabBeans.add(new UserBean(collab));
				}
			}
			equellaItemBean.setCollaborators(collabBeans);
		}
		if( state.isExport() )
		{
			ItemExportBean exportBean = itemSerializerService.getExportDetails(equellaItemBean);
			List<HistoryEventBean> history = itemSerializerService.getHistory(equellaItemBean.getUuid(),
				equellaItemBean.getVersion());
			exportBean.setHistory(history);
			ItemLockBean itemLockBean = itemSerializerService.getItemLock(equellaItemBean);
			if( itemLockBean != null )
			{
				exportBean.setLock(itemLockBean);
			}
			equellaItemBean.setExportDetails(exportBean);
		}
	}
}
