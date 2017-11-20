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

package com.tle.web.hierarchy.soap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.SchemaScript;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.hierarchy.HierarchyTopic.Attribute;
import com.tle.beans.hierarchy.HierarchyTopicDynamicKeyResources;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.hierarchy.VirtualTopicUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.service.ItemService;
import com.tle.core.powersearch.PowerSearchService;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.security.TLEAclManager;
import com.tle.web.hierarchy.TopicUtils;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;

@Bind
@Singleton
@SuppressWarnings("nls")
public class HierarchySoapService implements HierarchySoapInterface
{
	private static final String NAME = "name";
	private static final String SHORT_DESCRIPTION = "short.description";
	private static final String LONG_DESCRIPTION = "long.description";
	private static final String SUBTOPICS_HEADING = "subtopics.heading";
	private static final String RESULTS_HEADING = "results.heading";
	private static final String HIDE_EMPTY_SUBTOPICS = "hide.empty.subtopics";
	private static final String ADVANCED_SEARCH = "advanced.search";
	private static final String DISPLAY_ITEMS = "display.items";

	private static final String CONSTRAINTS = "constraints";
	private static final String FREETEXT = "freetext";
	private static final String SCHEMATA_SCHEMA = "schemata/schema";
	private static final String COLLS_COLLECTION = "collections/collection";

	private static final String INHERITED_CONSTRAINTS = "inherited.constraints";
	private static final String INHERIT_FREETEXT = "inherit.freetext";

	private static final String DYNAMIC_FILTERING = "dynamic.filtering";
	private static final String XPATH = "xpath";
	private static final String SOURCE = "source";

	private static final String ATTRS_ATTRIBUTE = "attributes/attribute";
	private static final String KRS_KEYRESOURCE = "keyresources/keyresource";

	private static final String ATTR_UUID = "@uuid";
	private static final String ATTR_KEY = "@key";
	private static final String ATTR_VERSION = "@version";

	@Inject
	private HierarchyService hierarchyService;
	@Inject
	private SchemaService schemaService;
	@Inject
	private ItemDefinitionService itemDefService;
	@Inject
	private ItemService itemService;
	@Inject
	private PowerSearchService powerSearchService;
	@Inject
	private TLEAclManager aclManager;

	@Inject
	private BundleCache bundleCache;

	@Override
	public String getTopic(String uuid)
	{
		HierarchyTopic ht = null;
		boolean isDynamicTopic = false;
		for( String uuidValue : Splitter.on(',').omitEmptyStrings().split(uuid) )
		{
			final String[] uv = uuidValue.split(":", 2);
			ht = hierarchyService.getHierarchyTopicByUuid(uv[0]);
			if( uv.length > 1 )
			{
				isDynamicTopic = true;
			}
		}

		PropBagEx xml = new PropBagEx();
		buildXmlFromTopic(xml, ht, isDynamicTopic, uuid);
		return xml.getSubtree("topic").toString();
	}

	@Override
	public String listTopics(String parentUuid)
	{
		HierarchyTopic ht = null;
		Map<String, String> values = Maps.newHashMap();

		if( !Check.isEmpty(parentUuid) )
		{
			for( String uuidValue : Splitter.on(',').omitEmptyStrings().split(parentUuid) )
			{
				final String[] uv = uuidValue.split(":", 2);
				ht = hierarchyService.getHierarchyTopicByUuid(uv[0]);
				if( ht == null )
				{
					throw new IllegalArgumentException("Could not find topic " + uv[0]);
				}

				if( uv.length > 1 )
				{
					values.put(uv[0], URLUtils.basicUrlDecode(uv[1]));
				}
			}
		}

		List<HierarchyTopic> childTopics = hierarchyService.getChildTopics(ht);

		Collection<HierarchyTopic> allowedTopics = aclManager
			.filterNonGrantedObjects(Collections.singleton("VIEW_HIERARCHY_TOPIC"), childTopics);

		final List<VirtualisableAndValue<HierarchyTopic>> topicValues = hierarchyService
			.expandVirtualisedTopics((List<HierarchyTopic>) allowedTopics, null, null);

		PropBagEx xml = new PropBagEx();
		xml.setNodeName("topics");

		for( VirtualisableAndValue<HierarchyTopic> p : topicValues )
		{
			HierarchyTopic topic = p.getVt();
			String dynamicValue = p.getVirtualisedValue();
			Label topicName = new BundleLabel(topic.getName(), bundleCache);
			if( dynamicValue != null )
			{
				topicName = TopicUtils.labelForValue(topicName, dynamicValue);
			}
			String topicUuid = VirtualTopicUtils.buildTopicId(topic, dynamicValue, values);

			buildBasicXmlFromTopic(xml, topicUuid, topicName.getText());
		}
		return xml.toString();
	}

