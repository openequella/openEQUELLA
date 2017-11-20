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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.dytech.edge.exceptions.WebException;
import com.google.common.collect.Maps;
import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.SchemaScript;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.core.item.security.ItemSecurityConstants;
import com.tle.core.item.serializer.ItemSerializerItemBean;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.core.item.service.ItemService;
import com.tle.core.powersearch.PowerSearchService;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.security.TLEAclManager;
import com.tle.web.api.hierarchy.beans.HierarchyEditBean;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.equella.interfaces.beans.ItemDefinitionScriptBean;
import com.tle.web.api.item.equella.interfaces.beans.SchemaScriptBean;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.remoting.rest.service.UrlLinkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Bind
@Path("hierarchy")
@Api(value = "/hierarchy", description = "hierarchy")
@Produces({"application/json"})
@Singleton
@SuppressWarnings("nls")
public class EditHierarchyResource
{
	private static final String EDIT_PRIV = "EDIT_HIERARCHY_TOPIC";
	private static final String APIDOC_TOPICJSON = "The hierarchy topic in json format";

	@Inject
	private HierarchyService hierarchyService;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private UrlLinkService urlLinkService;
	@Inject
	private ItemLinkService itemLinkService;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemSerializerService itemSerializerService;
	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private PowerSearchService powerSearchService;
	@Inject
	private SchemaService schemaService;

	/**
	 * Returns all root-level topics.
	 * 
	 * @return Response encapsulating SearchBean of List of HierarchyEditBean
	 */
	@GET
	@Path("")
	@Produces("application/json")
	@ApiOperation(value = "List top-level hierarchies")
	public Response listTopLevel()
	{
		List<HierarchyTopic> topLevelNodes = hierarchyService.getChildTopics(null);

		Collection<HierarchyTopic> allowedTopics = aclManager.filterNonGrantedObjects(Collections.singleton(EDIT_PRIV),
			topLevelNodes);

		List<HierarchyEditBean> results = new ArrayList<HierarchyEditBean>(topLevelNodes.size());
		for( HierarchyTopic topLevelNode : allowedTopics )
		{
			HierarchyEditBean bean = beanFromHierarchyTopic(topLevelNode, true, null);
			results.add(bean);
		}

		SearchBean<HierarchyEditBean> retBean = new SearchBean<HierarchyEditBean>();

		retBean.setAvailable(results.size());
		retBean.setLength(results.size());
		retBean.setStart(0);
		retBean.setResults(results);

		return Response.ok(retBean).build();
	}

