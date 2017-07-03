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

package com.tle.web.api.item.resource.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.util.DateUtil;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.FileInfo;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.item.Comment;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemEditingException;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemLock;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.interfaces.CsvList;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.item.security.ItemSecurityConstants;
import com.tle.core.item.serializer.ItemCommentSerializer;
import com.tle.core.item.serializer.ItemDeserializerService;
import com.tle.core.item.serializer.ItemHistorySerializer;
import com.tle.core.item.serializer.ItemSerializerItemBean;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.core.item.serializer.where.AllVersionsWhereClause;
import com.tle.core.item.serializer.where.LatestVersionWhereClause;
import com.tle.core.item.serializer.where.SingleItemWhereClause;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemHistoryService;
import com.tle.core.item.service.ItemLockingService;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.service.ItemCommentService;
import com.tle.core.item.standard.service.ItemStandardService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.quickupload.service.QuickUploadService;
import com.tle.core.services.FileSystemService;
import com.tle.exceptions.PrivilegeRequiredException;
import com.tle.web.api.interfaces.beans.BlobBean;
import com.tle.web.api.interfaces.beans.FileListBean;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.ItemLockResource;
import com.tle.web.api.item.interfaces.ItemResource;
import com.tle.web.api.item.interfaces.beans.AttachmentBean;
import com.tle.web.api.item.interfaces.beans.CommentBean;
import com.tle.web.api.item.interfaces.beans.HistoryEventBean;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.api.item.interfaces.beans.ItemExportBean;
import com.tle.web.api.item.interfaces.beans.ItemLockBean;
import com.tle.web.api.item.interfaces.beans.NavigationNodeBean;
import com.tle.web.api.item.interfaces.beans.NavigationTabBean;
import com.tle.web.api.item.interfaces.beans.NavigationTreeBean;
import com.tle.web.api.item.resource.EquellaItemResource;
import com.tle.web.remoting.rest.service.RestImportExportHelper;
import com.tle.web.remoting.rest.service.UrlLinkService;

/**
 * see interface classes for @Path and other annotations
 * 
 * @author larry
 */
@SuppressWarnings("nls")
@Bind(EquellaItemResource.class)
@Singleton
public class ItemResourceImpl implements EquellaItemResource
{
	private static final String DISCOVER_ITEM = "DISCOVER_ITEM";
	private static final String VIEW_ITEM = "VIEW_ITEM";
	private static final Pattern UUID_PLACEHOLDER_PATTERN = Pattern.compile("^uuid:(.*)$");

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemSerializerService itemSerializerService;
	@Inject
	private ItemLinkService itemLinkService;
	@Inject
	private UrlLinkService urlLinkService;
	@Inject
	private ItemDeserializerService itemDeserializerService;
	@Inject
	private ItemCommentService itemCommentService;
	@Inject
	private ItemCommentSerializer itemCommentSerializer;
	@Inject
	private ItemHistoryService itemHistoryService;
	@Inject
	private ItemHistorySerializer itemHistorySerializer;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemStandardService itemStandardService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private FreeTextService freeTextService;
	@Inject
	private QuickUploadService quickUploadService;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ItemLockingService lockingService;

	/**
	 * NB: import|export=true is also an option in the query parameters.
	 */
	@Override
	public EquellaItemBean getItem(UriInfo uriInfo, String uuid, int version, CsvList info)
	{
		List<String> infos = CsvList.asList(info, ItemSerializerService.CATEGORY_ALL);
		ItemId itemId = new ItemId(uuid, version);
		ItemSerializerItemBean serializer = itemSerializerService.createItemBeanSerializer(
			new SingleItemWhereClause(itemId), infos, RestImportExportHelper.isExport(uriInfo), VIEW_ITEM,
			DISCOVER_ITEM);
		return singleItem(uuid, version, serializer, uriInfo);
	}

