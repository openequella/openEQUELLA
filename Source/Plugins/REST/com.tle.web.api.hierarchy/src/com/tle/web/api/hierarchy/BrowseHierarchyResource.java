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

package com.tle.web.api.hierarchy;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.SchemaScript;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.Utils;
import com.tle.common.hierarchy.VirtualTopicUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.interfaces.CsvList;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.PresetSearch;
import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SearchResults;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.core.item.serializer.ItemSerializerItemBean;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.security.TLEAclManager;
import com.tle.web.api.hierarchy.beans.HierarchyBrowseBean;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.ItemResource;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.remoting.rest.service.UrlLinkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author larry
 */
@Bind
@Path("browsehierarchy")
@Api(value = "/browsehierarchy", description = "hierarchy-browse")
@Produces({"application/json"})
@Singleton
@SuppressWarnings("nls")
public class BrowseHierarchyResource
{
	@Inject
	private HierarchyService hierarchyService;
	@Inject
	private UrlLinkService urlLinkService;
	@Inject
	private FreeTextService freetextService;
	@Inject
	private ItemLinkService itemLinkService;
	@Inject
	private ItemSerializerService itemSerializerService;
	@Inject
	private TLEAclManager aclManager;

	/**
	 * Similar to the <server_url>/api/hierarchy<br>
	 * 
	 * @see com.tle.web.api.hierarchy.EditHierarchyResource#listTopLevel<br>
	 *      except the former returns links for edit mode without resolving
	 *      virtual placeholders, whereas here the nodes are expanded, and links
	 *      point to display/browse mode
	 * @return
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "List top-level hierarchies")
	public Response listTopLevel()
	{
		List<HierarchyTopic> topLevelNodes = hierarchyService.getChildTopics(null);

		Collection<HierarchyTopic> allowedTopics = aclManager
			.filterNonGrantedObjects(Collections.singleton("VIEW_HIERARCHY_TOPIC"), topLevelNodes);

		final List<VirtualisableAndValue<HierarchyTopic>> topicValues = hierarchyService
			.expandVirtualisedTopics((List<HierarchyTopic>) allowedTopics, null, null);

		List<HierarchyBrowseBean> results = new ArrayList<HierarchyBrowseBean>(topLevelNodes.size());

		for( VirtualisableAndValue<HierarchyTopic> vav : topicValues )
		{
			HierarchyBrowseBean topLevelBean = beanFromVirtualHierarchy(vav, null, null);
			results.add(topLevelBean);
		}

		SearchBean<HierarchyBrowseBean> retBean = new SearchBean<HierarchyBrowseBean>();
		retBean.setAvailable(results.size());
		retBean.setLength(results.size());
		retBean.setStart(0);
		retBean.setResults(results);

		return Response.ok(retBean).build();
	}

	/**
	 * From any hierarchy beyond the root, display its contents and its
	 * subnodes, virtualize subnode names where required. @see
	 * com.tle.web.hierarchy.section.TopicDisplaySection for description of
	 * uuidCompound format. In the simplest case this is a bare uuid, but where
	 * virtualized values exist in the hierarchy or its parentage, the compound
	 * describes the inheritance as derived from specific resolved virtual
	 * values.
	 * 
	 * @param uuidCompound always present
	 * @param start start point of items in search. Optional, defaults to 0
	 * @param length number of items to return. if absent or negative, return
	 *            all. Optional defaults to 10
	 * @param info one more (comma-separated) of
	 *            basic,metadata,attachment,detail,navigation,drm,all. defaults
	 *            to "basic"
	 * @param order One of: "relevance", "modified", "name", "rating"
	 * @param reverse Reverse the order of the results.
	 * @return http Response, wrapping either bean content or error message
	 */
	@GET
	@Path("/{uuidcompound}")
	@ApiOperation(value = "Browse hierarchy content")
	public Response navigateHierarchy(
		// @formatter:off
			@ApiParam(value = "simple or compound uuid(s)", required = true)
			@PathParam("uuidcompound")
				String uuidCompound,
			@ApiParam(value="The first record of the search results to return", required = false, defaultValue="0")
			@QueryParam("start")
				int start,
			@ApiParam(value="The number of results to return", required = false, defaultValue = "10", allowableValues = "range[0,50]")
			@QueryParam("length")
			@DefaultValue("10")
				int length,
			@ApiParam(value="How much information to return for the results", required = false,
				allowableValues = ItemResource.ALL_ALLOWABLE_INFOS,
				allowMultiple = true)
			@QueryParam("info")
			CsvList info,
			@ApiParam(value="The order of the search results", allowableValues="relevance,modified,name,rating", required = false)
			@QueryParam("order")
				String order,
			@ApiParam(value="Reverse the order of the search results", allowableValues = "true,false", defaultValue = "false", required = false)
			@QueryParam("reverse")
				String reverse
			// @formatter:on
	)
	{
		HierarchyTopic topicThisLevel = null;
		Map<String, String> values = Maps.newHashMap();
		boolean simple = true;

		for( String uuidValue : Splitter.on(',').omitEmptyStrings().split(uuidCompound) )
		{
			final String[] uv = uuidValue.split(":", 2);
			HierarchyTopic verifyTopic = hierarchyService.getHierarchyTopicByUuid(uv[0]);
			if( verifyTopic == null )
			{
				return Response.status(Status.NOT_FOUND).entity("Topic with uuid (" + uv[0] + ") not found").build();
			}
			verifyTopic = hierarchyService.assertViewAccess(verifyTopic);

			// assertViewAccess doesn't actually throw an error (despite the
			// name of the function).
			// It just returns null if privilege not granted.
			if( verifyTopic == null )
			{
				return Response.status(Status.FORBIDDEN).build();
			}
			else if( topicThisLevel == null )
			{
				// initialise the hierarchy for this level - it will be the
				// first
				// (or only) in the compound sequence
				topicThisLevel = verifyTopic;
			}
			if( uv.length > 1 )
			{
				values.put(uv[0], URLUtils.basicUrlDecode(uv[1]));
				simple = false;
			}
		}

		List<HierarchyTopic> childTopics = hierarchyService.getChildTopics(topicThisLevel);

		Collection<HierarchyTopic> allowedChildTopics = aclManager
			.filterNonGrantedObjects(Collections.singleton("VIEW_HIERARCHY_TOPIC"), childTopics);

		Collection<String> collectionUuids = gatherCollectionUuids(topicThisLevel);

		final List<VirtualisableAndValue<HierarchyTopic>> topicValues = hierarchyService
			.expandVirtualisedTopics((List<HierarchyTopic>) allowedChildTopics, values, collectionUuids);

		VirtualisableAndValue<HierarchyTopic> vav = simple ? new VirtualisableAndValue<HierarchyTopic>(topicThisLevel)
			: new VirtualisableAndValue<HierarchyTopic>(topicThisLevel, values.get(topicThisLevel.getUuid()), 0);

		HierarchyBrowseBean bean = beanFromVirtualHierarchy(vav, values, topicValues);

		addItemsToBean(bean, vav, values, topicValues, start, length, info, order, reverse);

		return Response.ok(bean).build();

	}

