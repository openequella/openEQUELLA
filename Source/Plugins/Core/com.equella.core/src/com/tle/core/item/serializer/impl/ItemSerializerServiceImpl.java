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

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriInfo;

import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSetMultimap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemLock;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.Check;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.service.LanguageService;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.item.security.SimpleItemSecurity;
import com.tle.core.item.serializer.ItemHistorySerializer;
import com.tle.core.item.serializer.ItemSerializerItemBean;
import com.tle.core.item.serializer.ItemSerializerProvider;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.core.item.serializer.ItemSerializerState;
import com.tle.core.item.serializer.ItemSerializerWhere;
import com.tle.core.item.serializer.ItemSerializerXml;
import com.tle.core.item.serializer.XMLStreamer;
import com.tle.core.item.serializer.where.ItemIdsWhereClause;
import com.tle.core.item.service.ItemHistoryService;
import com.tle.core.item.service.ItemLockingService;
import com.tle.core.item.service.ItemService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.TLEAclManager;
import com.tle.core.workflow.dao.WorkflowDao;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.ItemLockResource;
import com.tle.web.api.item.interfaces.beans.HistoryEventBean;
import com.tle.web.api.item.interfaces.beans.ItemExportBean;
import com.tle.web.api.item.interfaces.beans.ItemLockBean;
import com.tle.web.api.item.interfaces.beans.ItemNodeStatusExportBean;
import com.tle.web.api.item.interfaces.beans.ItemNodeStatusMessageBean;
import com.tle.web.api.item.interfaces.beans.ItemNodeStatusMessageBean.MessageType;
import com.tle.web.remoting.rest.service.UrlLinkService;

@Bind(ItemSerializerService.class)
@Singleton
@SuppressWarnings("nls")
public class ItemSerializerServiceImpl implements ItemSerializerService
{
	@Inject
	private ItemService itemService;
	@Inject
	private ItemLockingService lockingService;
	@Inject
	private UrlLinkService urlLinkService;
	@Inject
	private ItemHistoryService itemHistoryService;
	@Inject
	private ItemHistorySerializer itemHistorySerializer;
	@Inject
	private ItemDao itemDao;
	@Inject
	private LanguageService languageService;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private WorkflowDao workflowDao;
	@Inject
	private PluginTracker<ItemSerializerProvider> providerTracker;

	@Override
	public ItemSerializerXml createXmlSerializer(Collection<Long> itemIds, Collection<String> categories,
		String... privileges)
	{
		if( Check.isEmpty(itemIds) )
		{
			return EMPTY_ITEM_SERIALIZER_XML;
		}

		ItemSerializerState state = createState(new ItemIdsWhereClause(itemIds), categories, privileges, false, false);
		return new SimpleItemSerializerXml(state);
	}

	@Override
	public ItemSerializerItemBean createItemBeanSerializer(ItemSerializerWhere where, Collection<String> categories,
		boolean export, String... privileges)
	{
		return new SimpleItemSerializerItemBean(createState(where, categories, privileges, false, export));
	}

	@Override
	public ItemSerializerItemBean createItemBeanSerializer(Collection<Long> itemIds, Collection<String> categories,
		boolean ignorePriv, boolean export)
	{
		if( Check.isEmpty(itemIds) )
		{
			return EMPTY_ITEM_SERIALIZER_ITEM_BEAN;
		}

		return new SimpleItemSerializerItemBean(
			createState(new ItemIdsWhereClause(itemIds), categories, null, ignorePriv, export));
	}

	@Override
	public ItemSerializerItemBean createItemBeanSerializer(Collection<Long> itemIds, Collection<String> categories,
		boolean export, String... privileges)
	{
		if( Check.isEmpty(itemIds) )
		{
			return EMPTY_ITEM_SERIALIZER_ITEM_BEAN;
		}

		return new SimpleItemSerializerItemBean(
			createState(new ItemIdsWhereClause(itemIds), categories, privileges, false, export));
	}