	/**
	 * Returns full details of a Hierarchy topic specified by its Uuid.
	 * 
	 * @return Response encapsulating HierarchyEditBean
	 */
	@GET
	@Path("/{uuid}")
	@Produces("application/json")
	@ApiOperation(value = "Hierarchy topic settings")
	public Response getHierarchyTopicSettings(
		@ApiParam(value = "base Hierarchy id", required = true) @PathParam("uuid") String uuid)
	{
		HierarchyTopic hierarchyTopic = hierarchyService.getHierarchyTopicByUuid(uuid);

		if( hierarchyTopic == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		else
		{
			List<HierarchyTopic> childTopics = hierarchyService.getChildTopics(hierarchyTopic);

			Collection<HierarchyTopic> allowedTopics = aclManager
				.filterNonGrantedObjects(Collections.singleton(EDIT_PRIV), childTopics);

			HierarchyEditBean bean = beanFromHierarchyTopic(hierarchyTopic, true, allowedTopics);
			return Response.ok(bean).build();
		}
	}

	@POST
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create a hierarchy topic")
	public Response createTopic(
// @formatter:off
			@ApiParam(APIDOC_TOPICJSON) HierarchyEditBean hierarchyBean
			// @formatter:on
	)
	{
		String uuid = hierarchyBean.getUuid();
		if( Check.isEmpty(uuid) )
		{
			uuid = UUID.randomUUID().toString();
			hierarchyBean.setUuid(uuid);
		}

		// null: there's no original that's being edited
		HierarchyTopic newTopic = hierarchyTopicFromBean(hierarchyBean, null);
		HierarchyTopic parentTopic = null;
		int index = 0;
		HierarchyEditBean parent = hierarchyBean.getParent();
		String parentUuid = (parent == null ? null : parent.getUuid());

		// parentUuid if provided must be valid
		if( !Check.isEmpty(parentUuid) )
		{
			parentTopic = hierarchyService.getHierarchyTopicByUuid(parentUuid);
			if( parentTopic == null )
			{
				throw new WebException(Status.BAD_REQUEST.getStatusCode(), Status.BAD_REQUEST.getReasonPhrase(),
					"parentUuid given is not valid");
			}
		}

		if( parentTopic != null )
		{
			hierarchyService.add(parentTopic, newTopic, index);
		}
		else
		{
			hierarchyService.addRoot(newTopic, index);
		}
		HierarchyEditBean returnBean = beanFromHierarchyTopic(newTopic, true, null);
		return Response.status(Status.CREATED).entity(returnBean).build();
	}

	@PUT
	@Path("/{uuid}")
	@ApiOperation(value = "edit a HierarchyTopic")
	public Response editHierarchy(
		// @formatter:off
			@ApiParam(value = "target hierarchy uuid", required = true)
				@PathParam("uuid")
				String uuid,
			@ApiParam(APIDOC_TOPICJSON)
				HierarchyEditBean hierarchyBean
			// @formatter:on
	)
	{
		// Ensure the uuid passed in is valid
		HierarchyTopic hierarchy = hierarchyService.getHierarchyTopicByUuid(uuid);
		if( hierarchy == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}

		// populate an Entity from the bean
		HierarchyTopic editedTopic = hierarchyTopicFromBean(hierarchyBean, hierarchy);

		hierarchyService.edit(editedTopic);
		return Response.ok().build();
	}

	@PUT
	@Path("/move/{uuid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Move a hierarchy relative to parent")
	public Response moveTopic(
		// @formatter:off
			@ApiParam(value = "id of hierarchy being moved")
				@PathParam("uuid")
				String childUuid,
			@ApiParam(value = "id of newly placed parent, if absent root level applies", required = false)
				@QueryParam("parent")
				String parentUuid,
			@ApiParam(value = "index of child within parent", required = true)
				@QueryParam("index")
				int index
			// @formatter:on
	)
	{
		// Ensure the childUuids passed in is valid
		HierarchyTopic childHierarchy = hierarchyService.getHierarchyTopicByUuid(childUuid);
		if( childHierarchy == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}

		// it is permissible for the parent to be absent (ie movement is at root
		// level)
		// but if supplied must be valid
		if( !Check.isEmpty(parentUuid) )
		{
			HierarchyTopic parentHierarchy = hierarchyService.getHierarchyTopicByUuid(parentUuid);
			if( parentHierarchy == null )
			{
				return Response.status(Status.NOT_FOUND).build();
			}
		}

		hierarchyService.move(childUuid, parentUuid, index);

		return Response.ok().build();
	}

	@DELETE
	@Path("/{uuid}")
	@ApiOperation(value = "Delete a Hierarchy, (includes any children)")
	public Response deleteHierarchy(
		@ApiParam(value = "base Hierarchy id", required = true) @PathParam("uuid") String uuid)
	{
		HierarchyTopic hierarchy = hierarchyService.getHierarchyTopicByUuid(uuid);
		if( hierarchy == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		hierarchyService.delete(hierarchy);
		return Response.status(Status.NO_CONTENT).build();
	}

	/**
	 * This method has a recursive all to self, if the boolean parameter is
	 * true.<b> The inner recursive call always has that parameter as false
	 */
	private HierarchyEditBean beanFromHierarchyTopic(HierarchyTopic hierarchyTopic, boolean details,
		Collection<HierarchyTopic> subnodes)
	{
		HierarchyEditBean bean = new HierarchyEditBean();
		String uuid = hierarchyTopic.getUuid();

		// the minimal is uuid, name and link to self
		bean.setUuid(uuid);
		bean.setName(CurrentLocale.get(hierarchyTopic.getName()));
		Map<String, Object> links = new HashMap<String, Object>();
		links.put("self", getSelfLink(uuid).toString());

		bean.setInheritFreetext(hierarchyTopic.isInheritFreetext());
		bean.setShowResults(hierarchyTopic.isShowResults());
		bean.setHideSubtopicsWithNoResults(hierarchyTopic.isHideSubtopicsWithNoResults());

		if( details )
		{
			// some further details about self
			// in the admin console, the Display Details tab ..
			if( hierarchyTopic.getShortDescription() != null )
			{
				bean.setShortDescription(CurrentLocale.get(hierarchyTopic.getShortDescription()));
			}
			if( hierarchyTopic.getLongDescription() != null )
			{
				bean.setLongDescription(CurrentLocale.get(hierarchyTopic.getLongDescription()));
			}
			if( hierarchyTopic.getSubtopicsSectionName() != null )
			{
				bean.setSubTopicsSectionName(CurrentLocale.get(hierarchyTopic.getSubtopicsSectionName()));
			}
			if( hierarchyTopic.getResultsSectionName() != null )
			{
				bean.setResultsSectionName(CurrentLocale.get(hierarchyTopic.getResultsSectionName()));
			}

			PowerSearch pwrSearch = hierarchyTopic.getAdvancedSearch();
			if( pwrSearch != null )
			{
				bean.setPowerSearchUuid(pwrSearch.getUuid());
			}

			// the admin console's Resource Filtering tab
			if( !Check.isEmpty(hierarchyTopic.getFreetext()) )
			{
				bean.setFreetext(hierarchyTopic.getFreetext());
			}

			if( !Check.isEmpty(hierarchyTopic.getAdditionalSchemas()) )
			{
				List<SchemaScript> value = hierarchyTopic.getAdditionalSchemas();
				bean.setAdditionalSchemaScripts(beansFromObjectsSchemaScript(value));
			}

			if( !Check.isEmpty(hierarchyTopic.getAdditionalItemDefs()) )
			{
				List<ItemDefinitionScript> value = hierarchyTopic.getAdditionalItemDefs();
				bean.setAdditionalCollectionScripts(beansFromObjectsItemDefinitionScript(value));
			}

			// Those operations seen in admin console's virtual topics tab...
			if( hierarchyTopic.isVirtual() )
			{
				bean.setVirtualisationPath(hierarchyTopic.getVirtualisationPath());
				bean.setVirtualisationId(hierarchyTopic.getVirtualisationId());
			}

			// Corresponding to the admin console's Key Resources tab
			if( !Check.isEmpty(hierarchyTopic.getKeyResources()) )
			{
				List<ItemBean> keyResourceBeans = itemBeansFromItems(hierarchyTopic.getKeyResources(), false);
				bean.setKeyResources(keyResourceBeans);
			}

			// For a sub-hierarchy, the admin console represented a Resource
			// Inheritance tab ...
			HierarchyTopic parentHierachy = hierarchyTopic.getParent();
			if( parentHierachy != null )
			{
				HierarchyEditBean parent = new HierarchyEditBean();
				parent.setUuid(hierarchyTopic.getParent().getUuid());
				Map<String, String> parentLink = Maps.newHashMap();
				parentLink.put("self", getSelfLink(parent.getUuid()).toString());
				parent.set("links", parentLink);
				bean.setParent(parent);

				getInheritedAndEligibleItemDefScripts(bean, hierarchyTopic, parentHierachy);
				getInheritedAndEligibleSchemaScripts(bean, hierarchyTopic, parentHierachy);
			}
			else
			{
				bean.setInheritFreetext(false);
			}

			// dynamic filtering properties
			//			if( !Check.isEmpty(hierarchyTopic.getAttributes()) )
			//			{
			//				List<HierarchyEditBean.AttributeBean> beanAttributes = Lists.newArrayList();
			//				for( HierarchyTopic.Attribute topicAttribute : hierarchyTopic.getAttributes() )
			//				{
			//					HierarchyEditBean.AttributeBean attribBean = new HierarchyEditBean.AttributeBean();
			//					attribBean.setKey(topicAttribute.getKey());
			//					attribBean.setValue(topicAttribute.getValue());
			//					beanAttributes.add(attribBean);
			//				}
			//				bean.setAttributes(beanAttributes);
			//			}

			// minimal detail of subnodes, if any
			if( !Check.isEmpty(subnodes) )
			{
				List<HierarchyEditBean> subnodeBeans = new ArrayList<HierarchyEditBean>(subnodes.size());
				for( HierarchyTopic subnode : subnodes )
				{
					HierarchyEditBean minimalBean = beanFromHierarchyTopic(subnode, false, null);
					subnodeBeans.add(minimalBean);
				}
				bean.setSubTopics(subnodeBeans);
			}
		}

		bean.set("links", links);
		return bean;
	}

	private HierarchyTopic hierarchyTopicFromBean(HierarchyEditBean bean, HierarchyTopic originalHierarchy)
	{
		HierarchyTopic hierarchyTopic = new HierarchyTopic();

		if( originalHierarchy != null )
		{
			// we need to preserve the database id and institution from the
			// topic in its original state
			hierarchyTopic.setId(originalHierarchy.getId());
			hierarchyTopic.setInstitution(originalHierarchy.getInstitution());
		}

		hierarchyTopic.setUuid(bean.getUuid());

		Locale locale = CurrentLocale.getLocale();
		// Name
		hierarchyTopic.setName(LangUtils.createTextTempLangugageBundle(bean.getName(), locale));
		// Short description
		hierarchyTopic.setShortDescription(LangUtils.createTextTempLangugageBundle(bean.getShortDescription(), locale));
		// Long description
		hierarchyTopic.setLongDescription(LangUtils.createTextTempLangugageBundle(bean.getLongDescription(), locale));
		// Sub topic headings
		hierarchyTopic
			.setSubtopicsSectionName(LangUtils.createTextTempLangugageBundle(bean.getSubTopicsSectionName(), locale));
		// Results heading
		hierarchyTopic
			.setResultsSectionName(LangUtils.createTextTempLangugageBundle(bean.getResultsSectionName(), locale));
		// Hide empty subtopics (true/false)
		hierarchyTopic.setHideSubtopicsWithNoResults(bean.isHideSubtopicsWithNoResults());
		// Advanced search
		String powerSearchUuid = bean.getPowerSearchUuid();
		if( !Check.isEmpty(powerSearchUuid) )
		{
			PowerSearch powerSearch = powerSearchService.getByUuid(powerSearchUuid);
			hierarchyTopic.setAdvancedSearch(powerSearch);
		}
		// Display items (true/false)
		hierarchyTopic.setShowResults(bean.isShowResults());

		// key resources
		if( !Check.isEmpty(bean.getKeyResources()) )
		{
			List<Item> keyResourceItems = new ArrayList<Item>(bean.getKeyResources().size());
			for( ItemBean keyItemBean : bean.getKeyResources() )
			{
				Item item = itemService.get(new ItemId(keyItemBean.getUuid(), keyItemBean.getVersion()));
				keyResourceItems.add(item);
			}
			hierarchyTopic.setKeyResources(keyResourceItems);
		}
		// Constraints ===================================================
		hierarchyTopic.setFreetext(bean.getFreetext());

		// Schemata - additional and inherited
		hierarchyTopic.setAdditionalSchemas(objectsFromBeansSchemaScript(bean.getAdditionalSchemaScripts()));
		hierarchyTopic.setInheritedSchemas(objectsFromBeansSchemaScript(bean.getInheritedSchemaScripts()));

		// Collections (itemdefs) - additional and inherited
		hierarchyTopic.setAdditionalItemDefs(objectsFromBeansItemDefinition(bean.getAdditionalCollectionScripts()));
		hierarchyTopic.setInheritedItemDefs(objectsFromBeansItemDefinition(bean.getInheritedCollectionScripts()));

		// Dynamic filtering

		if( !Check.isEmpty(bean.getVirtualisationPath()) )
		{
			hierarchyTopic.setVirtualisationPath(bean.getVirtualisationPath());
			hierarchyTopic.setVirtualisationId(bean.getVirtualisationId());
			//			if( !Check.isEmpty(bean.getAttributes()) )
			//			{
			//				for( HierarchyEditBean.AttributeBean beanAttribute : bean.getAttributes() )
			//				{
			//					hierarchyTopic.setAttribute(beanAttribute.getKey(), beanAttribute.getValue());
			//				}
			//			}
		}
		return hierarchyTopic;
	}

	/**
	 * If parameter requires, apply discover privilege to an list of Items.<br>
	 * Convert to List of EquellaItemBean
	 * 
	 * @param toFilt
	 * @return a list of EquellaItemBean if filtered Items exists, otherwise
	 *         null
	 */
	private List<ItemBean> itemBeansFromItems(List<Item> toFilt, boolean applyPriv)
	{
		List<ItemBean> itemBeans = null;
		Collection<Item> postFilt = applyPriv
			? aclManager.filterNonGrantedObjects(Collections.singleton(ItemSecurityConstants.DISCOVER_ITEM), toFilt)
			: toFilt;

		if( !Check.isEmpty(postFilt) )
		{
			itemBeans = new ArrayList<ItemBean>();
			Set<Long> itemIds = new HashSet<Long>();
			for( Item item : postFilt )
			{
				itemIds.add(item.getId());
			}

			ItemSerializerItemBean serializer = itemSerializerService.createItemBeanSerializer(itemIds,
				Collections.singletonList("none"), true);

			for( Item item : postFilt )
			{
				EquellaItemBean itemBean = new EquellaItemBean();
				itemBean.setUuid(item.getUuid());
				itemBean.setVersion(item.getVersion());
				serializer.writeItemBeanResult(itemBean, item.getId());

				itemLinkService.addLinks(itemBean);
				itemBeans.add(itemBean);
			}
		}
		return itemBeans;
	}

	/**
	 * Convert a list of schemaScriptBeans to SchemaScripts
	 * 
	 * @param beanSchemaScripts
	 * @return list SchemaScripts, or null if empty
	 */
	List<SchemaScript> objectsFromBeansSchemaScript(List<SchemaScriptBean> beanSchemaScripts)
	{
		List<SchemaScript> schemaScripts = null;
		if( !Check.isEmpty(beanSchemaScripts) )
		{
			schemaScripts = new ArrayList<SchemaScript>();
			for( SchemaScriptBean scriptBean : beanSchemaScripts )
			{
				Schema schema = schemaService.getByUuid(scriptBean.getSchema().getUuid());
				schemaScripts.add(new SchemaScript(schema, scriptBean.getScript()));
			}
		}
		return schemaScripts;
	}

	/**
	 * Convert a list of ItemDefinitionScriptBeans to ItemDefinitionScripts
	 * 
	 * @param ItemDefinitionScriptBeans
	 * @return list ItemDefinitionScript, or null if empty
	 */
	private List<ItemDefinitionScript> objectsFromBeansItemDefinition(List<ItemDefinitionScriptBean> beanItemDefs)
	{
		List<ItemDefinitionScript> itemDefScripts = null;
		if( !Check.isEmpty(beanItemDefs) )
		{
			itemDefScripts = new ArrayList<ItemDefinitionScript>();
			for( ItemDefinitionScriptBean itemDefinitionScriptBean : beanItemDefs )
			{
				ItemDefinition itemDefinition = itemDefinitionService
					.getByUuid(itemDefinitionScriptBean.getCollection().getUuid());
				ItemDefinitionScript script = new ItemDefinitionScript(itemDefinition,
					itemDefinitionScriptBean.getScript());
				itemDefScripts.add(script);
			}
		}
		return itemDefScripts;
	}

	/**
	 * there's a set of inheritable ItemDefn Scripts which may or may not be a
	 * super-set of ItemDefn Scripts actually inherited
	 * 
	 * @return
	 */
	private void getInheritedAndEligibleItemDefScripts(HierarchyEditBean bean, HierarchyTopic hierarchyTopic,
		HierarchyTopic parentHierachy)
	{
		// Set the explicitly inherited ItemDefinitions ...
		if( !Check.isEmpty(hierarchyTopic.getInheritedItemDefs()) )
		{
			List<ItemDefinitionScript> value = hierarchyTopic.getInheritedItemDefs();
			bean.setInheritedCollectionScripts(beansFromObjectsItemDefinitionScript(value));
		}

		// put together a list of eligible (but not yet explicitly inherited)
		// ItemDefinitions
		List<ItemDefinitionScript> allInheritableItemDefnScripts = new ArrayList<ItemDefinitionScript>();

		if( !Check.isEmpty(parentHierachy.getInheritedItemDefs()) )
		{
			allInheritableItemDefnScripts.addAll(parentHierachy.getInheritedItemDefs());
		}
		if( !Check.isEmpty(parentHierachy.getAdditionalItemDefs()) )
		{
			allInheritableItemDefnScripts.addAll(parentHierachy.getAdditionalItemDefs());
		}

		// the inheritable scripts are the combination of parent's additional
		// and its own inherited
		// Those scripts among them not already inherited by child hierarchy are
		// therefore retained as inheritable
		for( Iterator<ItemDefinitionScript> iter = allInheritableItemDefnScripts.iterator(); iter.hasNext(); )
		{
			ItemDefinitionScript aScript = iter.next();
			String aUuid = aScript.getEntity().getUuid();
			if( !Check.isEmpty(hierarchyTopic.getInheritedItemDefs()) )
			{
				for( ItemDefinitionScript childChosen : hierarchyTopic.getInheritedItemDefs() )
				{
					if( childChosen.getEntity().getUuid().equals(aUuid) )
					{
						iter.remove();
					}
				}
			}
		}
		if( !Check.isEmpty(allInheritableItemDefnScripts) )
		{
			List<ItemDefinitionScriptBean> eligibleItemDefinitionScripts = beansFromObjectsItemDefinitionScript(
				allInheritableItemDefnScripts);
			bean.setEligibleCollectionScripts(eligibleItemDefinitionScripts);
		}
	}

	/**
	 * Same logic as for ItemDefinitionScripts
	 * 
	 * @return
	 */
	private void getInheritedAndEligibleSchemaScripts(HierarchyEditBean bean, HierarchyTopic hierarchyTopic,
		HierarchyTopic parentHierachy)
	{
		// Set the explicitly inherited Schemas ...
		if( !Check.isEmpty(hierarchyTopic.getInheritedSchemas()) )
		{
			List<SchemaScript> value = hierarchyTopic.getInheritedSchemas();
			bean.setInheritedSchemaScripts(beansFromObjectsSchemaScript(value));
		}

		// put together a list of eligible (but not yet explicitly inherited)
		// Schemas
		List<SchemaScript> allInheritableSchemaScripts = new ArrayList<SchemaScript>();

		if( !Check.isEmpty(parentHierachy.getInheritedSchemas()) )
		{
			allInheritableSchemaScripts.addAll(parentHierachy.getInheritedSchemas());
		}
		if( !Check.isEmpty(parentHierachy.getAdditionalSchemas()) )
		{
			allInheritableSchemaScripts.addAll(parentHierachy.getAdditionalSchemas());
		}

		// the inheritable scripts are the combination of parent's additional
		// and its own inherited
		// Those scripts among them not already inherited by child hierarchy are
		// therefore retained as inheritable
		for( Iterator<SchemaScript> iter = allInheritableSchemaScripts.iterator(); iter.hasNext(); )
		{
			SchemaScript aScript = iter.next();
			String aUuid = aScript.getEntity().getUuid();
			if( !Check.isEmpty(hierarchyTopic.getInheritedSchemas()) )
			{
				for( SchemaScript childChosen : hierarchyTopic.getInheritedSchemas() )
				{
					if( childChosen.getEntity().getUuid().equals(aUuid) )
					{
						iter.remove();
					}
				}
			}
		}
		if( !Check.isEmpty(allInheritableSchemaScripts) )
		{
			List<SchemaScriptBean> eligibleSchemaScripts = beansFromObjectsSchemaScript(allInheritableSchemaScripts);
			bean.setEligibleSchemaScripts(eligibleSchemaScripts);
		}
	}

	/**
	 * Iterate though List of SchemaScript objects
	 * 
	 * @param schemaScripts
	 * @return list of SchemaScriptBeans if objects exist, else null
	 */
	private List<SchemaScriptBean> beansFromObjectsSchemaScript(List<SchemaScript> schemaScripts)
	{
		List<SchemaScriptBean> beans = null;
		if( !Check.isEmpty(schemaScripts) )
		{
			beans = new ArrayList<SchemaScriptBean>(schemaScripts.size());
			for( SchemaScript scriptObj : schemaScripts )
			{
				SchemaScriptBean bean = new SchemaScriptBean();
				bean.setSchema(new BaseEntityReference(scriptObj.getEntity().getUuid()));
				bean.setScript(scriptObj.getScript());
				beans.add(bean);
			}
		}
		return beans;
	}

	/**
	 * Iterate though List of ItemDefinitionScript objects
	 * 
	 * @param itemDefinitionScripts
	 * @return list of ItemDefinitionScriptBeans if objects exist, else null
	 */
	private List<ItemDefinitionScriptBean> beansFromObjectsItemDefinitionScript(
		List<ItemDefinitionScript> itemDefinitionScripts)
	{
		List<ItemDefinitionScriptBean> beans = null;
		if( !Check.isEmpty(itemDefinitionScripts) )
		{
			beans = new ArrayList<ItemDefinitionScriptBean>(itemDefinitionScripts.size());
			for( ItemDefinitionScript scriptObj : itemDefinitionScripts )
			{
				ItemDefinitionScriptBean bean = new ItemDefinitionScriptBean();
				bean.setCollection(new BaseEntityReference(scriptObj.getEntity().getUuid()));
				bean.setScript(scriptObj.getScript());
				beans.add(bean);
			}
		}
		return beans;
	}

	private URI getSelfLink(String hierarchyUuid)
	{
		return urlLinkService.getMethodUriBuilder(getClass(), "getHierarchyTopicSettings").build(hierarchyUuid);
	}
}
