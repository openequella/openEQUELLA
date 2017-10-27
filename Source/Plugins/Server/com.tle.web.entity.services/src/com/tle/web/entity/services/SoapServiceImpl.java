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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;

import com.tle.core.item.standard.service.ItemStandardService;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.log4j.Logger;

import com.dytech.devlib.Base64;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.valuebean.SearchRequest;
import com.dytech.edge.exceptions.ItemNotFoundException;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Comment;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemSelect;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.common.util.Dates;
import com.tle.common.util.EnumUtils.EnumMask;
import com.tle.common.util.EnumUtils.EnumMaskBuilder;
import com.tle.common.util.UtcDate;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.helper.ItemHelper.ItemHelperSettings;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.core.item.serializer.ItemSerializerXml;
import com.tle.core.item.serializer.XMLStreamer;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.operations.CloneFactory;
import com.tle.core.item.standard.operations.workflow.StatusOperation;
import com.tle.core.item.standard.service.ItemCommentService;
import com.tle.core.item.standard.service.ItemCommentService.CommentFilter;
import com.tle.core.item.standard.service.ItemCommentService.CommentOrder;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.search.LegacySearch;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.user.UserService;
import com.tle.core.soap.service.SoapXMLService;
import com.tle.freetext.FreetextIndex;
import com.tle.web.viewurl.ViewItemUrlFactory;

/**
 * Implementation of SoapService41 + SoapService50
 *
 * @author aholland
 */
@Bind
@Singleton
public class SoapServiceImpl implements SoapService50
{
	private static final Logger LOGGER = Logger.getLogger(SoapServiceImpl.class);

	/**
	 * Maps enums to the bitmask defined for <code>getComments</code>. Don't
	 * change the order or position of existing enums.
	 */
	private static final EnumMask<CommentFilter> COMMENT_FILTER_MASK = EnumMaskBuilder.with(CommentFilter.class)
			.add(CommentFilter.MUST_HAVE_COMMENT, CommentFilter.MUST_HAVE_RATING, CommentFilter.NOT_ANONYMOUS_OR_GUEST,
					CommentFilter.ONLY_MOST_RECENT_PER_USER)
			.build();
	/**
	 * Maps enums to the order defined for <code>getComments</code>. Don't
	 * change the order or position of existing enums.
	 */
	private static final EnumMask<CommentOrder> COMMENT_ORDER_MAPPING = EnumMaskBuilder.with(CommentOrder.class)
			.add(CommentOrder.REVERSE_CHRONOLOGICAL, CommentOrder.CHRONOLOGICAL, CommentOrder.HIGHEST_RATED,
					CommentOrder.LOWEST_RATED)
			.build();

	@Inject
	private ItemService itemService;
	@Inject
	private ItemStandardService itemStandardService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private ItemCommentService itemCommentService;
	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private SchemaService schemaService;
	@Inject
	private FreeTextService freetextService;
	@Inject
	private FreetextIndex freetextIndex;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private UserService userService;
	@Inject
	private ItemOperationFactory editFactory;
	@Inject
	private CloneFactory cloneFactory;
	@Inject
	private SoapXMLService soapXML;
	@Inject
	private WebServiceContext webServiceContext;
	@Inject
	private ItemOperationFactory workflowFactory;
	@Inject
	private ItemSerializerService itemSerializerService;

	private WebAuthenticationDetails getDetails()
	{
		return userService.getWebAuthenticationDetails(
				(HttpServletRequest) webServiceContext.getMessageContext().get(AbstractHTTPDestination.HTTP_REQUEST));
	}

	@Override
	public String login(String username, String password)
	{
		return soapXML.convertUserToXML(userService.login(username, password, getDetails(), true).getUserBean())
				.toString();
	}

	@Override
	public String loginWithToken(String token)
	{
		return soapXML.convertUserToXML(userService.loginWithToken(token, getDetails(), true).getUserBean()).toString();
	}

	@Override
	public void logout()
	{
		userService.logoutToGuest(getDetails(), false);
	}

	@Override
	public void keepAlive()
	{
		userService.keepAlive();
	}

	@Override
	public String newItem(String collectionUuid)
	{
		ItemDefinition collection = collectionService.getByUuid(collectionUuid);
		final PropBagEx item = soapXML
				.convertItemPackToXML(itemService.operation(null, workflowFactory.create(collection)), false);
		return item.toString();
	}