	@Override
	public String createTopic(String parentUuid, String topicXml, int index)
	{
		HierarchyTopic newTopic = new HierarchyTopic();
		String newTopicUUID = UUID.randomUUID().toString();
		newTopic.setUuid(newTopicUUID);
		buildTopicFromXml(newTopic, new PropBagEx(topicXml), null, false);

		if( !Check.isEmpty(parentUuid) )
		{
			hierarchyService.add(getTopicOrFail(parentUuid), newTopic, index);
		}
		else
		{
			hierarchyService.addRoot(newTopic, index);
		}

		return newTopicUUID;
	}

	@Override
	public void editTopic(String topicUuid, String topicXml)
	{
		HierarchyTopic existingTopic = null;
		boolean isDynamicTopic = false;
		for( String uuidValue : Splitter.on(',').omitEmptyStrings().split(topicUuid) )
		{
			final String[] uv = uuidValue.split(":", 2);
			existingTopic = hierarchyService.getHierarchyTopicByUuid(uv[0]);
			if( uv.length > 1 )
			{
				isDynamicTopic = true;
			}
		}

		if( existingTopic == null )
		{
			throw new RuntimeException("(null pointer event) Could not identifiy existing topic from " + topicUuid);
		}
		HierarchyTopic updatedTopic = new HierarchyTopic();
		updatedTopic.setId(existingTopic.getId());
		updatedTopic.setUuid(existingTopic.getUuid());
		updatedTopic.setInstitution(CurrentInstitution.get());
		updatedTopic.setParent(existingTopic.getParent());

		buildTopicFromXml(updatedTopic, new PropBagEx(topicXml), topicUuid, isDynamicTopic);

		hierarchyService.edit(updatedTopic);
	}

	@Override
	public void moveTopic(String childId, String parentId, int index)
	{
		hierarchyService.move(childId, parentId, index);
	}

	@Override
	public void deleteTopic(String uuid)
	{
		HierarchyTopic topic = hierarchyService.getHierarchyTopicByUuid(uuid);
		hierarchyService.delete(topic);
	}

	private PropBagEx buildBasicXmlFromTopic(PropBagEx xml, String topicUuid, String topicName)
	{
		PropBagEx tx = xml.newSubtree("topic");
		tx.setNode(ATTR_UUID, topicUuid);
		tx.setNode(NAME, topicName);
		return tx;
	}

	private PropBagEx buildBasicXmlFromTopic(PropBagEx xml, HierarchyTopic topic)
	{
		PropBagEx tx = xml.newSubtree("topic");
		tx.setNode(ATTR_UUID, topic.getUuid());
		tx.setNode(NAME, CurrentLocale.get(topic.getName()));
		return tx;
	}

