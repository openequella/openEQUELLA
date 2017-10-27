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

package com.tle.common.settings.standard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;
import com.tle.common.settings.annotation.PropertyDataList;
import com.tle.common.settings.annotation.PropertyList;

/**
 * @author Aaron
 */
public class SearchSettings implements ConfigurationProperties
{
	public static final int URL_DEPTH_LEVEL_NONE = 0;
	public static final int URL_DEPTH_LEVEL_REFERENCED = 1;
	public static final int URL_DEPTH_LEVEL_REFERENCED_AND_LINKED = 2;

	public static final int DEFAULT_TITLE_BOOST_VALUE = 5;
	public static final int DEFAULT_DESCRIPTION_BOOST_VALUE = 3;
	public static final int DEFAULT_ATTACHMENT_BOOST_VALUE = 2;

	private static final long serialVersionUID = 1;

	private static final String SEARCH = "search"; //$NON-NLS-1$

	private final Map<String, SearchFilter> filterMap = new ConcurrentHashMap<String, SearchFilter>();

	@Property(key = "search.authenticatefeedsbydefault")
	private boolean authenticateFeedsByDefault;
	@Property(key = "search.shownonlivecheckbox")
	private boolean searchingShowNonLiveCheckbox;
	@Property(key = "search.disableGallery")
	private boolean searchingDisableGallery;
	@Property(key = "search.disableVideos")
	private boolean searchingDisableVideos;
	@Property(key = "gallery.filecount.disabled")
	private boolean fileCountDisabled;

	@Property(key = "freetext.urlharvest.level")
	private int urlLevel = URL_DEPTH_LEVEL_NONE;

	@Property(key = "searching.term.boost.title")
	private int titleBoost = DEFAULT_TITLE_BOOST_VALUE;
	@Property(key = "searching.term.boost.description")
	private int descriptionBoost = DEFAULT_DESCRIPTION_BOOST_VALUE;
	@Property(key = "searching.term.boost.attachment")
	private int attachmentBoost = DEFAULT_ATTACHMENT_BOOST_VALUE;

	@Property(key = "search.defaultsort")
	private String defaultSearchSort;

	@PropertyDataList(key = SEARCH + ".filters", type = SearchFilter.class)
	private final List<SearchFilter> filters = new ArrayList<SearchFilter>()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean add(SearchFilter element)
		{
			filterMap.put(element.getId(), element);
			return super.add(element);
		}

		@Override
		public void clear()
		{
			filterMap.clear();
			super.clear();
		}

		@Override
		public boolean remove(Object element)
		{
			filterMap.remove(element);
			return super.remove(element);
		}
	};

	public List<SearchFilter> getFilters()
	{
		return filters;
	}

	public SearchFilter getSearchFilter(String filterId)
	{
		return filterMap.get(filterId);
	}

	public static class SearchFilter implements ConfigurationProperties
	{
		private static final long serialVersionUID = 1;

		@Property(key = "id")
		private String id;
		@Property(key = "name")
		private String name;
		@PropertyList(key = "mimetypes")
		private List<String> mimeTypes = new ArrayList<String>();

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public List<String> getMimeTypes()
		{
			return mimeTypes;
		}

		public void setMimeTypes(List<String> mimeTypes)
		{
			this.mimeTypes = mimeTypes;
		}
	}

	public boolean isSearchingShowNonLiveCheckbox()
	{
		return searchingShowNonLiveCheckbox;
	}

	public void setSearchingShowNonLiveCheckbox(boolean searchingShowNonLiveCheckbox)
	{
		this.searchingShowNonLiveCheckbox = searchingShowNonLiveCheckbox;
	}

	public String getDefaultSearchSort()
	{
		return defaultSearchSort;
	}

	public void setDefaultSearchSort(String defaultSearchSort)
	{
		this.defaultSearchSort = defaultSearchSort;
	}

	public boolean isAuthenticateFeedsByDefault()
	{
		return authenticateFeedsByDefault;
	}

	public void setAuthenticateFeedsByDefault(boolean authenticateFeedsByDefault)
	{
		this.authenticateFeedsByDefault = authenticateFeedsByDefault;
	}

	public int getUrlLevel()
	{
		return urlLevel;
	}

	public void setUrlLevel(int urlLevel)
	{
		this.urlLevel = urlLevel;
	}

	public int getTitleBoost()
	{
		return titleBoost;
	}

	public void setTitleBoost(int titleBoost)
	{
		this.titleBoost = titleBoost;
	}

	public int getDescriptionBoost()
	{
		return descriptionBoost;
	}

	public void setDescriptionBoost(int descriptionBoost)
	{
		this.descriptionBoost = descriptionBoost;
	}

	public int getAttachmentBoost()
	{
		return attachmentBoost;
	}

	public void setAttachmentBoost(int attachmentBoost)
	{
		this.attachmentBoost = attachmentBoost;
	}

	public boolean isSearchingDisableGallery()
	{
		return searchingDisableGallery;
	}

	public void setSearchingDisableGallery(boolean searchingDisableGallery)
	{
		this.searchingDisableGallery = searchingDisableGallery;
	}

	public boolean isSearchingDisableVideos()
	{
		return searchingDisableVideos;
	}

	public void setSearchingDisableVideos(boolean searchingDisableVideos)
	{
		this.searchingDisableVideos = searchingDisableVideos;
	}

	public boolean isFileCountDisabled()
	{
		return fileCountDisabled;
	}

	public void setFileCountDisabled(boolean fileCountDisabled)
	{
		this.fileCountDisabled = fileCountDisabled;
	}
}