	@Override
	public Response edit(String uuid, int version, String stagingUuid, String lockId, boolean keepLocked,
		String waitForIndex, String taskUuid, ItemBean itemBean)
	{
		// TODO: remove this HAX
		final EquellaItemBean equellaItemBean = EquellaItemBean.copyFrom(itemBean);
		replacePlaceholderUuids(equellaItemBean);
		equellaItemBean.setUuid(uuid);
		equellaItemBean.setVersion(version);
		boolean ensureOnIndexList = Boolean.parseBoolean(waitForIndex);

		ItemIdKey itemIdKey = itemDeserializerService.edit(equellaItemBean, stagingUuid, lockId, !keepLocked,
			ensureOnIndexList);
		if( ensureOnIndexList )
		{
			freeTextService.waitUntilIndexed(itemIdKey);
		}
		return Response.status(Status.OK).location(itemLinkService.getItemURI(itemIdKey)).build();
	}

	@Override
	public Response newItem(UriInfo uriInfo, String stagingUuid, boolean draft, String waitForIndex, ItemBean itemBean)
	{
		// TODO: remove this HAX
		final EquellaItemBean equellaItemBean = EquellaItemBean.copyFrom(itemBean);
		replacePlaceholderUuids(equellaItemBean);
		final boolean ensureOnIndexList = Boolean.parseBoolean(waitForIndex);

		final ItemIdKey itemId;
		if( RestImportExportHelper.isImport(uriInfo) )
		{
			itemId = itemDeserializerService.importItem(equellaItemBean, stagingUuid, ensureOnIndexList);
		}
		else
		{
			itemId = itemDeserializerService.newItem(equellaItemBean, stagingUuid, draft, ensureOnIndexList, false);
		}
		if( ensureOnIndexList )
		{
			freeTextService.waitUntilIndexed(itemId);
		}
		return Response.status(Status.CREATED).location(itemLinkService.getItemURI(itemId)).build();
	}

	/**
	 * This will generate uuids for attachments using a placeholder uuid of
	 * "uuid:0", "uuid:1", etc and replace any refs in the navigation nodes to
	 * the correct uuid
	 */
	private void replacePlaceholderUuids(ItemBean bean)
	{
		Map<String, String> uuidMap = Maps.newHashMap();
		List<AttachmentBean> attachments = bean.getAttachments();
		if( attachments != null )
		{
			for( AttachmentBean attachment : attachments )
			{
				Matcher matcher = UUID_PLACEHOLDER_PATTERN.matcher(Strings.nullToEmpty(attachment.getUuid()));

				if( matcher.matches() )
				{
					String key = matcher.group(0);
					if( uuidMap.containsKey(key) )
					{
						throw new ItemEditingException("Another attachment is already using this placeholder: " + key);
					}
					// else
					String uuid = UUID.randomUUID().toString();
					uuidMap.put(key, uuid);
					attachment.setUuid(uuid);
				}
			}
		}
		if( !uuidMap.isEmpty() )
		{
			NavigationTreeBean navigation = bean.getNavigation();
			if( navigation != null )
			{
				List<NavigationNodeBean> nodes = navigation.getNodes();
				replaceNavigationUuids(nodes, uuidMap);
			}

			String metadata = bean.getMetadata();
			if( !Check.isEmpty(metadata) )
			{
				PropBagEx xml = new PropBagEx(metadata);
				replaceMetadataUuids(xml, uuidMap);
				bean.setMetadata(xml.toString());
			}
		}

	}

	private void replaceMetadataUuids(PropBagEx metadata, Map<String, String> uuidMap)
	{
		for( PropBagEx node : metadata.iterator() )
		{
			replaceMetadataUuids(node, uuidMap);
			String value = node.getNode();
			if( !Check.isEmpty(value) )
			{
				Matcher matcher = UUID_PLACEHOLDER_PATTERN.matcher(value);
				if( matcher.matches() )
				{
					String key = matcher.group(0);
					if( !uuidMap.containsKey(key) )
					{
						throw new ItemEditingException("No attachment is using this placeholder: " + key);
					}
					node.setNode("", uuidMap.get(key));
				}
			}
		}
	}

	private void replaceNavigationUuids(List<NavigationNodeBean> nodes, Map<String, String> uuidMap)
	{
		if( Check.isEmpty(nodes) )
		{
			return;
		}
		for( NavigationNodeBean node : nodes )
		{
			replaceNavigationUuids(node.getNodes(), uuidMap);
			List<NavigationTabBean> tabs = node.getTabs();
			if( !Check.isEmpty(tabs) )
			{
				for( NavigationTabBean tab : tabs )
				{
					Matcher matcher = UUID_PLACEHOLDER_PATTERN.matcher(tab.getAttachment().getUuid());
					if( matcher.matches() )
					{
						String key = matcher.group(0);
						if( !uuidMap.containsKey(key) )
						{
							throw new ItemEditingException("No attachment is using this placeholder: " + key);
						}
						// else
						tab.getAttachment().setUuid(uuidMap.get(key));
					}

				}
			}

		}

	}

