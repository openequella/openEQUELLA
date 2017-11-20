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

package com.tle.core.harvester.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.harvester.AbstractHarvesterProtocol;
import com.tle.core.plugins.AbstractPluginService;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.ItemNotFoundException;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemSelect;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.HarvesterProfileService;
import com.tle.core.harvester.LearningEdge;
import com.tle.core.harvester.old.TLEItem;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.helper.ItemHelper.ItemHelperSettings;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.operations.AbstractEditMetadataOperation;
import com.tle.core.schema.service.SchemaService;
import com.tle.web.viewurl.ViewItemUrlFactory;

@Bind(LearningEdge.class)
@Singleton
@SuppressWarnings("nls")
public class LearningEdgeImpl implements LearningEdge
{
	private static final Logger LOGGER = Logger.getLogger(LearningEdgeImpl.class);
	protected static final String KEY_PFX = AbstractPluginService.getMyPluginId(LearningEdgeImpl.class)+".";

	@Inject
	private ItemService itemService;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private HarvesterProfileService harvesterProfileService;
	@Inject
	private SchemaService schemaService;
	@Inject
	private FreeTextService freetextService;
	@Inject
	private InitialiserService initialiserService;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private ItemOperationFactory workflowFactory;

	@Override
	@Transactional
	public PropBagEx newItem(String uuid, ItemDefinition itemDef) throws Exception
	{
		ItemPack<Item> pack = itemService.operation(null, workflowFactory.create());
		Item newItem = pack.getItem();

		newItem.setItemDefinition(itemDef);

		if( uuid != null && !uuid.isEmpty() )
		{
			newItem.setUuid(uuid);
		}

		return itemHelper.convertToXml(pack, new ItemHelperSettings(false));
	}

	@Override
	public PropBagEx newItem(ItemDefinition itemDef) throws Exception
	{
		return newItem(null, itemDef);
	}

	@Override
	@Transactional
	public PropBagEx newVersion(TLEItem item)
	{
		ItemId key = new ItemId(item.getUuid(), item.getVersion());
		ItemPack pack = itemService.operation(key, workflowFactory.newVersion(false));

		return new PropBagEx(itemHelper.convertToXml(pack, new ItemHelperSettings(false)).toString());
	}

	@Override
	@Transactional
	public PropBagEx modifyInPlace(TLEItem item) throws Exception
	{
		ItemId key = new ItemId(item.getUuid(), item.getVersion());
		ItemPack pack = itemService.operation(key, workflowFactory.startEdit(true));
		AbstractEditMetadataOperation abs = workflowFactory.editExistingItemMetadata();
		abs.setItemPack(pack);
		pack = itemService.operation(key, abs);
		return new PropBagEx(itemHelper.convertToXml(pack, new ItemHelperSettings(false)).toString());
	}

	private SearchResults<Item> getResults(Search searchReq)
	{
		return freetextService.search(searchReq, 0, 100);
	}

	@Override
	@Transactional
	public boolean itemExists(String uuid)
	{
		try
		{
			return itemService.getUnsecure(new ItemId(uuid, itemService.getLatestVersion(uuid))) != null;
		}
		catch( ItemNotFoundException e )
		{
			return false;
		}
	}

	@Override
	@Transactional
	public TLEItem getItem(String uuid, ItemDefinition newItemDef) throws Exception
	{
		Item item = itemService.get(new ItemId(uuid, itemService.getLatestVersion(uuid)));

		ItemDefinition oldItemDef = item.getItemDefinition();

		if( !oldItemDef.getUuid().equalsIgnoreCase(newItemDef.getUuid()) )
		{
			throw new RuntimeApplicationException(CurrentLocale.get(KEY_PFX + "learning.exists"));
		}

		PropBagEx itemXml = new PropBagEx(item.getItemXml().getXml());
		String modified = itemXml.getNode("item/olddatemodified");
		String created = itemXml.getNode("item/olddatecreated");

		if( Check.isEmpty(modified) )
		{
			modified = itemXml.getNode("item/datemodified");
		}
		if( Check.isEmpty(created) )
		{
			created = itemXml.getNode("item/datecreated");
		}
		Date modifiedDate = parseOrNow(modified);
		Date createdDate = parseOrNow(created);

		return new TLEItem(uuid, item.getVersion(), createdDate, modifiedDate);
	}