	private void buildXmlFromTopic(PropBagEx xml, HierarchyTopic topic, boolean isDynamicTopic, String id)
	{
		PropBagEx tx = buildBasicXmlFromTopic(xml, topic);

		// Short description
		LanguageBundle shortDescription = topic.getShortDescription();
		if( shortDescription != null )
		{
			tx.setNode(SHORT_DESCRIPTION, CurrentLocale.get(shortDescription));
		}

		// Long description
		LanguageBundle longDescription = topic.getLongDescription();
		if( longDescription != null )
		{
			tx.setNode(LONG_DESCRIPTION, CurrentLocale.get(longDescription));
		}

		// Sub topic headings
		LanguageBundle subtopicsSectionName = topic.getSubtopicsSectionName();
		if( subtopicsSectionName != null )
		{
			tx.setNode(SUBTOPICS_HEADING, CurrentLocale.get(subtopicsSectionName));
		}

		// Results heading
		LanguageBundle resultsSectionName = topic.getResultsSectionName();
		if( resultsSectionName != null )
		{
			tx.setNode(RESULTS_HEADING, CurrentLocale.get(resultsSectionName));
		}

		// Hide empty subtopics (true/false)
		tx.setNode(HIDE_EMPTY_SUBTOPICS, topic.isHideSubtopicsWithNoResults());

		// Advanced search
		PowerSearch advancedSearch = topic.getAdvancedSearch();
		if( advancedSearch != null )
		{
			tx.setNode(ADVANCED_SEARCH, advancedSearch.getUuid());
		}

		// Display items (true/false)
		tx.setNode(DISPLAY_ITEMS, topic.isShowResults());

		// Constraints ===================================================
		PropBagEx txConsts = tx.newSubtree(CONSTRAINTS);

		// Freetext
		String freetext = topic.getFreetext();
		if( !Check.isEmpty(freetext) )
		{
			txConsts.setNode(FREETEXT, freetext);
		}
		// Schemata
		List<SchemaScript> additionalSchemas = topic.getAdditionalSchemas();
		if( !Check.isEmpty(additionalSchemas) )
		{
			PropBagEx txcSchemata = txConsts.newSubtree("schemata");
			for( SchemaScript schemaScript : additionalSchemas )
			{
				PropBagEx schemaNode = txcSchemata.newSubtree("schema");
				schemaNode.setNode(ATTR_UUID, schemaScript.getEntity().getUuid());
				String script = schemaScript.getScript();
				if( !Check.isEmpty(script) )
				{
					schemaNode.setNode("", script);
				}
			}
		}

		// Collections
		List<ItemDefinitionScript> additionalItemDefs = topic.getAdditionalItemDefs();
		if( !Check.isEmpty(additionalItemDefs) )
		{
			PropBagEx txcCollections = txConsts.newSubtree("collections");
			for( ItemDefinitionScript itemDefScript : additionalItemDefs )
			{
				PropBagEx collectionNode = txcCollections.newSubtree("collection");
				collectionNode.setNode(ATTR_UUID, itemDefScript.getEntity().getUuid());
				String script = itemDefScript.getScript();
				if( !Check.isEmpty(script) )
				{
					collectionNode.setNode("", script);
				}
			}
		}

		// Inherited constraints ==========================================
		PropBagEx txInheritedConsts = tx.newSubtree(INHERITED_CONSTRAINTS);

		// Inherited freetext
		txInheritedConsts.setNode(INHERIT_FREETEXT, topic.isInheritFreetext());

		// Inherited schemata
		List<SchemaScript> inheritedSchemas = topic.getInheritedSchemas();
		if( !Check.isEmpty(inheritedSchemas) )
		{
			PropBagEx txcInheritSchemata = txInheritedConsts.newSubtree("inherited.schemata");
			for( SchemaScript schemaScript : inheritedSchemas )
			{
				PropBagEx schemaNode = txcInheritSchemata.newSubtree("inherit.schema");
				schemaNode.setNode(ATTR_UUID, schemaScript.getEntity().getUuid());
				String script = schemaScript.getScript();
				if( !Check.isEmpty(script) )
				{
					schemaNode.setNode("", script);
				}
			}
		}

		// Inherited collections
		List<ItemDefinitionScript> inheritedItemDefs = topic.getInheritedItemDefs();
		if( !Check.isEmpty(inheritedItemDefs) )
		{
			PropBagEx txcInheritCollections = txInheritedConsts.newSubtree("inherited.collections");
			for( ItemDefinitionScript itemDefScript : inheritedItemDefs )
			{
				PropBagEx collectionNode = txcInheritCollections.newSubtree("inherited.collection");
				collectionNode.setNode(ATTR_UUID, itemDefScript.getEntity().getUuid());
				String script = itemDefScript.getScript();
				if( !Check.isEmpty(script) )
				{
					collectionNode.setNode("", script);
				}
			}
		}

		// Dynamic filtering
		if( topic.isVirtual() )
		{
			PropBagEx txDynamic = tx.newSubtree(DYNAMIC_FILTERING);
			String virtualisationPath = topic.getVirtualisationPath();
			String virtualisationId = topic.getVirtualisationId();

			// Xpath
			if( !Check.isEmpty(virtualisationPath) )
			{
				txDynamic.setNode(XPATH, virtualisationPath);
			}

			// Source
			if( !Check.isEmpty(virtualisationId) )
			{
				txDynamic.setNode(SOURCE, virtualisationId);
			}
		}

		// Attributes
		List<Attribute> attributes = topic.getAttributes();
		if( !Check.isEmpty(attributes) )
		{
			PropBagEx txAttributes = tx.newSubtree("attributes");
			for( Attribute attr : attributes )
			{
				PropBagEx keyNode = txAttributes.newSubtree("attribute");
				keyNode.setNode(ATTR_KEY, attr.getKey());
				String value = attr.getValue();
				if( !Check.isEmpty(value) )
				{
					keyNode.setNode("", value);
				}
			}
		}

		// Key resources

		if( isDynamicTopic )
		{
			List<HierarchyTopicDynamicKeyResources> dynamicKeyResources = hierarchyService.getDynamicKeyResource(id);
			if( !Check.isEmpty(dynamicKeyResources) )
			{
				PropBagEx keyResources = tx.newSubtree("keyresources");
				for( HierarchyTopicDynamicKeyResources ht : dynamicKeyResources )
				{
					PropBagEx keyNode = keyResources.newSubtree("keyresource");
					keyNode.setNode(ATTR_UUID, ht.getUuid());
					keyNode.setNode(ATTR_VERSION, ht.getVersion());
				}
			}
		}
		else
		{
			List<Item> keyResources = topic.getKeyResources();
			if( !Check.isEmpty(keyResources) )
			{
				PropBagEx txKeyResources = tx.newSubtree("keyresources");
				for( Item item : keyResources )
				{
					PropBagEx keyNode = txKeyResources.newSubtree("keyresource");
					keyNode.setNode(ATTR_UUID, item.getUuid());
					keyNode.setNode(ATTR_VERSION, item.getVersion());
				}
			}
		}

	}