	@Override
	public ItemExportBean getExportDetails(EquellaItemBean equellaBean)
	{
		ItemExportBean exportBean = new ItemExportBean();

		Item item = itemService.get(new ItemId(equellaBean.getUuid(), equellaBean.getVersion()));
		exportBean.setModerating(item.isModerating());

		ModerationStatus moderationStatus = item.getModeration();

		if( moderationStatus != null )
		{
			exportBean.setReviewDate(moderationStatus.getReviewDate());
			exportBean.setLastAction(moderationStatus.getLastAction());
			exportBean.setStart(moderationStatus.getStart());
			exportBean.setLiveApprovalDate(moderationStatus.getLiveApprovalDate());
			if( moderationStatus.getResumeStatus() != null )
			{
				exportBean.setResumeStatus(moderationStatus.getResumeStatus().toString());
			}
			if( moderationStatus.getDeletedStatus() != null )
			{
				exportBean.setDeletedStatus(moderationStatus.getDeletedStatus().toString());
			}
			exportBean.setRejectedMessage(moderationStatus.getRejectedMessage());
			exportBean.setRejectedBy(moderationStatus.getRejectedBy());
			exportBean.setRejectedStep(moderationStatus.getRejectedStep());
			exportBean.setUnarchiveModerating(moderationStatus.isUnarchiveModerating());
			exportBean.setDeletedModerating(moderationStatus.isDeletedModerating());
			exportBean.setNeedsReset(moderationStatus.isNeedsReset());
		}
		List<WorkflowItemStatus> itemStatuses = workflowDao.findWorkflowItemStatusesForItem(item);
		if( itemStatuses.size() > 0 )
		{
			List<ItemNodeStatusExportBean> statuses = Lists.newArrayList();
			for( Object collectionObj : itemStatuses )
			{
				WorkflowItemStatus itemStatus = (WorkflowItemStatus) collectionObj;

				ItemNodeStatusExportBean inseb = new ItemNodeStatusExportBean();
				inseb.setAcceptedUsers(itemStatus.getAcceptedUsers());
				inseb.setAssignedTo(itemStatus.getAssignedTo());
				if( itemStatus.getCause() != null )
				{
					long currentItemStatusCause = itemStatus.getCause().getId();
					// go through the itemStatuses List, and whichever - if any
					// - is the object identified by this ID, we preserve its
					// index as the value we place in this bean
					for( int indx = 0; indx < itemStatuses.size(); ++indx )
					{
						if( itemStatuses.get(indx).getId() == currentItemStatusCause )
						{
							inseb.setCauseIndex(indx);
							break; // from inner for loop
						}
					}
				}

				if( !Check.isEmpty(itemStatus.getComments()) )
				{
					List<ItemNodeStatusMessageBean> insmbs = Lists.newArrayList();
					for( WorkflowMessage wrkflwMsg : itemStatus.getComments() )
					{
						UserBean ub = new UserBean(wrkflwMsg.getUser());
						MessageType msgType = null;
						switch( wrkflwMsg.getType() )
						{
							case 'a':
								msgType = MessageType.accept;
								break;
							case 'c':
								msgType = MessageType.comment;
								break;
							case 'r':
								msgType = MessageType.reject;
								break;
							case 's':
								msgType = MessageType.submit;
								break;
							default:
								// won't worry about throwing an exception:
								// leave value as null / blank
								break;
						}
						ItemNodeStatusMessageBean insmb = new ItemNodeStatusMessageBean(msgType, ub,
							wrkflwMsg.getMessage(), wrkflwMsg.getDate());
						insmbs.add(insmb);
					}
					inseb.setComments(insmbs);
				}
				inseb.setDue(itemStatus.getDateDue());
				inseb.setStarted(itemStatus.getStarted());
				inseb.setStatus(Character.toString(itemStatus.getStatus()));
				if( itemStatus.getNode() != null ) // surely not
				{
					inseb.setUuid(itemStatus.getNode().getUuid());
				}
				statuses.add(inseb);
			}
			exportBean.setStatuses(statuses);
		}
		return exportBean;
	}

