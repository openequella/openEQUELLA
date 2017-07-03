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

package com.tle.web.api.item.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.PathUtils;
import com.tle.common.URLUtils;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.AttachmentBean;
import com.tle.web.api.item.interfaces.beans.FileBean;
import com.tle.web.api.item.interfaces.beans.FolderBean;
import com.tle.web.api.item.interfaces.beans.GenericFileBean;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.api.item.interfaces.beans.RootFolderBean;
import com.tle.web.viewable.ViewItemLinkFactory;

@Bind(ItemLinkService.class)
@Singleton
@SuppressWarnings("nls")
public class ItemLinkServiceImpl implements ItemLinkService
{
	private static final String PATH_ITEMAPI = "api/item/";
	private static final String PATH_FILE_API = "api/file/";

	private static final String CONTEXT_FILE_CONTENT = "content";
	private static final String CONTEXT_FILE_DIR = "dir";

	private static final String REL_SELF = "self";
	private static final String REL_VIEW = "view";
	private static final String REL_CONTENT = "content";
	private static final String REL_DIR = "dir";
	private static final String REL_THUMB = "thumbnail";

	@Inject
	private ViewItemLinkFactory linkFactory;
	@Inject
	private InstitutionService institutionService;

	@Override
	public URI getItemURI(ItemKey itemKey)
	{
		try
		{
			return new URI(getItemURLStr(itemKey));
		}
		catch( URISyntaxException e )
		{
			throw new RuntimeException(e);
		}
	}

	private String getItemURLStr(ItemKey itemKey)
	{
		ItemId itemId = ItemId.fromKey(itemKey);
		return institutionService.institutionalise(PATH_ITEMAPI + itemId + '/');
	}

	@Override
	public ItemBean addLinks(ItemBean itemBean)
	{
		final Map<String, String> links = Maps.newHashMap();
		final ItemId itemId = new ItemId(itemBean.getUuid(), itemBean.getVersion());
		links.put(REL_SELF, getItemURLStr(itemId));
		links.put(REL_VIEW, linkFactory.createViewLink(itemId).getHref());

		final List<AttachmentBean> attachments = itemBean.getAttachments();
		if( attachments != null )
		{
			for( AttachmentBean attachmentBean : attachments )
			{
				final Map<String, String> attachLinks = Maps.newHashMap();
				attachLinks.put(REL_VIEW,
					linkFactory.createViewAttachmentLink(itemId, attachmentBean.getUuid()).getHref());
				attachLinks.put(REL_THUMB,
					linkFactory.createThumbnailAttachmentLink(itemId, attachmentBean.getUuid()).getHref());
				attachmentBean.set("links", attachLinks);
			}
		}
		itemBean.set("links", links);
		return itemBean;
	}

	@Override
	public EquellaItemBean addLinks(EquellaItemBean itemBean)
	{
		final Map<String, String> links = Maps.newHashMap();
		final ItemId itemId = new ItemId(itemBean.getUuid(), itemBean.getVersion());
		links.put(REL_SELF, getItemURLStr(itemId));
		links.put(REL_VIEW, linkFactory.createViewLink(itemId).getHref());

		final List<AttachmentBean> attachments = itemBean.getAttachments();
		if( attachments != null )
		{
			for( AttachmentBean attachmentBean : attachments )
			{
				final Map<String, String> attachLinks = Maps.newHashMap();
				attachLinks.put(REL_VIEW,
					linkFactory.createViewAttachmentLink(itemId, attachmentBean.getUuid()).getHref());
				attachLinks.put(REL_THUMB,
					linkFactory.createThumbnailAttachmentLink(itemId, attachmentBean.getUuid()).getHref());
				attachmentBean.set("links", attachLinks);
			}
		}
		itemBean.set("links", links);
		return itemBean;
	}

	@Override
	public URI getFileDirURI(StagingFile staging, String path)
	{
		try
		{
			return new URI(institutionService
				.institutionalise(PathUtils.filePath(PATH_FILE_API, staging.getUuid(), CONTEXT_FILE_DIR, path)));
		}
		catch( URISyntaxException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public URI getFileContentURI(StagingFile staging, String path)
	{
		try
		{
			return new URI(institutionService
				.institutionalise(PathUtils.filePath(PATH_FILE_API, staging.getUuid(), CONTEXT_FILE_CONTENT, path)));
		}
		catch( URISyntaxException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public RootFolderBean addLinks(RootFolderBean stagingBean)
	{
		final Map<String, String> links = Maps.newHashMap();
		final String selfUrl = institutionService
			.institutionalise(PathUtils.filePath(PATH_FILE_API, stagingBean.getUuid(), CONTEXT_FILE_DIR));
		links.put(REL_SELF, selfUrl);
		links.put(REL_DIR, selfUrl);
		links.put(REL_CONTENT, institutionService
			.institutionalise(PathUtils.filePath(PATH_FILE_API, stagingBean.getUuid(), CONTEXT_FILE_CONTENT)));
		stagingBean.set("links", links);
		return stagingBean;
	}

	@Override
	public FileBean addLinks(StagingFile staging, FileBean fileBean, String fullPath)
	{
		addGenericFileLinks(staging, fileBean, fullPath);
		return fileBean;
	}

	@Override
	public FolderBean addLinks(StagingFile staging, FolderBean folderBean, String fullPath)
	{
		addGenericFileLinks(staging, folderBean, fullPath);
		return folderBean;
	}

	private void addGenericFileLinks(StagingFile staging, GenericFileBean fileBean, String fullPath)
	{
		final Map<String, String> links = Maps.newHashMap();
		final String selfUrl = institutionService.institutionalise(URLUtils
			.urlEncode(PathUtils.filePath(PATH_FILE_API, staging.getUuid(), CONTEXT_FILE_DIR, fullPath), false));
		links.put(REL_SELF, selfUrl);
		links.put(REL_DIR, selfUrl);
		links.put(REL_CONTENT, institutionService.institutionalise(URLUtils
			.urlEncode(PathUtils.filePath(PATH_FILE_API, staging.getUuid(), CONTEXT_FILE_CONTENT, fullPath), false)));
		fileBean.set("links", links);
	}
}