	@Override
	public String editItem(String itemUuid, int itemVersion, boolean modifyingAttachments)
	{
		if( itemUuid == null )
		{
			throw new IllegalArgumentException("itemUuid cannot be null"); //$NON-NLS-1$
		}

		final ItemPack pack = itemService.operation(getItemId(itemUuid, itemVersion),
				editFactory.startEdit(modifyingAttachments));
		return soapXML.convertItemPackToXML(pack, true).toString();
	}

	@SuppressWarnings({"unchecked", "nls"})
	@Override
	public String saveItem(final String itemXML, final boolean submit)
	{
		final PropBagEx itemBag = new PropBagEx(itemXML);
		final ItemId key = getItemId(itemBag);
		final ItemPack pack = new ItemPack();
		Item bean = new Item();
		pack.setItem(bean);
		pack.setXml(itemBag);
		itemHelper.convertToItemPack(pack, new ItemHelperSettings(false));

		ItemDefinition itemDef = collectionService.getByUuid(bean.getItemDefinition().getUuid());
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
				workflowFactory.saveWithOperations(true, (List<WorkflowOperation>) pack.getAttribute("preSaveOperations"),
						(List<WorkflowOperation>) pack.getAttribute("postSaveOperations")));

		ItemPack<Item> ret = itemService.operation(key, ops.toArray(new WorkflowOperation[ops.size()]));
		ret.getItem().setNewItem(false);