	@Override
	public List<HistoryEventBean> getHistory(String uuid, int version)
	{
		List<HistoryEvent> history = itemHistoryService.getHistory(new ItemId(uuid, version));
		List<HistoryEventBean> historyBeans = Lists.newArrayList();
		if( history != null )
		{
			for( HistoryEvent historyEvent : history )
			{
				HistoryEventBean bean = itemHistorySerializer.serialize(historyEvent);
				historyBeans.add(bean);
			}
		}
		return historyBeans;
	}

	/**
	 * Similar operation in the ItemLockResource
	 * 
	 * @see com.tle.web.api.item.interfaces.ItemLockResource#get(UriInfo,
	 *      String, int)
	 * @param uuid
	 * @param version
	 * @return
	 */
	@Override
	public ItemLockBean getItemLock(EquellaItemBean equellaBean)
	{
		Item item = itemService.get(new ItemId(equellaBean.getUuid(), equellaBean.getVersion()));
		final ItemLock lock = lockingService.get(item);
		if( lock == null )
		{
			return null;
		}
		final URI loc = urlLinkService.getMethodUriBuilder(ItemLockResource.class, "get").build(item.getUuid(),
			item.getVersion());
		final ItemLockBean lockBean = new ItemLockBean();
		final Map<String, String> linkMap = Maps.newHashMap();
		linkMap.put("self", loc.toString());
		lockBean.setOwner(new UserBean(lock.getUserID()));
		lockBean.setUuid(lock.getUserSession());
		lockBean.set("links", linkMap);
		return lockBean;
	}

	@Transactional
	protected ItemSerializerState createState(ItemSerializerWhere where, Collection<String> categories,
		String[] privileges, boolean ignorePriv, boolean export)
	{
		Set<String> setCats;
		if( categories instanceof Set )
		{
			setCats = (Set<String>) categories;
		}
		else
		{
			setCats = Sets.newHashSet(categories);
		}
		ItemSerializerState state = new ItemSerializerState(setCats, export);
		if( ignorePriv )
		{
			state.setIgnorePrivileges(true);
		}
		else
		{
			for( String priv : privileges )
			{
				state.addPrivilege(priv);
			}
		}
		where.addWhere(state);
		List<ItemSerializerProvider> providers = providerTracker.getBeanList();

		// Allow providers to add their own joins and projections to the item
		// query - hopefully things can be retrieved in this one query!
		for( ItemSerializerProvider provider : providers )
		{
			provider.prepareItemQuery(state);
		}

		// Perform the query and store the results
		List<Map<String, Object>> itemQueryResults = itemService.queryItems(state.getItemQuery(),
			state.getFirstResult(), state.getMaxResults());
		state.processItemQueryResults(itemQueryResults);

		if( state.isOwnerQueryAdded() )
		{
			Multimap<Long, String> collabs = itemDao.getCollaboratorsForItemIds(state.getItemKeys());
			for( Entry<Long, Collection<String>> entry : collabs.asMap().entrySet() )
			{
				state.setData(entry.getKey(), ItemSerializerState.COLLAB_ALIAS, entry.getValue());
			}
		}

		processPrivileges(state);

		// Allow providers to perform additional queries that may be required
		for( ItemSerializerProvider provider : providers )
		{
			provider.performAdditionalQueries(state);
		}

		// Resolve any requested language bundles to strings for the current
		// locale of the user
		Set<Long> bundlesToResolve = state.getBundlesToResolve();
		if( !Check.isEmpty(bundlesToResolve) )
		{
			state.setResolvedBundles(languageService.getNames(bundlesToResolve));
		}
		return state;
	}