	/**
	 * Hierarchy properties including sub-topics, but not including items.
	 * 
	 * @param vav
	 * @param virtualizedMap
	 * @param childTopicValues
	 * @return
	 */
	private HierarchyBrowseBean beanFromVirtualHierarchy(VirtualisableAndValue<HierarchyTopic> vav,
		Map<String, String> virtualizedMap, List<VirtualisableAndValue<HierarchyTopic>> childTopicValues)
	{
		HierarchyBrowseBean bean = new HierarchyBrowseBean();
		HierarchyTopic hierarchy = vav.getVt();
		String dynamicValue = vav.getVirtualisedValue();

		// name, description etc
		manipulateDynamicStrings(bean, vav);

		bean.setUuid(hierarchy.getUuid());

		String topicId = null;
		// NB: the link must use the compound id (where such applies), not the
		// simple uuid
		if( dynamicValue != null )
		{
			if( virtualizedMap == null )
			{
				virtualizedMap = new HashMap<String, String>();
			}
			virtualizedMap.put(vav.getVt().getUuid(), dynamicValue);
			topicId = VirtualTopicUtils.buildTopicId(hierarchy, dynamicValue, virtualizedMap);
		}
		else
		{
			topicId = hierarchy.getUuid();
		}

		HierarchyTopic parentHierarchy = hierarchy.getParent();
		if( parentHierarchy != null )
		{
			// add parent to the links bag, using the compounding apparatus
			String parentalCompoundId = VirtualTopicUtils.buildTopicId(parentHierarchy,
				virtualizedMap.get(parentHierarchy.getUuid()), virtualizedMap);
			HierarchyBrowseBean parent = new HierarchyBrowseBean();
			parent.setUuid(parentalCompoundId);
			parent.set("links", Collections.singletonMap("self", getSelfLink(parentalCompoundId)));
			bean.setParent(parent);
		}

		PowerSearch pwrSearch = hierarchy.getAdvancedSearch();
		if( pwrSearch != null )
		{
			bean.setPowerSearchUuid(pwrSearch.getUuid());
		}

		// navigable childTopics, if any
		if( !Check.isEmpty(childTopicValues) )
		{
			List<HierarchyBrowseBean> childTopicBeans = new ArrayList<HierarchyBrowseBean>(childTopicValues.size());
			for( VirtualisableAndValue<HierarchyTopic> childTopicValue : childTopicValues )
			{
				HierarchyBrowseBean subBean = new HierarchyBrowseBean();

				HierarchyTopic childTopic = childTopicValue.getVt();
				String childDynamicValue = childTopicValue.getVirtualisedValue();
				String childId = childDynamicValue != null
					? VirtualTopicUtils.buildTopicId(childTopic, childDynamicValue, virtualizedMap)
					: childTopic.getUuid();

				manipulateDynamicStrings(subBean, childTopicValue);

				subBean.setUuid(childTopic.getUuid());

				childTopicBeans.add(subBean);
				Map<String, Object> subLinks = new HashMap<String, Object>();

				// NB: as with the parent, the link must use the compound id,
				// not the simple uuid
				subLinks.put("self", getSelfLink(childId).toString());
				subBean.set("links", subLinks);
			}
			bean.setSubTopics(childTopicBeans);
		}

		Map<String, Object> links = new HashMap<String, Object>();
		links.put("self", getSelfLink(topicId).toString());
		bean.set("links", links);

		return bean;
	}

