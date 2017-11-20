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

package com.tle.web.mycontent.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.dytech.devlib.PropBagEx;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.beans.mycontent.ScrapbookItemBean;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.interfaces.CsvList;
import com.tle.common.searching.Search.SortType;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.util.UnmodifiableIterable;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.service.ItemStandardService;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.mycontent.MyContentConstants;
import com.tle.mycontent.service.MyContentFields;
import com.tle.mycontent.service.MyContentService;
import com.tle.mypages.service.MyPagesService;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.myresources.MyResourcesSearch;
import com.tle.web.viewable.ViewItemLinkFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Dongsheng Cai
 */
@Bind
@Path("scrapbook")
@Api(value = "/scrapbook", description = "scrapbook")
@Produces({"application/json"})
@SuppressWarnings("nls")
@Singleton
public class ScrapbookResource
{
	@Inject
	private FreeTextService freetextService;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemStandardService itemStandardService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private MyContentService myContentService;
	@Inject
	private MyPagesService myPagesService;
	@Inject
	private ItemOperationFactory workflowFactory;
	@Inject
	private StagingService stagingService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private ViewItemLinkFactory linkFactory;

	@GET
	@Path("/")
	@ApiOperation(value = "Search my scrapbook")
	public Response search(
		// @formatter:off
		@ApiParam(value="Query string", required = false)
		@QueryParam("q")
			String query,
		@ApiParam(value="List of resource types", required = false)
		@QueryParam("resourcetypes")
			CsvList csvResourceTypes,
		@ApiParam(value="List of mime types", required = false)
		@QueryParam("mimetypes")
			CsvList csvMimeTypes,
		@ApiParam(value="Search offset", required = false, defaultValue="0") @QueryParam("offset")
			int offset,
		@ApiParam(value="Search results length", required = false, defaultValue = "10", allowableValues = "range[1,50]")
		@QueryParam("length")
		@DefaultValue("10")
			int length,
		@ApiParam(value="sorttype", allowableValues="range[0,2]", required = false) @QueryParam("sorttype")
			int sortType
		)
		// @formatter:on
	{
		final SearchBean<ScrapbookItemBean> result = new SearchBean<ScrapbookItemBean>();

		List<String> resourceTypes = CsvList.asList(csvResourceTypes);
		List<String> mimeTypes = CsvList.asList(csvMimeTypes);
		final List<ScrapbookItemBean> resultItems = Lists.newArrayList();

		MyResourcesSearch search = new MyResourcesSearch();
		search.setItemStatuses(Collections.singletonList(ItemStatus.PERSONAL));
		search.setOwner(CurrentUser.getUserID());
		if( resourceTypes != null )
		{
			search.addMust("/" + MyContentConstants.CONTENT_TYPE_NODE, resourceTypes);
		}
		if( mimeTypes != null )
		{
			search.setMimeTypes(mimeTypes);
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

		List<Item> items = results.getResults();

		for( Item item : items )
		{
			resultItems.add(serializerToScrapbookBean(item));
		}
		result.setStart(results.getOffset());
		result.setLength(results.getCount());
		result.setAvailable(results.getAvailable());
		result.setResults(resultItems);
		return Response.ok(result).build();
	}

	@GET
	@Path("/{uuid}")
	@ApiOperation(value = "Get my scrapbook item")
	public Response getScrapbookItem(@ApiParam(value = "Scrapbook item uuid") @PathParam("uuid") String uuid)
	{
		ItemId itemId = new ItemId(uuid, 1);
		Item item = itemService.get(itemId);
		ScrapbookItemBean scrapbookItem = serializerToScrapbookBean(item);
		return Response.ok(scrapbookItem).build();
	}

	@DELETE
	@Path("/{uuid}")
	@ApiOperation(value = "Delete a scrapbook item")
	public Response delete(@ApiParam(value = "Scrapbook item uuid") @PathParam("uuid") String uuid)
	{
		ItemId itemId = new ItemId(uuid, 1);
		itemStandardService.delete(itemId, true, false, false);
		return Response.noContent().build();
	}

	@POST
	@Path("/")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create new scrapbook item")
	public Response create(@ApiParam(value = "Scrapbook item") ScrapbookItemBean itemBean, @Context UriInfo info)
	{
		return Response.status(Status.CREATED)
			.location(getScrapbookItemURI(createOrUpdateScrapbookItem(null, itemBean, info).getItemId())).build();
	}

	@PUT
	@Path("/{uuid}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update an scrapbook item")
	public Response update(@ApiParam(value = "Scrapbook item uuid") @PathParam("uuid") String uuid,
		@ApiParam(value = "Scrapbook item") ScrapbookItemBean itemBean, @Context UriInfo info)
	{
		return Response.status(Status.OK)
			.location(getScrapbookItemURI(createOrUpdateScrapbookItem(uuid, itemBean, info).getItemId())).build();
	}

	private ItemPack<Item> createOrUpdateScrapbookItem(String uuid, ScrapbookItemBean scrapbookItem, UriInfo info)
	{
		ItemId itemId = null;
		if( uuid != null )
		{
			itemId = new ItemId(uuid, 1);
			itemService.get(itemId);
		}
		else
		{
			itemId = new ItemId(UUID.randomUUID().toString(), 1);
		}
		final MyContentFields fields = new MyContentFields();
		fields.setResourceId(scrapbookItem.getType());
		if( scrapbookItem.getKeywords() != null )
		{
			fields.setTags(scrapbookItem.getKeywords().toLowerCase());
		}
		else
		{
			fields.setTags("");

		}
		fields.setTitle(scrapbookItem.getTitle());

		final List<WorkflowOperation> operations = new ArrayList<WorkflowOperation>();
		if( uuid != null )
		{
			operations.add(workflowFactory.startEdit(true));
		}
		else
		{
			operations.add(workflowFactory.create(myContentService.getMyContentItemDef(), ItemStatus.PERSONAL));
		}

		List<Map<String, String>> pages = scrapbookItem.getPages();
		if( !Check.isEmpty(pages) )
		{
			for( Map<String, String> page : pages )
			{
				String filename = page.get("title") + ".html";

				if( scrapbookItem.getType().equals("mypages") )
				{
					operations.add(myPagesService.getEditOperation(fields, filename,
						new ByteArrayInputStream(page.get("html").getBytes()), false, false));
				}
				else
				{
					operations.add(myContentService.getEditOperation(fields, filename,
						new ByteArrayInputStream(page.get("html").getBytes()), null, false, false));
				}

			}
		}
		Map<String, Object> file = scrapbookItem.getFile();
		if( file != null && file.get("stagingUuid") != null )
		{
			String stagingId = (String) file.get("stagingUuid");
			if( !stagingService.stagingExists(stagingId)
				|| !fileSystemService.fileExists(new StagingFile(stagingId), null) )
			{
				throw new WebApplicationException(Status.NOT_FOUND);
			}

			operations.add(myContentService.getEditOperation(fields, (String) file.get("filename"), null, stagingId,
				false, false));
		}
		else
		{
			operations.add(myContentService.getEditOperation(fields, null, null, null, false, true));
		}
		operations.add(workflowFactory.save());
		return itemService.operation(itemId, operations.toArray(new WorkflowOperation[operations.size()]));
	}

	private URI getScrapbookItemURI(ItemKey itemKey)
	{
		try
		{
			ItemId itemId = ItemId.fromKey(itemKey);
			String url = institutionService.institutionalise("api/scrapbook/" + itemId.getUuid() + '/');
			return new URI(url);
		}
		catch( URISyntaxException e )
		{
			throw new RuntimeException(e);
		}
	}

	private ScrapbookItemBean serializerToScrapbookBean(Item item)
	{
		final ItemId itemId = new ItemId(item.getUuid(), item.getVersion());
		ScrapbookItemBean scrapbookItem = new ScrapbookItemBean();
		scrapbookItem.setUuid(item.getUuid());
		scrapbookItem.setTitle(itemService.getItemXmlPropBag(item).getNode("//name"));
		PropBagEx xmlBag = new PropBagEx();
		xmlBag.setXML(item.getItemXml().getXml());
		String type = xmlBag.getNode("//content_type", "myresource").toLowerCase();
		scrapbookItem.setType(type);
		UnmodifiableIterable<Attachment> attachments = item.getAttachmentsUnmodifiable();

		List<Map<String, String>> htmlPages = new ArrayList<Map<String, String>>();
		for( Attachment att : attachments )
		{
			if( att.getAttachmentType() == AttachmentType.HTML && type.equals("mypages") )
			{
				final HtmlAttachment htmlAttach = (HtmlAttachment) att;
				Map<String, String> page = Maps.newHashMap();
				page.put("title", htmlAttach.getDescription());

				final String filename = htmlAttach.getFilename();

				try( InputStream input = fileSystemService.read(itemFileService.getItemFile(htmlAttach.getItem()),
					filename) )
				{
					java.util.Scanner s = new java.util.Scanner(input);
					s.useDelimiter("\\A");
					page.put("html", s.hasNext() ? s.next() : "");
					s.close();
				}
				catch( Exception ex )
				{

				}

				htmlPages.add(page);
			}
			else if( att.getAttachmentType() == AttachmentType.FILE )
			{
				final FileAttachment fileAtt = (FileAttachment) att;
				Map<String, Object> file = Maps.newHashMap();
				file.put("uuid", fileAtt.getUuid());
				file.put("filename", fileAtt.getFilename());
				file.put("links", Collections.singletonMap("view",
					linkFactory.createViewAttachmentLink(itemId, fileAtt.getUuid()).getHref()));
				scrapbookItem.setFile(file);
			}
		}
		scrapbookItem.setPages(htmlPages);
		scrapbookItem.setKeywords(xmlBag.getNode("//keywords", ""));

		final Map<String, String> links = Maps.newHashMap();
		links.put("self", institutionService.institutionalise("api/scrapbook/" + itemId.getUuid() + '/'));
		if( type.equals("mypages") )
		{
			links.put("view", linkFactory.createViewLink(itemId).getHref() + "viewpages.jsp");
		}
		// There is no view for non page items
		// else
		// {
		// links.put("view", linkFactory.createViewLink(itemId).getHref());
		// }
		scrapbookItem.setLinks(links);
		return scrapbookItem;
	}
}