	private EquellaItemBean singleItem(String uuid, int version, ItemSerializerItemBean serializer, UriInfo uriInfo)
	{
		Collection<Long> itemIds = serializer.getItemIds();
		Iterator<Long> iter = itemIds.iterator();
		if( !iter.hasNext() )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		Long itemKey = iter.next();
		if( serializer.hasPrivilege(itemKey, VIEW_ITEM) || serializer.hasPrivilege(itemKey, DISCOVER_ITEM) )
		{
			return serializeOne(itemKey, uuid, version, serializer, uriInfo);
		}

		throw new PrivilegeRequiredException(ItemSecurityConstants.VIEW_ITEM, ItemSecurityConstants.DISCOVER_ITEM);
	}

	private EquellaItemBean serializeOne(Long itemKey, String uuid, int version, ItemSerializerItemBean serializer,
		UriInfo uriInfo)
	{
		int setVersion = version == 0 ? (Integer) serializer.getData(itemKey, AllVersionsWhereClause.ALIAS_VERSION)
			: version;
		EquellaItemBean equellaBean = new EquellaItemBean();
		equellaBean.setUuid(uuid);
		equellaBean.setVersion(setVersion);
		serializer.writeItemBeanResult(equellaBean, itemKey);

		// This check will enforce Administrator credentials, if export=true in
		// parameter string ...
		if( RestImportExportHelper.isExport(uriInfo) )
		{
			ItemExportBean exportBean = buildExportBean(equellaBean);
			equellaBean.setExportDetails(exportBean);
		}
		itemLinkService.addLinks(equellaBean);
		return equellaBean;
	}

	/**
	 * NB: import|export=true is also an option in the query parameters.
	 */
	@Override
	public List<ItemBean> getAllVersions(UriInfo uriInfo, String uuid, CsvList info)
	{
		List<String> infos = CsvList.asList(info, ItemSerializerService.CATEGORY_ALL);
		ItemSerializerItemBean serializer = itemSerializerService.createItemBeanSerializer(
			new AllVersionsWhereClause(uuid), infos, RestImportExportHelper.isExport(uriInfo), VIEW_ITEM,
			DISCOVER_ITEM);
		List<ItemBean> itemBeans = Lists.newArrayList();
		for( Long itemKey : serializer.getItemIds() )
		{
			if( serializer.hasPrivilege(itemKey, VIEW_ITEM) || serializer.hasPrivilege(itemKey, DISCOVER_ITEM) )
			{
				itemBeans.add(serializeOne(itemKey, uuid, 0, serializer, uriInfo));
			}
		}
		return itemBeans;
	}

	@Override
	public ItemBean getLatest(UriInfo uriInfo, String uuid, CsvList info)
	{
		List<String> infos = CsvList.asList(info, ItemSerializerService.CATEGORY_ALL);
		ItemSerializerItemBean serializer = itemSerializerService.createItemBeanSerializer(
			new LatestVersionWhereClause(uuid, false), infos, RestImportExportHelper.isExport(uriInfo), VIEW_ITEM,
			DISCOVER_ITEM);
		return singleItem(uuid, 0, serializer, uriInfo);
	}

	@Override
	public ItemBean getLatestLive(UriInfo uriInfo, String uuid, CsvList info)
	{
		List<String> infos = CsvList.asList(info, ItemSerializerService.CATEGORY_ALL);
		ItemSerializerItemBean serializer = itemSerializerService.createItemBeanSerializer(
			new LatestVersionWhereClause(uuid, true), infos, RestImportExportHelper.isExport(uriInfo), VIEW_ITEM,
			DISCOVER_ITEM);
		return singleItem(uuid, 0, serializer, uriInfo);
	}