	/**
	 * Search for and add item links to the bean. Called for any browse
	 * operation except top-level browse.
	 * 
	 * @param bean
	 * @param vav
	 * @param virtualizedMap
	 * @param childTopicValues
	 * @return
	 */
	private void addItemsToBean(HierarchyBrowseBean bean, VirtualisableAndValue<HierarchyTopic> vav,
		Map<String, String> virtualizedMap, List<VirtualisableAndValue<HierarchyTopic>> childTopicValues, int start,
		int length, CsvList info, String order, String reverse)
	{
		HierarchyTopic hierarchy = vav.getVt();
		String freetextQuery = hierarchyService.getFullFreetextQuery(hierarchy);

		// build and run a search for items
		FreeTextBooleanQuery searchClause = hierarchyService.getSearchClause(hierarchy, virtualizedMap);

		final PresetSearch search = new PresetSearch(freetextQuery, searchClause, true);

		Collection<Schema> schemas = gatherSchemas(hierarchy);
		Set<String> collectionUuids = gatherCollectionUuids(hierarchy);

		search.setCollectionUuids(collectionUuids);
		search.setSchemas(schemas);

		// how much info are we extracting for items?
		final List<String> infos = CsvList.asList(info, ItemSerializerService.CATEGORY_BASIC);

		final SortType orderType = DefaultSearch.getOrderType(order, null);
		final boolean reverseOrder = (reverse != null && Utils.parseLooseBool(reverse, false));

		search.setSortFields(orderType.getSortField(reverseOrder));

		final SearchBean<ItemBean> result = new SearchBean<ItemBean>();
		final List<ItemBean> resultItems = Lists.newArrayList();

		SearchResults<ItemIdKey> searchResults = freetextService.searchIds(search, start, length);

		final List<ItemIdKey> itemIds = searchResults.getResults();
		final List<Long> ids = Lists.transform(itemIds, new Function<ItemIdKey, Long>()
		{
			@Override
			public Long apply(ItemIdKey input)
			{
				return input.getKey();
			}
		});

		final ItemSerializerItemBean serializer = itemSerializerService.createItemBeanSerializer(ids, infos, false);
		for( ItemIdKey itemIdKey : itemIds )
		{
			ItemBean returnableItemBean = writeOutItemBean(serializer, itemIdKey.getUuid(), itemIdKey.getVersion(),
				itemIdKey.getKey());
			resultItems.add(returnableItemBean);
		}

		result.setStart(searchResults.getOffset());
		result.setLength(searchResults.getCount());
		result.setAvailable(searchResults.getAvailable());
		result.setResults(resultItems);
		bean.setSearchResults(result);

		// Key resources, if present, are not paged
		List<Item> keyItems = hierarchy.getKeyResources();

		if( !Check.isEmpty(keyItems) )
		{
			// detail keyword from ItemResource.ALL_ALLOWABLE_INFOS
			List<ItemBean> keyItemBeans = !Check.isEmpty(keyItems) ? itemBeansFromItemList(keyItems, infos) : null;
			bean.setKeyResources(keyItemBeans);
		}
	}

