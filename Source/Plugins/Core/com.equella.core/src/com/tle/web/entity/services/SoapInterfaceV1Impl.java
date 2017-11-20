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

package com.tle.web.entity.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.dytech.devlib.Base64;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.valuebean.ItemKey;
import com.dytech.edge.common.valuebean.SearchRequest;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.searching.Search;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.helper.ItemHelper.ItemHelperSettings;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.search.LegacySearch;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.xml.service.XmlService;
import com.tle.web.viewurl.ViewItemUrlFactory;

@Bind
@Singleton
public class SoapInterfaceV1Impl extends AbstractSoapService implements com.tle.core.remoting.SoapInterfaceV1
{
	private static final Logger LOGGER = Logger.getLogger(SoapInterfaceV1Impl.class);
	private static final int MAX_QUERY_RESULTS = 100;

	@Inject
	private ItemService itemService;
	@Inject
	private ItemDefinitionService itemdefService;
	@Inject
	private FreeTextService freetextService;
	@Inject
	private InitialiserService initialiserService;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private XmlService xmlService;
	@Inject
	private ItemOperationFactory workflowFactory;

	@SuppressWarnings("nls")
	private PropBagEx doSave(PropBagEx item, final boolean submit, boolean unlock)
	{
		final ItemId key = getItemId(item);
		final ItemPack<Item> pack = new ItemPack<>();
		Item bean = new Item();
		pack.setItem(bean);
		pack.setXml(item);
		itemHelper.convertToItemPack(pack, new ItemHelperSettings(false));

		ItemDefinition itemDef = itemdefService.getByUuid(bean.getItemDefinition().getUuid());
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
		List<WorkflowOperation> preSave = pack.getAttribute("preSaveOperations");
		List<WorkflowOperation> postSave = pack.getAttribute("postSaveOperations");
		ops.add(workflowFactory.saveWithOperations(unlock, preSave, postSave));

		ItemPack<Item> ret = itemService.operation(key, ops.toArray(new WorkflowOperation[ops.size()]));
		ret.getItem().setNewItem(false);

		return convertToXml(ret);
	}

	private ItemId getItemId(PropBagEx xml)
	{
		String uuid = xml.getNode("item/@id"); //$NON-NLS-1$
		int version = xml.getIntNode("item/@version", 0); //$NON-NLS-1$
		if( version == 0 )
		{
			version = itemService.getLatestVersion(uuid);
		}
		if( uuid.length() == 0 )
		{
			uuid = UUID.randomUUID().toString();
			xml.setNode("item/@id", uuid); //$NON-NLS-1$
		}
		xml.setNode("item/@version", version); //$NON-NLS-1$

		return new ItemId(uuid, version);
	}

	@Override
	public String newItem(String ssid, String itemdefid)
	{
		authenticate(ssid);
		PropBagEx item = convert(itemService.operation(null, workflowFactory.create()));
		item.setNode("item/@itemdefid", itemdefid); //$NON-NLS-1$
		return item.toString();
	}

	@Override
	public String startEdit(String ssid, String uuid, int version, String itemdefid, boolean bModifyAttach)

	{
		Preconditions.checkNotNull(uuid);

		authenticate(ssid);

		ItemId key = new ItemId(uuid, version);
		ItemPack pack = itemService.operation(key, workflowFactory.startEdit(bModifyAttach));
		return convert(pack).toString();
	}

	@Override
	public String stopEdit(String ssid, String itemXML, boolean bSubmit)
	{
		authenticate(ssid);
		return doSave(new PropBagEx(itemXML), bSubmit, true).toString();
	}

	@Override
	public void cancelEdit(String ssid, String uuid, int version, String itemdefid)
	{
		authenticate(ssid);
		//This looks like a staging leak (no staging ID to pass to cancelEdit)
		itemService.operation(new ItemId(uuid, version), workflowFactory.cancelEdit(null, true));
	}

	@Override
	public void forceUnlock(String ssid, String uuid, int version, String itemdefid)
	{
		itemService.forceUnlock(itemService.get(new ItemId(uuid, version)));
	}

	@Override
	public void deleteItem(String ssid, String uuid, int version, String itemdefUuid)
	{
		authenticate(ssid);
		itemService.operation(new ItemId(uuid, version), workflowFactory.delete(), workflowFactory.save());
	}

	@Override
	public void uploadAttachment(String ssid, String stagingid, String filename, String data, boolean overwrite)
	{
		authenticate(ssid);
		byte[] bytes = new Base64().decode(data);
		StagingFile staging = new StagingFile(stagingid);
		try
		{
			fileSystemService.write(staging, filename, new ByteArrayInputStream(bytes), !overwrite);
		}
		catch( IOException ex )
		{
			LOGGER.error("Error writing file", ex);
			throw new RuntimeApplicationException("Error writing file on server");
		}
	}

	@Override
	public void deleteAttachment(String ssid, String stagingid, String fileName)
	{
		authenticate(ssid);
		fileSystemService.removeFile(new StagingFile(stagingid), fileName);
	}

	@Override
	public void unzipFile(String ssid, String uuid, String zipfile, String outpath)
	{
		try
		{
			authenticate(ssid);
			fileSystemService.unzipFile(new StagingFile(uuid), zipfile, outpath);
		}
		catch( IOException ex )
		{
			LOGGER.error("Error unzipping file", ex);
			throw new RuntimeApplicationException("Error unzipping file on server");
		}
	}

