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

package com.tle.web.viewurl;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.registry.TreeRegistry;
import com.tle.web.viewable.NewViewableItemState;
import com.tle.web.viewable.ViewableItem;

@NonNullByDefault
@Bind(ViewItemUrlFactory.class)
@Singleton
public class ViewItemUrlFactoryImpl implements ViewItemUrlFactory
{
	private static final String VIEWITEM_PATH = "/viewitem/viewitem.do"; //$NON-NLS-1$
	@Inject
	private InstitutionService institutionService;
	@Inject
	private SectionsController sectionsController;
	@Inject
	private TreeRegistry treeRegistry;

	@Override
	public ViewItemUrl createFullItemUrl(ItemKey itemId)
	{
		return doCreateFromItemId(null, null, itemId, UrlEncodedString.BLANK, null, ViewItemUrl.FLAG_FULL_URL);
	}

	private SectionInfo createViewInfo(@Nullable SectionInfo existing, String itemdir)
	{
		HttpServletRequest request = null;
		HttpServletResponse response = null;
		if( existing != null )
		{
			request = existing.getRequest();
			response = existing.getResponse();
		}
		SectionTree tree = treeRegistry.getTreeForPath(VIEWITEM_PATH);
		URI institutionUri = institutionService.getInstitutionUri();
		URI itemDirUri;
		try
		{
			itemDirUri = new URI(null, null, itemdir, null);
		}
		catch( URISyntaxException e )
		{
			throw new IllegalArgumentException(e);
		}
		URI relativeItemDir = institutionUri.relativize(institutionUri.resolve(itemDirUri));
		return sectionsController.createInfo(tree, '/' + relativeItemDir.getPath(), request, response, existing, null,
			null);
	}

	@Override
	public ViewItemUrl createItemUrl(SectionInfo info, ItemKey itemId, int flags)
	{
		return createItemUrl(info, itemId, UrlEncodedString.BLANK, null, flags);
	}

	@Override
	public ViewItemUrl createItemUrl(SectionInfo info, ItemKey itemId)
	{
		return createItemUrl(info, itemId, UrlEncodedString.BLANK);
	}

	@Override
	public ViewItemUrl createItemUrl(SectionInfo info, ItemKey item, UrlEncodedString filePath)
	{
		return createItemUrl(info, item, filePath, null, 0);
	}

	@Override
	public ViewItemUrl createItemUrl(SectionInfo info, ItemKey itemId, UrlEncodedString filePath, String queryString,
		int flags)
	{
		return doCreateFromItemId(info, null, itemId, filePath, queryString, flags);
	}

	private ViewItemUrl doCreateFromItemId(@Nullable SectionInfo existing, @Nullable String contextPath, ItemKey itemId,
		UrlEncodedString filePath, String queryString, int flags)
	{
		NewViewableItemState viewable = new NewViewableItemState();
		viewable.setItemId(itemId);
		if( contextPath != null )
		{
			viewable.setContext(contextPath);
		}
		String itemdir = viewable.getItemdir(institutionService);
		return new ViewItemUrl(createViewInfo(existing, itemdir), itemdir, filePath, queryString, institutionService,
			flags);
	}

	@Override
	public ViewItemUrl createItemUrl(SectionInfo info, String itemServletContext, ItemKey itemId,
		UrlEncodedString filePath, int flags)
	{
		return doCreateFromItemId(info, itemServletContext, itemId, filePath, null, flags);
	}

	@Override
	public ViewItemUrl createItemUrl(SectionInfo info, ViewableItem<Item> viewableItem)
	{
		return createItemUrl(info, viewableItem, UrlEncodedString.BLANK, 0);
	}

	@Override
	public ViewItemUrl createItemUrl(SectionInfo info, ViewableItem<Item> viewableItem, int flags)
	{
		return createItemUrl(info, viewableItem, UrlEncodedString.BLANK, flags);
	}

	@Override
	public ViewItemUrl createItemUrl(SectionInfo info, ViewableItem<Item> viewableItem, UrlEncodedString filePath,
		int flags)
	{
		return createItemUrl(info, viewableItem, filePath, null, flags);
	}

	@Override
	public ViewItemUrl createItemUrl(SectionInfo info, ViewableItem<Item> viewableItem, UrlEncodedString filePath,
		@Nullable String queryString, int flags)
	{
		String itemdir = viewableItem.getItemdir();
		return new ViewItemUrl(createViewInfo(info, itemdir), itemdir, filePath, queryString, institutionService,
			flags);
	}
}