	@SuppressWarnings("unchecked")
	private void processPrivileges(ItemSerializerState state)
	{
		Set<String> privs = state.getPrivileges();
		if( !privs.isEmpty() )
		{
			if( state.isIgnorePrivileges() )
			{
				Builder<String, Long> privMultimap = ImmutableSetMultimap.builder();
				for( Long itemId : state.getItemKeys() )
				{
					for( String priv : privs )
					{
						privMultimap.put(priv, itemId);
					}
				}

				state.setPrivilegeMap(privMultimap.build());
			}
			else
			{
				List<SimpleItemSecurity> securityObjects = Lists.newArrayList();
				for( Long itemId : state.getItemKeys() )
				{
					Map<String, Object> itemData = state.getItemData(itemId);
					String status = (String) itemData.get(ItemSerializerState.STATUS_ALIAS);
					long collectionId = (Long) itemData.get(ItemSerializerState.COLLECTIONID_ALIAS);
					String ownerUuid = (String) itemData.get(ItemSerializerState.OWNER_ALIAS);
					String currentUserId = CurrentUser.getUserID();
					boolean owner = ownerUuid != null && currentUserId.equals(ownerUuid);
					if( !owner )
					{
						Collection<String> collabs = (Collection<String>) itemData
							.get(ItemSerializerState.COLLAB_ALIAS);
						owner = collabs != null && collabs.contains(currentUserId);
					}
					Collection<String> metadataTargets = (Collection<String>) itemData
						.get(ItemSerializerState.SECURITY_ALIAS);
					SimpleItemSecurity itemSecurity = new SimpleItemSecurity(itemId, status, collectionId,
						metadataTargets, owner);
					securityObjects.add(itemSecurity);
				}
				Builder<String, Long> privMultimap = ImmutableSetMultimap.builder();
				Map<SimpleItemSecurity, Map<String, Boolean>> privMap = aclManager.getPrivilegesForObjects(privs,
					securityObjects);
				for( Entry<SimpleItemSecurity, Map<String, Boolean>> securityPrivEntry : privMap.entrySet() )
				{
					SimpleItemSecurity key = securityPrivEntry.getKey();
					long itemId = key.getItemId();
					for( Entry<String, Boolean> privEntry : securityPrivEntry.getValue().entrySet() )
					{
						if( privEntry.getValue() )
						{
							privMultimap.put(privEntry.getKey(), itemId);
						}
					}
				}
				state.setPrivilegeMap(privMultimap.build());
			}
		}
	}

	public class SimpleItemSerializerXml implements ItemSerializerXml
	{
		private ItemSerializerState state;

		public SimpleItemSerializerXml(ItemSerializerState state)
		{
			this.state = state;
		}

		@Override
		public void writeXmlResult(XMLStreamer xml, long itemId)
		{
			for( ItemSerializerProvider provider : providerTracker.getBeanList() )
			{
				provider.writeXmlResult(xml, state, itemId);
			}
		}
	}

	private final ItemSerializerXml EMPTY_ITEM_SERIALIZER_XML = new ItemSerializerXml()
	{
		@Override
		public void writeXmlResult(XMLStreamer xml, long itemId)
		{
			throw new UnsupportedOperationException("This serialiser assumes there are no items");
		}
	};

	public class SimpleItemSerializerItemBean implements ItemSerializerItemBean
	{
		private ItemSerializerState state;

		public SimpleItemSerializerItemBean(ItemSerializerState state)
		{
			this.state = state;
		}

		@Override
		public void writeItemBeanResult(EquellaItemBean equellaItemBean, long itemId)
		{
			for( ItemSerializerProvider provider : providerTracker.getBeanList() )
			{
				provider.writeItemBeanResult(equellaItemBean, state, itemId);
			}
		}

		@Override
		public boolean hasPrivilege(long itemKey, String privilege)
		{
			return state.hasPrivilege(itemKey, privilege);
		}

		@Override
		public Collection<Long> getItemIds()
		{
			return state.getItemKeys();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getData(long itemId, String alias)
		{
			return (T) state.getData(itemId, alias);
		}
	}

	private final ItemSerializerItemBean EMPTY_ITEM_SERIALIZER_ITEM_BEAN = new ItemSerializerItemBean()
	{
		@Override
		public void writeItemBeanResult(EquellaItemBean equellaItemBean, long itemId)
		{
			throw new UnsupportedOperationException("This serialiser assumes there are no items");
		}

		@Override
		public boolean hasPrivilege(long itemKey, String privilege)
		{
			throw new UnsupportedOperationException("This serialiser assumes there are no items");
		}

		@Override
		public Collection<Long> getItemIds()
		{
			return Collections.emptySet();
		}

		@Override
		public <T> T getData(long itemId, String alias)
		{
			throw new UnsupportedOperationException("This serialiser assumes there are no items");
		}
	};
}