	// Lots of database calls could be mashed into one
	private void buildTopicFromXml(HierarchyTopic topic, PropBagEx xml, String id, boolean isDynamicTopic)
	{
		// Name
		topic.setName(getBundle(xml, NAME));

		// Short description
		topic.setShortDescription(getBundle(xml, SHORT_DESCRIPTION));

		// Long description
		topic.setLongDescription(getBundle(xml, LONG_DESCRIPTION));

		// Sub topic headings
		topic.setSubtopicsSectionName(getBundle(xml, SUBTOPICS_HEADING));

		// Results heading
		topic.setResultsSectionName(getBundle(xml, RESULTS_HEADING));

		// Hide empty subtopics (true/false)
		topic.setHideSubtopicsWithNoResults(Boolean.parseBoolean(xml.getNode(HIDE_EMPTY_SUBTOPICS)));

		// Advanced search
		String powerSearchUuid = xml.getNode(ADVANCED_SEARCH);
		if( !Check.isEmpty(powerSearchUuid) )
		{
			PowerSearch powerSearch = powerSearchService.getByUuid(powerSearchUuid);
			topic.setAdvancedSearch(powerSearch);
		}

		// Display items (true/false)
		topic.setShowResults(Boolean.parseBoolean(xml.getNode(DISPLAY_ITEMS)));

		// Constraints ===================================================
		PropBagEx constraints = xml.getSubtree(CONSTRAINTS);

		if( constraints != null )
		{
			// Freetext
			topic.setFreetext(constraints.getNode(FREETEXT));

			// Schemata
			PropBagIterator schemaIter = constraints.iterator(SCHEMATA_SCHEMA);
			List<SchemaScript> additionalSchemas = new ArrayList<SchemaScript>();
			Map<String, String> schemaMap = new LinkedHashMap<String, String>();

			for( PropBagEx schemaXml : schemaIter )
			{
				schemaMap.put(schemaXml.getNode(ATTR_UUID), schemaXml.getNode("/"));
			}

			for( Schema entity : schemaService.getByUuids(schemaMap.keySet()) )
			{
				SchemaScript script = new SchemaScript(entity, schemaMap.get(entity.getUuid()));
				additionalSchemas.add(script);
			}

			topic.setAdditionalSchemas(additionalSchemas);

			// Collections (itemdefs)
			PropBagIterator itemDefIter = constraints.iterator(COLLS_COLLECTION);
			List<ItemDefinitionScript> additionalItemDefs = new ArrayList<ItemDefinitionScript>();
			Map<String, String> itemDefMap = new LinkedHashMap<String, String>();

			for( PropBagEx collectionXml : itemDefIter )
			{
				itemDefMap.put(collectionXml.getNode(ATTR_UUID), collectionXml.getNode("/"));
			}

			for( ItemDefinition entity : itemDefService.getByUuids(itemDefMap.keySet()) )
			{
				ItemDefinitionScript script = new ItemDefinitionScript(entity, itemDefMap.get(entity.getUuid()));
				additionalItemDefs.add(script);
			}
			topic.setAdditionalItemDefs(additionalItemDefs);
		}

		// Inherited constraints ==========================================
		PropBagEx inheritedConstraints = xml.getSubtree(INHERITED_CONSTRAINTS);

		if( inheritedConstraints != null )
		{
			// Inherited freetext
			topic.setInheritFreetext(Boolean.parseBoolean(inheritedConstraints.getNode(INHERIT_FREETEXT)));

			// Inherited Schemata
			PropBagIterator inhSchemaIter = inheritedConstraints.iterator(SCHEMATA_SCHEMA);
			List<SchemaScript> inheritedSchemas = new ArrayList<SchemaScript>();
			Map<String, String> inhSchemaMap = new LinkedHashMap<String, String>();

			for( PropBagEx inhSchemaXml : inhSchemaIter )
			{
				inhSchemaMap.put(inhSchemaXml.getNode(ATTR_UUID), inhSchemaXml.getNode("/"));
			}

			for( Schema entity : schemaService.getByUuids(inhSchemaMap.keySet()) )
			{
				SchemaScript script = new SchemaScript(entity, inhSchemaMap.get(entity.getUuid()));
				inheritedSchemas.add(script);
			}
			topic.setInheritedSchemas(inheritedSchemas);

			// Inherited Collections (itemdefs)
			PropBagIterator inhItemDefIter = inheritedConstraints.iterator(COLLS_COLLECTION);
			List<ItemDefinitionScript> inheritedItemDefs = new ArrayList<ItemDefinitionScript>();
			Map<String, String> inhItemDefMap = new LinkedHashMap<String, String>();

			for( PropBagEx inhCollectionXml : inhItemDefIter )
			{
				inhItemDefMap.put(inhCollectionXml.getNode(ATTR_UUID), inhCollectionXml.getNode("/"));
			}

			for( ItemDefinition entity : itemDefService.getByUuids(inhItemDefMap.keySet()) )
			{
				ItemDefinitionScript script = new ItemDefinitionScript(entity, inhItemDefMap.get(entity.getUuid()));
				inheritedItemDefs.add(script);
			}
			topic.setInheritedItemDefs(inheritedItemDefs);
		}

		// Dynamic filtering
		PropBagEx dynamic = xml.getSubtree(DYNAMIC_FILTERING);

		if( dynamic != null )
		{
			// Xpath
			topic.setVirtualisationPath(dynamic.getNode(XPATH));

			// Source
			topic.setVirtualisationId(dynamic.getNode(SOURCE));
		}

		// Attributes
		PropBagIterator attributeIter = xml.iterator(ATTRS_ATTRIBUTE);

		for( PropBagEx attribute : attributeIter )
		{
			topic.setAttribute(attribute.getNode(ATTR_KEY), attribute.getNode("/"));
		}

		// Key resources
		PropBagIterator keyResourceIter = xml.iterator(KRS_KEYRESOURCE);
		List<ItemId> itemIds = new ArrayList<ItemId>();

		for( PropBagEx keyresourceXml : keyResourceIter )
		{
			itemIds.add(
				new ItemId(keyresourceXml.getNode(ATTR_UUID), Integer.parseInt(keyresourceXml.getNode(ATTR_VERSION))));
		}
		// Add key items to dynamic topic
		if( isDynamicTopic )
		{
			for( ItemId itemId : itemIds )
			{
				hierarchyService.addKeyResource(id, itemId);
			}
		}
		else
		{
			List<Item> keyResources = new ArrayList<Item>();
			for( Item i : itemService.queryItemsByItemIds(itemIds).values() )
			{
				keyResources.add(i);
			}
			topic.setKeyResources(keyResources);
		}
	}

	private LanguageBundle getBundle(PropBagEx xml, String path)
	{
		return LangUtils.getBundleFromXml(xml.getSubtree(path));
	}

	private HierarchyTopic getTopicOrFail(String uuid)
	{
		HierarchyTopic topic = hierarchyService.getHierarchyTopicByUuid(uuid);
		if( topic == null )
		{
			throw new IllegalArgumentException("Topic '" + uuid + "' not found");
		}
		return topic;
	}
}