	@Override
	public List<CommentBean> getComments(String uuid, int version)
	{
		List<Comment> comments = itemCommentService.getComments(new ItemId(uuid, version), null, null, -1);
		List<CommentBean> commentBeans = Lists.newArrayList();
		if( comments != null )
		{
			for( Comment comment : comments )
			{
				CommentBean bean = itemCommentSerializer.serialize(comment);
				commentBeans.add(bean);
			}
		}
		return commentBeans;
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

	@Override
	public Response deleteItem(String uuid, int version, String waitForIndex, boolean purge)
	{
		boolean ensureOnIndexList = Boolean.parseBoolean(waitForIndex);

		itemStandardService.delete(new ItemId(uuid, version), purge, ensureOnIndexList, false);
		return Response.status(Status.NO_CONTENT).build();
	}

	@Override
	public Response createStagingFromItem(String itemUuid, int itemVersion) throws IOException
	{
		return Response.status(Status.NOT_IMPLEMENTED).build();
	}

	@Override
	public FileListBean listFiles(UriInfo uriInfo, String uuid, int version)
	{
		ItemId itemId = new ItemId(uuid, version);
		checkViewItem(itemId);
		ItemFile itemFile = itemFileService.getItemFile(itemId, null);

		FileEntry base;
		try
		{
			base = fileSystemService.enumerateTree(itemFile, "", null);
		}
		catch( IOException e )
		{
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
		List<BlobBean> blobs = Lists.newArrayList();
		for( FileEntry fileEntry : base.getFiles() )
		{
			buildBlobBeans(itemFile, uuid, version, blobs, fileEntry, "");
		}
		Collections.sort(blobs, new Comparator<BlobBean>()
		{
			@Override
			public int compare(BlobBean o1, BlobBean o2)
			{
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});

		final FileListBean fileList = new FileListBean();
		fileList.setFiles(blobs);
		return fileList;
	}

	private void buildBlobBeans(ItemFile itemFile, String uuid, int version, List<BlobBean> blobs, FileEntry entry,
		String currentPath)
	{
		// Folders are not listed
		if( entry.isFolder() )
		{
			for( FileEntry subEntry : entry.getFiles() )
			{
				buildBlobBeans(itemFile, uuid, version, blobs, subEntry,
					PathUtils.filePath(currentPath, entry.getName()));
			}
		}
		else
		{
			final BlobBean blobBean = new BlobBean();
			final String filename = entry.getName();
			final String filePath = PathUtils.filePath(currentPath, filename);
			try
			{
				String md5CheckSum = fileSystemService.getMD5Checksum(itemFile, filePath);
				blobBean.setEtag("\"" + md5CheckSum + "\"");
			}
			catch( IOException e )
			{
				// Whatever
			}
			blobBean.setName(filePath);
			blobBean.setSize(entry.getLength());
			blobBean.setContentType(mimeService.getMimeTypeForFilename(filename));
			final Map<String, URI> links = new HashMap<>();
			links.put("self",
				urlLinkService.getMethodUriBuilder(ItemResource.class, "readFile").build(uuid, version, filePath));
			blobBean.set("links", links);
			blobs.add(blobBean);
		}
	}

	@Override
	public Response headFile(HttpHeaders headers, String uuid, int version, String path)
	{
		ItemId itemId = new ItemId(uuid, version);
		checkViewItem(itemId);

		ItemFile itemFile = itemFileService.getItemFile(itemId, null);
		ResponseBuilder builder = makeBlobHeaders(itemFile, path);

		String contentType = mimeService.getMimeTypeForFilename(path);
		builder.type(contentType);

		if( !fileSystemService.fileExists(itemFile, path) )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		return builder.build();
	}

	@Override
	public Response readFile(HttpHeaders headers, String uuid, int version, String path)
	{
		ItemId itemId = new ItemId(uuid, version);
		checkViewItem(itemId);
		ItemFile itemFile = itemFileService.getItemFile(itemId, null);

		try
		{
			if( !fileSystemService.fileExists(itemFile, path) )
			{
				return Response.status(Status.NOT_FOUND).build();
			}

			final String etag = headers.getHeaderString(HttpHeaders.IF_NONE_MATCH);
			if( etag != null )
			{
				String md5Checksum = fileSystemService.getMD5Checksum(itemFile, path);
				String quotedChecksum = "\"" + md5Checksum + "\"";
				if( Objects.equals(etag, quotedChecksum) )
				{
					return Response.notModified(quotedChecksum).build();
				}
			}
			final String modifiedSince = headers.getHeaderString(HttpHeaders.IF_MODIFIED_SINCE);
			if( modifiedSince != null )
			{
				final Date lastModified = new Date(fileSystemService.lastModified(itemFile, path));
				if( Objects.equals(modifiedSince, DateUtil.formatDate(lastModified)) )
				{
					return Response.notModified().build();
				}
			}

			ResponseBuilder builder;
			try
			{
				builder = makeBlobHeaders(itemFile, path);
				builder.type(mimeService.getMimeTypeForFilename(path));
				builder.entity(fileSystemService.read(itemFile, path));
			}
			catch( IOException e )
			{
				return Response.status(Status.NOT_FOUND).build();
			}
			return builder.build();
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	// EQUELLA specific endpoints defined by EquellaItemResource

	@Override
	public Response newItemQuick(UriInfo uriInfo, String filename, InputStream binaryData)
	{
		try( InputStream bd = binaryData )
		{
			Map<String, List<String>> params = uriInfo.getQueryParameters();
			Pair<ItemId, Attachment> attInfo = quickUploadService.createOrSelectExisting(bd, filename, params);

			return Response.status(Status.CREATED).location(itemLinkService.getItemURI(attInfo.getFirst())).build();
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public CommentBean getOneComment(UriInfo uriInfo, String uuid, int version, String commentUuid)
	{
		Item item = itemService.get(new ItemId(uuid, version));
		Comment comment = itemCommentService.getComment(item, commentUuid);
		if( comment == null )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		CommentBean bean = itemCommentSerializer.serialize(comment);
		return bean;
	}

	@Override
	public Response postComments(UriInfo uriInfo, String uuid, int version, CommentBean commentBean)
	{
		UserBean postedBy = commentBean.getPostedBy();
		itemCommentService.addComment(new ItemId(uuid, version), commentBean.getComment(), commentBean.getRating(),
			commentBean.isAnonymous(),
			postedBy != null && RestImportExportHelper.isImport(uriInfo) ? postedBy.getId() : "");

		return Response.status(Status.CREATED).build();
	}

	@Override
	public Response deleteComment(UriInfo uriInfo, String uuid, int version, String commentUuid)
	{
		itemCommentService.deleteComment(new ItemId(uuid, version), commentUuid);
		return Response.status(Status.OK).build();
	}

	// private methods

	private ResponseBuilder makeBlobHeaders(ItemFile itemFile, String filename)
	{
		FileInfo fileInfo = fileSystemService.getFileInfo(itemFile, filename);
		ResponseBuilder builder = Response.ok();
		builder.lastModified(new Date(fileSystemService.lastModified(itemFile, filename)));
		builder.header(HttpHeaders.ETAG, fileInfo.getMd5CheckSum());
		builder.header(HttpHeaders.CONTENT_LENGTH, fileInfo.getLength());
		builder.header(HttpHeaders.CONTENT_TYPE, mimeService.getMimeTypeForFilename(fileInfo.getFilename()));
		return builder;
	}

	private void checkViewItem(ItemId itemId)
	{
		ItemSerializerItemBean serializer = itemSerializerService
			.createItemBeanSerializer(new SingleItemWhereClause(itemId), new HashSet<String>(), false, VIEW_ITEM);

		Iterator<Long> iter = serializer.getItemIds().iterator();
		if( !iter.hasNext() )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		Long itemKey = iter.next();
		if( !serializer.hasPrivilege(itemKey, ItemSecurityConstants.VIEW_ITEM) )
		{
			throw new PrivilegeRequiredException(ItemSecurityConstants.VIEW_ITEM);
		}
	}

	private ItemExportBean buildExportBean(EquellaItemBean equellaBean)
	{
		ItemExportBean exportBean = itemSerializerService.getExportDetails(equellaBean);

		List<HistoryEventBean> history = getHistory(equellaBean.getUuid(), equellaBean.getVersion());
		exportBean.setHistory(history);

		ItemLockBean itemLockBean = getItemLock(equellaBean);
		if( itemLockBean != null )
		{
			exportBean.setLock(itemLockBean);
		}

		return exportBean;
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
	private ItemLockBean getItemLock(EquellaItemBean equellaBean)
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
}