	/**
	 * @param
	 * @return
	 */
	protected List<ItemBean> itemBeansFromItemList(List<Item> items, Collection<String> infos)
	{
		List<ItemBean> returnResults = Lists.newArrayList();

		final List<Long> ids = Lists.transform(items, new Function<Item, Long>()
		{
			@Override
			public Long apply(Item input)
			{
				return input.getId();
			}
		});

		final ItemSerializerItemBean serializer = itemSerializerService.createItemBeanSerializer(ids, infos, false);
		for( Item item : items )
		{
			ItemBean itemBean = writeOutItemBean(serializer, item.getUuid(), item.getVersion(), item.getId());
			itemLinkService.addLinks(itemBean);
			returnResults.add(itemBean);
		}

		return returnResults;
	}

	private ItemBean writeOutItemBean(ItemSerializerItemBean serializer, String itemUuid, int itemVersion, long lKey)
	{
		EquellaItemBean itemBean = new EquellaItemBean();
		itemBean.setUuid(itemUuid);
		itemBean.setVersion(itemVersion);
		serializer.writeItemBeanResult(itemBean, lKey);
		itemLinkService.addLinks(itemBean);
		return itemBean;
	}

	/**
	 * Name and other dynamic strings may contain a replaceable wildcard ...
	 */
	private void manipulateDynamicStrings(HierarchyBrowseBean bean, VirtualisableAndValue<HierarchyTopic> vav)
	{
		HierarchyTopic hierarchy = vav.getVt();

		String dynamicValue = vav.getVirtualisedValue();

		bean.setName(virtualizeString(hierarchy.getName(), dynamicValue));
		bean.setShortDescription(virtualizeString(hierarchy.getShortDescription(), dynamicValue));
		bean.setLongDescription(virtualizeString(hierarchy.getLongDescription(), dynamicValue));
		bean.setResultsSectionName(virtualizeString(hierarchy.getResultsSectionName(), dynamicValue));
		bean.setSubTopicsSectionName(virtualizeString(hierarchy.getSubtopicsSectionName(), dynamicValue));
	}

	private String virtualizeString(LanguageBundle bundle, String dynamic)
	{
		String virtualised = bundle != null ? CurrentLocale.get(bundle) : null;
		if( !Check.isEmpty(virtualised) && !Check.isEmpty(dynamic) )
		{
			virtualised = virtualised.replaceAll("%s", dynamic);
		}
		return virtualised;
	}

	/**
	 * @param hierarchy
	 * @return non-empty set of String, or null
	 */
	private Collection<Schema> gatherSchemas(HierarchyTopic hierarchy)
	{
		List<SchemaScript> allSchemaScripts = hierarchy.getAdditionalSchemas();
		if( Check.isEmpty(allSchemaScripts) )
		{
			allSchemaScripts = hierarchy.getInheritedSchemas();
		}
		else if( !Check.isEmpty(hierarchy.getInheritedSchemas()) )
		{
			allSchemaScripts.addAll(hierarchy.getInheritedSchemas());
		}
		Collection<Schema> retSet = null;
		if( !Check.isEmpty(allSchemaScripts) )
		{
			retSet = new HashSet<Schema>(allSchemaScripts.size());
			for( SchemaScript schemaScript : allSchemaScripts )
			{
				retSet.add(schemaScript.getEntity());
			}
		}
		return retSet;
	}

	/**
	 * @param hierarchy
	 * @return non-empty set of String, or null
	 */
	private Set<String> gatherCollectionUuids(HierarchyTopic hierarchy)
	{
		List<ItemDefinitionScript> allDefs = hierarchy.getAdditionalItemDefs();
		allDefs.addAll(hierarchy.getInheritedItemDefs());
		Set<String> collectionUuids = new HashSet<String>();
		for( ItemDefinitionScript aScript : allDefs )
		{
			collectionUuids.add(aScript.getEntity().getUuid());
		}

		return collectionUuids.size() > 0 ? collectionUuids : null;
	}

	private URI getSelfLink(String hierarchyUuid)
	{
		return urlLinkService.getMethodUriBuilder(getClass(), "navigateHierarchy").build(hierarchyUuid);
	}
}