	@Override
	@Transactional
	public TLEItem getLatestItem(Search searchReq) throws Exception
	{

		SearchResults<Item> results = getResults(searchReq);

		PropBagEx xml = new PropBagEx().newSubtree("results");
		for( Item item : results.getResults() )
		{
			if( item != null )
			{
				ItemDefinition itemDef = initialiserService.initialise(item.getItemDefinition());
				item = initialiserService.initialise(item);
				item.setItemDefinition(itemDef);

				PropBagEx itemXml = null;
				final ItemSelect select = searchReq.getSelect();
				if( select != null && select.isItemXml() )
				{
					itemXml = convertToXml(new ItemPack(item, itemService.getItemXmlPropBag(item), null));
				}
				else
				{
					itemXml = convertToXml(new ItemPack(item, new PropBagEx(), null));
				}
				itemXml.setNode("item/url", urlFactory.createFullItemUrl(item.getItemId()).getHref());
				PropBagEx resultXml = new PropBagEx(item.getItemXml().getXml());
				itemXml.setNode("item/olddatemodified", resultXml.getNode("item/olddatemodified"));
				itemXml.setNode("item/olddatecreated", resultXml.getNode("item/olddatecreated"));
				itemXml.setNode("item/datemodified", resultXml.getNode("item/datemodified"));
				itemXml.setNode("item/datecreated", resultXml.getNode("item/datecreated"));
				xml.newSubtree("result").append("", itemXml);
			}
		}
		xml.setNode("@count", results.getCount());
		xml.setNode("available", results.getAvailable());

		int max = 0;
		for( PropBagEx x : xml.iterateAll("result/xml/item") )
		{
			int version = x.getIntNode("@version");
			if( version > max )
			{
				xml = x;
				max = version;
			}
		}

		if( max == 0 )
		{
			return null;
		}
		else
		{
			String uuid = xml.getNode("@id");
			int version = xml.getIntNode("@version");
			String modified = xml.getNode("olddatemodified");
			String created = xml.getNode("olddatecreated");

			Date creationDate = null;
			Date modifiedDate = null;
			try
			{
				if( Check.isEmpty(modified) )
				{
					modified = xml.getNode("datemodified");
				}
				if( Check.isEmpty(created) )
				{
					created = xml.getNode("datecreated");
				}

				modifiedDate = parseOrNow(modified);
				creationDate = parseOrNow(created);
			}
			catch( ParseException ex )
			{
				LOGGER.error(CurrentLocale.get("com.tle.core.harvester.learning.badformat", "/xml/item/olddatemodified",
					created));
				throw ex;
			}

			return new TLEItem(uuid, version, creationDate, modifiedDate);
		}
	}

	private Date parseOrNow(String aDate) throws ParseException
	{
		return Check.isEmpty(aDate) ? new Date() : new UtcDate(aDate, Dates.ISO_WITH_GENERAL_TIMEZONE).toDate();
	}

	private String newDateIfNone(String aDate)
	{
		if( aDate == null || aDate.isEmpty() )
		{
			aDate = new UtcDate(new Date()).format(Dates.ISO_WITH_GENERAL_TIMEZONE);
		}
		return aDate;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void uploadItem(PropBagEx itemXml, ItemDefinition itemDefUuid) throws Exception
	{
		String dateModified = newDateIfNone(itemXml.getNode("item/datemodified"));
		String dateCreated = newDateIfNone(itemXml.getNode("item/datecreated"));

		itemXml.setNode("item/olddatemodified", dateModified);
		itemXml.setNode("item/olddatecreated", dateCreated);
		final boolean submit = true;
		final boolean unlock = true;

		final ItemId key = getItemId(itemXml);
		final ItemPack pack = new ItemPack();
		Item bean = new Item();
		pack.setItem(bean);
		pack.setXml(itemXml);
		itemHelper.convertToItemPack(pack, new ItemHelperSettings(false));

		ItemDefinition itemDef = itemDefinitionService.getByUuid(bean.getItemDefinition().getUuid());
		if( itemDef == null )
		{
			itemDef = itemDefUuid;
		}
		bean.setItemDefinition(itemDef);

		List<WorkflowOperation> ops = new ArrayList<WorkflowOperation>();
		ops.add(workflowFactory.editMetadata(pack));
		if( submit && !bean.isNewItem() && bean.getStatus() != ItemStatus.DRAFT )
		{
			ops.add(workflowFactory.redraft());
		}
		ops.add(workflowFactory.metadataMap());
		if( submit )
		{
			ops.add(workflowFactory.submit());
		}
		ops.add(
			workflowFactory.saveWithOperations(unlock, (List<WorkflowOperation>) pack.getAttribute("preSaveOperations"),
				(List<WorkflowOperation>) pack.getAttribute("postSaveOperations")));

		ItemPack<Item> ret = itemService.operation(key, ops.toArray(new WorkflowOperation[ops.size()]));
		ret.getItem().setNewItem(false);

		itemHelper.convertToXml(ret, new ItemHelperSettings(true)).toString();

	}

	private ItemId getItemId(PropBagEx xml)
	{
		String uuid = xml.getNode("item/@id");
		int version = xml.getIntNode("item/@version", 0);
		if( version == 0 )
		{
			version = itemService.getLatestVersion(uuid);
		}
		if( uuid.length() == 0 )
		{
			uuid = UUID.randomUUID().toString();
			xml.setNode("item/@id", uuid);
		}
		xml.setNode("item/@version", version);

		return new ItemId(uuid, version);
	}

	private PropBagEx convertToXml(ItemPack pack)
	{
		return itemHelper.convertToXml(pack, new ItemHelperSettings(true));
	}

	@Override
	public void updateProfileRunDate(HarvesterProfile profile, Date date)
	{
		harvesterProfileService.updateLastRun(profile, date);
	}

	@Override
	@Transactional
	public ItemDefinition getItemDefByUuid(String itemdef)
	{
		return itemDefinitionService.getByUuid(itemdef);
	}

	@Override
	@Transactional
	public long getSchemaByUuid(String itemdef)
	{
		return itemDefinitionService.getSchemaIdForCollectionUuid(itemdef);
	}

	@Override
	@Transactional
	public String transformSchema(long schema, PropBagEx xml, String xsltName) throws Exception
	{
		return schemaService.transformForImport(schema, xsltName, xml);
	}
}
