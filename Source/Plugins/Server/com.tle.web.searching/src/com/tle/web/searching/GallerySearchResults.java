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

package com.tle.web.searching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.search.DefaultSearch;
import com.tle.common.settings.standard.SearchSettings;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.mimetypes.MimeTypesSearchResults;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.searching.itemlist.GalleryItemList;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.generic.AbstractPrototypeSection;

@SuppressWarnings("nls")
@Bind
public class GallerySearchResults extends AbstractPrototypeSection<SearchResultsModel>
	implements
		StandardSearchResultType
{
	private static PluginResourceHelper helper = ResourcesService.getResourceHelper(GallerySearchResults.class);

	@Inject
	private GalleryItemList itemList;
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private ConfigurationService configService;

	private Cache<String, Collection<String>> mimeCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS)
		.softValues().build();
	private final static String CACHE_KEY = "mime_types";

	@Override
	public AbstractItemList<com.tle.web.itemlist.item.StandardItemListEntry, ?> getCustomItemList()
	{
		return itemList;
	}

	@Override
	public String getKey()
	{
		return helper.key("result.type.gallery");
	}

	@Override
	public String getValue()
	{
		return "gallery";
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
		tree.registerInnerSection(itemList, parentId);

	}

	private Collection<String> getMimeTypeRestrictions()
	{
		final Collection<String> cachedMimes = mimeCache.getIfPresent(CACHE_KEY);
		if( cachedMimes == null )
		{
			MimeTypesSearchResults mimes = mimeTypeService.searchByMimeType("image/", 0, -1);
			ArrayList<String> imageTypes = Lists.newArrayList();
			for( MimeEntry imageMime : mimes.getResults() )
			{
				imageTypes.add(imageMime.getType());
			}
			mimeCache.put(CACHE_KEY, imageTypes);
			return imageTypes;
		}
		return cachedMimes;
	}

	@Override
	public void addResultTypeDefaultRestrictions(DefaultSearch defaultSearch)
	{
		defaultSearch.addMust(FreeTextQuery.FIELD_REAL_THUMB, "true");
		defaultSearch.setMimeTypes(getMimeTypeRestrictions());
	}

	@Override
	public boolean isDisabled()
	{
		final SearchSettings settings = getSearchSettings();
		return settings.isSearchingDisableGallery();

	}

	private SearchSettings getSearchSettings()
	{
		return configService.getProperties(new SearchSettings());
	}
}