	@Override
	public String searchItems(String ssid, String searchReqStr, int offset, int limit)
	{
		authenticate(ssid);
		if( limit == -1 )
		{
			limit = MAX_QUERY_RESULTS;
		}
		SearchRequest searchReq = xmlService.deserialiseFromXml(getClass().getClassLoader(), searchReqStr);
		Search sr = new LegacySearch(searchReq, itemdefService);
		FreetextSearchResults<FreetextResult> results = freetextService.search(sr, offset, limit);

		PropBagEx xml = new PropBagEx().newSubtree("results"); //$NON-NLS-1$
		for( Item item : results.getResults() )
		{
			if( item != null )
			{
				// Hibernate, "transparent" my arze
				ItemDefinition itemDef = initialiserService.initialise(item.getItemDefinition());
				item = initialiserService.initialise(item);
				item.setItemDefinition(itemDef);

				PropBagEx itemXml = null;
				if( Objects.equals(searchReq.getSelect(), "*") ) //$NON-NLS-1$
				{
					itemXml = convertToXml(new ItemPack(item, itemService.getItemXmlPropBag(item), null));
				}
				else
				{
					itemXml = convertToXml(new ItemPack(item, new PropBagEx(), null));
				}
				itemXml.setNode("item/url", urlFactory.createFullItemUrl(item.getItemId()).getHref()); //$NON-NLS-1$
				xml.newSubtree("result").append("", itemXml); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		xml.setNode("@count", results.getCount()); //$NON-NLS-1$
		xml.setNode("available", results.getAvailable()); //$NON-NLS-1$

		return xml.toString();
	}

	@Override
	public int queryCount(String ssid, String[] itemdefs, String where)
	{
		authenticate(ssid);
		return freetextService.totalCount(Lists.newArrayList(itemdefs), where);
	}

	@Override
	public String getItem(String ssid, String itemUuid, int version, String itemdef, String select)
	{
		authenticate(ssid);
		ItemPack pack = itemService.getItemPack(new ItemId(itemUuid, version));
		PropBagEx xml = convertToXml(pack);
		return xml.toString();
	}

	@Override
	@SuppressWarnings("nls")
	public String enumerateWritableItemDefs(String ssid)
	{
		authenticate(ssid);

		Collection<ItemDefinition> itemdefs = itemdefService.enumerateCreateable();
		PropBagEx retBag = new PropBagEx();
		for( ItemDefinition itemDef : itemdefs )
		{
			PropBagEx def = retBag.newSubtree("itemdef");
			def.setNode("id", itemDef.getId());
			def.setNode("uuid", itemDef.getUuid());
			def.setNode("name", CurrentLocale.get(itemDef.getName()));
			def.setNode("system", itemDef.isSystemType());
		}

		return retBag.toString();
	}

	@Override
	public ItemKey[][] enumerateItemDependencies(String ssid, ItemKey[] items, boolean recurse)
	{
		authenticate(ssid);
		return new DependancyWebHelper(itemService).enumerateDependancies(items, recurse);
	}

	private PropBagEx convertToXml(ItemPack pack)
	{
		return itemHelper.convertToXml(pack, new ItemHelperSettings(true));
	}

	private PropBagEx convert(ItemPack pack)
	{
		return itemHelper.convertToXml(pack, new ItemHelperSettings(false));
	}

	public static class DependancyWebHelper
	{
		private static final Log LOGGER = LogFactory.getLog(DependancyWebHelper.class);

		private final ItemService itemService;

		public DependancyWebHelper(ItemService itemService)
		{
			this.itemService = itemService;
		}

		public ItemKey[][] enumerateDependancies(ItemKey[] keys, boolean recurse)
		{
			ItemKey[][] results = new ItemKey[keys.length][];
			for( int i = 0; i < keys.length; i++ )
			{
				results[i] = enumerateDependancies(keys[i], recurse);
			}
			return results;
		}

		public ItemKey[] enumerateDependancies(ItemKey key, boolean recurse)
		{
			if( recurse )
			{
				Set<ItemKey> processed = new HashSet<ItemKey>();
				Set<ItemKey> depends = new HashSet<ItemKey>();
				List<ItemKey> stillToProcess = new ArrayList<ItemKey>();

				stillToProcess.add(key);

				while( !stillToProcess.isEmpty() )
				{
					ItemKey currentKey = stillToProcess.remove(0);
					if( !processed.contains(currentKey) )
					{
						ItemKey[] curDepends = enumerateDependancies(currentKey);
						for( int i = 0; i < curDepends.length; i++ )
						{
							if( !depends.contains(curDepends[i]) )
							{
								depends.add(curDepends[i]);
							}
							stillToProcess.add(curDepends[i]);
						}
						processed.add(currentKey);
					}
				}
				return depends.toArray(new ItemKey[depends.size()]);
			}
			else
			{
				return enumerateDependancies(key);
			}
		}

		public ItemKey[] enumerateDependancies(ItemKey key)
		{
			Set<ItemKey> depends = new HashSet<ItemKey>();
			try
			{
				PropBagEx plan = itemService.getItemPack(new ItemId(key.getUuid(), key.getVersion())).getXml();
				key.setVersion(plan.getIntNode("item/@version", 1)); //$NON-NLS-1$
				depends.add(key);
			}
			catch( NotFoundException e )
			{
				LOGGER.error("Error getting item " + key); //$NON-NLS-1$
			}

			return depends.toArray(new ItemKey[depends.size()]);
		}
	}
}
