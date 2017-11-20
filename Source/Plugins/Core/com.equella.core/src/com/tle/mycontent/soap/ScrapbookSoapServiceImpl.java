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

package com.tle.mycontent.soap;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.Base64;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.ItemNotFoundException;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.searching.Search.SortType;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.soap.service.SoapXMLService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.mycontent.MyContentConstants;
import com.tle.mycontent.service.MyContentFields;
import com.tle.mycontent.service.MyContentService;
import com.tle.web.myresources.MyResourcesSearch;
import com.tle.web.viewurl.ViewItemUrlFactory;

/**
 * @author Aaron
 */
@Bind(ScrapbookSoapService.class)
@Singleton
public class ScrapbookSoapServiceImpl implements ScrapbookSoapService
{
	@Inject
	private FreeTextService freetextService;
	@Inject
	private SoapXMLService soapXML;
	@Inject
	private ItemService itemService;
	@Inject
	private MyContentService myContentService;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private ItemOperationFactory workflowFactory;

	@Override
	public String search(String query, String[] resourceTypes, String[] mimeTypes, int sortType, int offset, int length)
	{
		MyResourcesSearch search = new MyResourcesSearch();
		search.setItemStatuses(Collections.singletonList(ItemStatus.PERSONAL));
		search.setOwner(CurrentUser.getUserID());
		if( resourceTypes != null )
		{
			search.addMust("/" + MyContentConstants.CONTENT_TYPE_NODE, Arrays.asList(resourceTypes));
		}
		if( mimeTypes != null )
		{
			search.setMimeTypes(Arrays.asList(mimeTypes));
		}
		search.setQuery(query);
		switch( sortType )
		{
			case 0:
				search.setSortType(SortType.RANK);
				break;
			case 2:
				search.setSortType(SortType.NAME);
				break;
			case 5:
				search.setSortType(SortType.DATECREATED);
				break;
			case 1:
			default:
				search.setSortType(SortType.DATEMODIFIED);
				break;
		}
		FreetextSearchResults<FreetextResult> results = freetextService.search(search, offset, length);

		PropBagEx xml = new PropBagEx().newSubtree("results");
		for( Item item : results.getResults() )
		{
			if( item != null )
			{
				PropBagEx itemXml = soapXML
					.convertItemPackToXML(new ItemPack(item, itemService.getItemXmlPropBag(item), null), true);
				itemXml.setNode("item/url", urlFactory.createFullItemUrl(item.getItemId()).getHref());
				xml.newSubtree("result").append("", itemXml);
			}
		}
		xml.setNode("@count", results.getCount());
		xml.setNode("available", results.getAvailable());

		return xml.toString();
	}

	@Override
	public String create(String title, String keywords, String resourceType, String filename, String base64Data)
	{
		// FIXME: dodge. Should check extension points.
		if( resourceType == null || !(resourceType.equals("myresource") || resourceType.equals("mypages")) )
		{
			throw new IllegalArgumentException("resourceType must be one of 'mypages' or 'mycontent'");
		}
		if( Check.isEmpty(base64Data) )
		{
			throw new IllegalArgumentException("base64Data cannot be empty");
		}
		if( Check.isEmpty(filename) )
		{
			throw new IllegalArgumentException("filename cannot be empty");
		}

		// FIXME: duped code from MyResourceContributeSection...also doesn't
		// handle mypages
		final ItemId itemId = new ItemId(UUID.randomUUID().toString(), 1);

		final MyContentFields fields = new MyContentFields();
		fields.setResourceId(resourceType);
		if( keywords != null )
		{
			fields.setTags(keywords.toLowerCase());
		}
		fields.setTitle(title);

		final List<WorkflowOperation> ops = new ArrayList<WorkflowOperation>();
		ops.add(workflowFactory.create(myContentService.getMyContentItemDef(), ItemStatus.PERSONAL));
		ops.add(myContentService.getEditOperation(fields, filename,
			new ByteArrayInputStream(new Base64().decode(base64Data)), null, false, false));
		ops.add(workflowFactory.save());

		final ItemPack newItem = itemService.operation(itemId, ops.toArray(new WorkflowOperation[ops.size()]));
		return soapXML.convertItemPackToXML(newItem, true).toString();
	}

	@Override
	public String update(String itemUuid, String title, String keywords, String filename, String base64Data)
	{
		if( Check.isEmpty(itemUuid) )
		{
			throw new IllegalArgumentException("itemUuid cannot be empty");
		}

		// load the old one
		final ItemId itemId = new ItemId(itemUuid, 1);
		final ItemPack<Item> itemPack = itemService.getItemPack(itemId);
		if( !itemPack.getItem().getOwner().equals(CurrentUser.getUserID()) )
		{
			throw new AccessDeniedException("You cannot update other user's scrapbook items");
		}

		final PropBagEx itemXml = itemPack.getXml();
		final String oldTitle = itemXml.getNode(MyContentConstants.NAME_NODE);
		final String oldKeywords = itemXml.getNode(MyContentConstants.KEYWORDS_NODE);
		final String resourceType = itemXml.getNode(MyContentConstants.CONTENT_TYPE_NODE);

		ByteArrayInputStream stream = null;
		if( !Check.isEmpty(base64Data) )
		{
			final byte[] bytes = new Base64().decode(base64Data);
			stream = new ByteArrayInputStream(bytes);
		}

		final MyContentFields fields = new MyContentFields();
		fields.setResourceId(resourceType);
		fields.setTags(Check.isEmpty(keywords) ? oldKeywords : keywords.toLowerCase());
		fields.setTitle(Check.isEmpty(title) ? oldTitle : title);

		final List<WorkflowOperation> ops = new ArrayList<WorkflowOperation>();
		ops.add(workflowFactory.startEdit(true));
		ops.add(myContentService.getEditOperation(fields, filename, stream, null, false, true));
		ops.add(workflowFactory.save());

		final ItemPack item = itemService.operation(itemId, ops.toArray(new WorkflowOperation[ops.size()]));
		return soapXML.convertItemPackToXML(item, true).toString();
	}

	@Override
	public boolean exists(String itemUuid)
	{
		try
		{
			Item item = itemService.get(new ItemId(itemUuid, 1));
			if( !item.getOwner().equals(CurrentUser.getUserID()) )
			{
				throw new AccessDeniedException("Item does not belong to current user");
			}
			return true;
		}
		catch( ItemNotFoundException inf )
		{
			return false;
		}
	}
}