		return soapXML.convertItemPackToXML(pack, true).toString();
	}

	@Override
	public String newVersionItem(String itemUuid, int itemVersion, boolean copyAttachments)
	{
		final ItemPack pack = itemService.operation(getItemId(itemUuid, itemVersion),
				workflowFactory.newVersion(copyAttachments));

		return soapXML.convertItemPackToXML(pack, true).toString();
	}

	@Override
	public String cloneItem(String itemUuid, int itemVersion, boolean copyAttachments)
	{
		final ItemPack pack = itemService.operation(getItemId(itemUuid, itemVersion),
				cloneFactory.clone(copyAttachments, false));

		return soapXML.convertItemPackToXML(pack, true).toString();
	}

	@Override
	public void archiveItem(String itemUuid, int itemVersion)
	{
		itemService.operation(getItemId(itemUuid, itemVersion), workflowFactory.archive(), workflowFactory.save());
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

		return getItemId(uuid, version);
	}

	private ItemId getItemId(String uuid, int version)
	{
		if( version == 0 )
		{
			return new ItemId(uuid, itemService.getLiveItemVersion(uuid));
		}
		else if( version == -1 )
		{
			return new ItemId(uuid, itemService.getLatestVersion(uuid));
		}
		return new ItemId(uuid, version);
	}

	@Override
	public void cancelItemEdit(String itemUuid, int itemVersion)
	{
		// This looks like a staging leak (no staging ID to pass to cancelEdit)
		itemService.operation(getItemId(itemUuid, itemVersion), workflowFactory.cancelEdit(null, true));
	}

	@Override
	public void unlock(String itemUuid, int itemVersion)
	{
		itemService.forceUnlock(itemService.get(getItemId(itemUuid, itemVersion)));
	}

	@Override
	public void deleteItem(String itemUuid, int itemVersion)
	{
		final ItemId itemId = getItemId(itemUuid, itemVersion);
		itemStandardService.delete(itemId, false, true, true);
	}

	@Override
	public boolean itemExists(String itemUuid, int itemVersion)
	{
		try
		{
			itemService.get(getItemId(itemUuid, itemVersion));
			return true;
		}
		catch( ItemNotFoundException n )
		{
			return false;
		}
	}

	@Override
	public void uploadFile(String stagingId, String filename, String base64Data, boolean overwrite)
	{
		// decode and upload file
		byte[] bytes = new Base64().decode(base64Data);
		StagingFile staging = new StagingFile(stagingId);
		try
		{
			fileSystemService.write(staging, filename, new ByteArrayInputStream(bytes), !overwrite);
		}
		catch( IOException ex )
		{
			LOGGER.error("Error writing file", ex);
			throw new RuntimeApplicationException("Error writing file to server");
		}
	}

	@Override
	public void deleteFile(String stagingId, String filename)
	{
		fileSystemService.removeFile(new StagingFile(stagingId), filename);
	}

	@Override
	public void unzipFile(String stagingId, String zipfile, String outpath)
	{
		try
		{
			fileSystemService.unzipFile(new StagingFile(stagingId), zipfile, outpath);
		}
		catch( IOException ex )
		{
			LOGGER.error("Error unzipping file", ex);
			throw new RuntimeApplicationException("Error unzipping file on the server");
		}
	}

	@SuppressWarnings("nls")
	@Override
	public String searchItems(String freetext, String[] collectionUuids, String whereClause, boolean onlyLive,
							  int orderType, boolean reverseOrder, int offset, int length)
	{
		Preconditions.checkArgument(length <= 50, "Length must be less than or equal to 50");

		if( length < 0 )
		{
			length = 50;
		}

		ItemSelect is = new ItemSelect();
		is.setAttachments(false);
		is.setBadurls(false);
		is.setCollaborators(false);
		is.setDescription(true);
		is.setDrm(true);
		is.setHistory(false);
		is.setItemdef(true);
		is.setItemXml(true);
		is.setModeration(true);
		is.setName(true);

		DefaultSearch ls = createSearch(freetext, collectionUuids, whereClause, onlyLive, orderType, reverseOrder);
		ls.setSelect(is);
		FreetextSearchResults<FreetextResult> results = freetextService.search(ls, offset, length);

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
	@SuppressWarnings("nls")
	public String searchItemsFast(String freetext, String[] collectionUuids, String whereClause, boolean onlyLive,
								  int orderType, boolean reverseOrder, int offset, int length, String[] resultCategories)
	{
		Preconditions.checkArgument(length <= 50, "Length must be less than or equal to 50");

		if( length < 0 )
		{
			length = 50;
		}

		final SearchResults<ItemIdKey> results = freetextService.searchIds(
				createSearch(freetext, collectionUuids, whereClause, onlyLive, orderType, reverseOrder), offset, length);

		// No results - return immediately
		if( results.getResults().isEmpty() )
		{
			return "<results available=\"0\" />";
		}

		Set<String> cats = Collections.emptySet();
		if( resultCategories != null )
		{
			cats = Sets.newHashSet(resultCategories);
		}
		List<Long> itemIds = Lists.newArrayList();
		for( ItemIdKey id : results.getResults() )
		{
			itemIds.add(id.getKey());
		}
		ItemSerializerXml serializer = itemSerializerService.createXmlSerializer(itemIds, cats);
		StringWriter strWriter = new StringWriter();
		XMLStreamer streamer = new XMLStreamer(strWriter);
		streamer.startElement("results");
		streamer.addAttribute("available", results.getAvailable());
		// Write out the XML
		for( ItemIdKey result : results.getResults() )
		{
			streamer.startElement("result");
			streamer.addAttribute("uuid", result.getUuid());
			streamer.addAttribute("version", result.getVersion());

			serializer.writeXmlResult(streamer, result.getKey());
			streamer.endElement(); // </result>
		}
		streamer.endElement();
		streamer.finished();

		return strWriter.toString();
	}

	private DefaultSearch createSearch(String freetext, String[] collectionUuids, String whereClause, boolean onlyLive,
									   int orderType, boolean reverseOrder)
	{
		SearchRequest searchReq = new SearchRequest();
		searchReq.setQuery(freetext);
		if( collectionUuids != null && collectionUuids.length > 0 )
		{
			searchReq.setItemdefs(Arrays.asList(collectionUuids));
		}
		searchReq.setWhere(whereClause);
		searchReq.setOnlyLive(onlyLive);
		searchReq.setOrderType(orderType);
		searchReq.setSortReverse(reverseOrder);

		return new LegacySearch(searchReq, collectionService);
	}

	private DefaultSearch createSearch(String[] collectionUuids, String where)
	{
		FreeTextQuery query = WhereParser.parse(where);

		Collection<String> collections = null;
		if( collectionUuids != null && collectionUuids.length > 0 )
		{
			collections = Arrays.asList(collectionUuids);
		}

		DefaultSearch search = new DefaultSearch();
		search.setFreeTextQuery(query);
		search.setCollectionUuids(collections);
		return search;
	}

	@Override
	public int queryCount(String[] collectionUuids, String where)
	{
		return freetextService.countsFromFilters(Collections.singleton(createSearch(collectionUuids, where)))[0];
	}

	@Override
	public int[] queryCounts(String[] collectionUuids, String[] wheres)
	{
		if( Check.isEmpty(wheres) )
		{
			return new int[0];
		}

		List<Search> searches = new ArrayList<Search>();
		for( String where : wheres )
		{
			searches.add(createSearch(collectionUuids, where));
		}

		return freetextService.countsFromFilters(searches);
	}

	@Override
	public String facetCount(String freetext, String[] collectionUuids, String whereClause, String[] facetXpaths)
			throws Exception
	{
		PropBagEx rv = new PropBagEx("<facets/>"); //$NON-NLS-1$

		if( Check.isEmpty(facetXpaths) )
		{
			return rv.toString();
		}

		DefaultSearch s = createSearch(collectionUuids, whereClause);
		s.setQuery(freetext);

		// Key is XPath without "/xml"; Value is the original XPath
		final ImmutableMap<String, String> mappedFacets = Maps.uniqueIndex(Arrays.asList(facetXpaths),
				new Function<String, String>()
				{
					@Override
					public String apply(String input)
					{
						return input.substring(input.indexOf('/', 1));
					}
				});
		final Multimap<String, Pair<String, Integer>> vcs = freetextIndex.facetCount(s, mappedFacets.keySet());

		// Entry set is in same order as the passed in facet XPaths
		for( Map.Entry<String, String> fxp : mappedFacets.entrySet() )
		{
			PropBagEx fxml = rv.newSubtree("facet"); //$NON-NLS-1$
			fxml.setNode("@xpath", fxp.getValue()); //$NON-NLS-1$

			for( Pair<String, Integer> valueCount : vcs.get(fxp.getKey()) )
			{
				final String value = valueCount.getFirst();
				if( !Check.isEmpty(value) )
				{
					PropBagEx vxml = fxml.newSubtree("value"); //$NON-NLS-1$
					vxml.setNode("@count", valueCount.getSecond()); //$NON-NLS-1$
					vxml.setNode("", value); //$NON-NLS-1$
				}
			}
		}

		return rv.toString();
	}

	@Override
	public String getItem(String itemUuid, int version, String select)
	{
		ItemPack pack = itemService.getItemPack(getItemId(itemUuid, version));
		PropBagEx xml = soapXML.convertItemPackToXML(pack, true);
		return xml.toString();
	}

	@Override
	public String getSearchableCollections()
	{
		Collection<ItemDefinition> collections = collectionService.enumerateSearchable();
		final PropBagEx collectionsXML = new PropBagEx();
		for( ItemDefinition collection : collections )
		{
			collectionsXML.append("/", soapXML.convertCollectionToXML(collection)); //$NON-NLS-1$
		}

		return collectionsXML.toString();
	}

	@Override
	public String getContributableCollections()
	{
		Collection<ItemDefinition> collections = collectionService.enumerateCreateable();
		final PropBagEx collectionsXML = new PropBagEx();
		for( ItemDefinition collection : collections )
		{
			collectionsXML.append("/", soapXML.convertCollectionToXML(collection)); //$NON-NLS-1$
		}

		return collectionsXML.toString();
	}

	private void itemOp(String itemUuid, int itemVersion, WorkflowOperation op)
	{
		final List<WorkflowOperation> workOps = new ArrayList<WorkflowOperation>();
		workOps.add(workflowFactory.startLock());
		workOps.add(op);
		workOps.add(workflowFactory.save());
		itemService.operation(getItemId(itemUuid, itemVersion), workOps.toArray(new WorkflowOperation[workOps.size()]));
	}

	@Override
	public void setOwner(String itemUuid, int itemVersion, String userId)
	{
		itemOp(itemUuid, itemVersion, workflowFactory.changeOwner(userId));
	}

	@Override
	public void addSharedOwner(String itemUuid, int itemVersion, final String userId)
	{
		itemOp(itemUuid, itemVersion, workflowFactory.modifyCollaborators(userId, false));
	}

	@Override
	public void removeSharedOwner(String itemUuid, int itemVersion, final String userId)
	{
		itemOp(itemUuid, itemVersion, workflowFactory.modifyCollaborators(userId, true));
	}

	@Override
	public String acceptTask(String itemUuid, int itemVersion, String taskId, boolean unlock)
	{
		itemService.operation(new ItemTaskId(itemUuid, itemVersion, taskId),
				workflowFactory.accept(taskId, null, null), workflowFactory.status(), workflowFactory.saveUnlock(unlock));
		return taskId;
	}

	@Override
	public String rejectTask(String itemUuid, int itemVersion, String taskId, String rejectMessage, String toStep,
							 boolean unlock)
	{
		itemService.operation(new ItemTaskId(itemUuid, itemVersion, taskId),
				workflowFactory.reject(taskId, rejectMessage, toStep, null), workflowFactory.status(),
				workflowFactory.saveUnlock(unlock));

		return taskId;
	}

	@Override
	public String getSchema(final String schemaUuid)
	{
		return soapXML.convertSchemaToXML(schemaService.getByUuid(schemaUuid)).toString();
	}

	@Override
	public String getCollection(final String collectionUuid)
	{
		return soapXML.convertCollectionToXML(collectionService.getByUuid(collectionUuid)).toString();
	}

	@Override
	public String[] getItemFilenames(String itemUuid, int itemVersion, String path, boolean system)
	{
		final List<String> results = new ArrayList<String>();
		final ItemFile fileHandle = itemFileService.getItemFile(new ItemId(itemUuid, itemVersion), null);
		final Collection<String> list = fileSystemService.grep(fileHandle, path, "**"); //$NON-NLS-1$
		for( String file : list )
		{
			// Ensure we filter out system directories
			if( system || !(file.charAt(0) == '_' && file.contains("/")) ) //$NON-NLS-1$
			{
				results.add(file);
			}
		}
		Collections.sort(results, new Comparator<String>()
		{
			@Override
			public int compare(String arg0, String arg1)
			{
				return arg0.compareToIgnoreCase(arg1);
			}
		});
		return results.toArray(new String[results.size()]);
	}

	@Override
	@SuppressWarnings("nls")
	public String getComment(String itemUuid, int itemVersion, String commentUuid)
	{
		Comment c = itemCommentService.getComment(itemService.getUnsecure(getItemId(itemUuid, itemVersion)),
				commentUuid);

		PropBagEx xml = new PropBagEx();
		addCommentXml(xml, c);
		return xml.getSubtree("comment").toString();
	}

	@Override
	@SuppressWarnings("nls")
	public String getComments(String itemUuid, int itemVersion, int filter, int order, int limit)
	{
		final Item item = itemService.getUnsecure(getItemId(itemUuid, itemVersion));
		final EnumSet<CommentFilter> filterEnums = COMMENT_FILTER_MASK.enumsForMask(filter);
		final CommentOrder orderEnum = COMMENT_ORDER_MAPPING.getForBit(order);

		final List<Comment> comments = itemCommentService.getComments(item, filterEnums, orderEnum, limit);

		PropBagEx xml = new PropBagEx();
		xml.setNodeName("comments");
		xml.setNode("@average", item.getRating());
		for( Comment c : comments )
		{
			addCommentXml(xml, c);
		}
		return xml.toString();
	}

	@SuppressWarnings("nls")
	private void addCommentXml(PropBagEx xml, Comment c)
	{
		PropBagEx cx = xml.newSubtree("comment");
		cx.setNode("uuid", c.getUuid());

		int rating = c.getRating();
		if( rating > 0 )
		{
			cx.setNode("rating", rating);
		}

		String text = c.getComment();
		if( !Check.isEmpty(text) )
		{
			cx.setNode("text", text);
		}

		if( !c.isAnonymous() )
		{
			String owner = c.getOwner();
			if( !Check.isEmpty(owner) )
			{
				cx.setNode("owner", owner);
			}
		}

		cx.setNode("dateCreated", new UtcDate(c.getDateCreated()).format(Dates.ISO_WITH_TIMEZONE));
	}

	@Override
	public void deleteComment(String itemUuid, int itemVersion, String commentUuid)
	{
		itemCommentService.deleteComment(getItemId(itemUuid, itemVersion), commentUuid);
	}

	@Override
	public void addComment(String itemUuid, int itemVersion, String commentText, int rating, boolean anonymous)
	{
		itemCommentService.addComment(getItemId(itemUuid, itemVersion), commentText, rating, anonymous);
	}
}
