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

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.common.interfaces.UuidReference;
import com.tle.core.guice.Bind;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.item.security.ItemSecurityConstants;
import com.tle.core.item.serializer.ItemSerializerProvider;
import com.tle.core.item.serializer.ItemSerializerState;
import com.tle.core.item.serializer.XMLStreamer;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.NavigationNodeBean;
import com.tle.web.api.item.interfaces.beans.NavigationTabBean;
import com.tle.web.api.item.interfaces.beans.NavigationTreeBean;

@Bind
@Singleton
@SuppressWarnings("nls")
public class NavigationNodeSerializerProvider implements ItemSerializerProvider
{
	private static final String KEY_NAVIGATION_NODES = "nodes";

	private static final String CATEGORY_NAVIGATION = "navigation";

	private static final String ALIAS_MANUAL_NAVIGATION = "manualnav";
	private static final String ALIAS_SHOW_SPLIT = "showsplit";

	// @Inject
	// private ItemNavigationNodeSerializer serialiser;
	@Inject
	private ItemDao itemDao;

	@Override
	public void prepareItemQuery(ItemSerializerState state)
	{
		if( state.hasCategory(CATEGORY_NAVIGATION) )
		{
			state.addPrivilege(ItemSecurityConstants.VIEW_ITEM);
			final ProjectionList projection = state.getItemProjection();
			projection.add(Projections.property("navigationSettings.manualNavigation"), ALIAS_MANUAL_NAVIGATION);
			projection.add(Projections.property("navigationSettings.showSplitOption"), ALIAS_SHOW_SPLIT);
		}
	}

	@Override
	public void performAdditionalQueries(ItemSerializerState state)
	{
		if( state.hasCategory(CATEGORY_NAVIGATION) )
		{
			ListMultimap<Long, ItemNavigationNode> nodes = itemDao
				.getNavigationNodesForItemIds(state.getItemIdsWithPrivilege(ItemSecurityConstants.VIEW_ITEM));
			for( Long itemId : nodes.keySet() )
			{
				state.setData(itemId, KEY_NAVIGATION_NODES, nodes.get(itemId));
			}
		}
	}

	@Override
	public void writeXmlResult(XMLStreamer xml, ItemSerializerState state, long itemId)
	{
		if( state.hasCategory(CATEGORY_NAVIGATION) )
		{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void writeItemBeanResult(EquellaItemBean equellaItemBean, ItemSerializerState state, long itemId)
	{
		if( state.hasCategory(CATEGORY_NAVIGATION) && state.hasPrivilege(itemId, ItemSecurityConstants.VIEW_ITEM) )
		{
			final Collection<ItemNavigationNode> navigaton = state.getData(itemId, KEY_NAVIGATION_NODES);
			final boolean manualNavigation = (Boolean) state.getData(itemId, ALIAS_MANUAL_NAVIGATION);
			final boolean showSplit = (Boolean) state.getData(itemId, ALIAS_SHOW_SPLIT);

			final List<NavigationNodeBean> rootNodes = Lists.newArrayList();

			final NavigationTreeBean tree = new NavigationTreeBean();
			// TODO: add a link
			tree.setHideUnreferencedAttachments(manualNavigation);
			tree.setShowSplitOption(showSplit);
			tree.setNodes(rootNodes);

			final ListMultimap<String, NavigationNodeBean> childMap = ArrayListMultimap.create();
			final List<NavigationNodeBean> allNodeBeans = Lists.newArrayList();

			if( navigaton != null )
			{
				for( ItemNavigationNode node : navigaton )
				{
					final NavigationNodeBean bean = new NavigationNodeBean();
					// TODO: add a link
					bean.setUuid(node.getUuid());
					bean.setImsId(node.getIdentifier());
					bean.setIcon(node.getIcon());
					bean.setName(node.getName());

					final List<ItemNavigationTab> tabs = node.getTabs();
					if( tabs != null )
					{
						final List<NavigationTabBean> tabBeans = Lists.newArrayList();
						for( ItemNavigationTab tab : tabs )
						{
							final NavigationTabBean tabBean = new NavigationTabBean();
							// TODO: add a link
							tabBean.setName(tab.getName());
							tabBean.setViewer(tab.getViewer());

							// FIXME: what do we do here? It *is* just a
							// reference
							final Attachment attachment = tab.getAttachment();
							if( attachment != null )
							{
								// TODO: add a link
								tabBean.setAttachment(new UuidReference(attachment.getUuid()));
							}
							tabBeans.add(tabBean);
						}
						bean.setTabs(tabBeans);
					}

					final ItemNavigationNode parent = node.getParent();
					if( parent != null )
					{
						childMap.put(parent.getUuid(), bean);
					}
					else
					{
						rootNodes.add(bean);
					}
					allNodeBeans.add(bean);
				}
			}

			for( NavigationNodeBean nodeBean : allNodeBeans )
			{
				nodeBean.setNodes(childMap.get(nodeBean.getUuid()));
			}

			equellaItemBean.setNavigation(tree);
		}
	}
}
