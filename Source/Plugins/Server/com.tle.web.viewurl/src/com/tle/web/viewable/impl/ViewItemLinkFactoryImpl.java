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

package com.tle.web.viewable.impl;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemKey;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionUtils;
import com.tle.web.viewable.ViewItemConstants;
import com.tle.web.viewable.ViewItemLinkFactory;

@NonNullByDefault
@Bind(ViewItemLinkFactory.class)
@Singleton
public class ViewItemLinkFactoryImpl implements ViewItemLinkFactory
{
	@Inject
	private InstitutionService institutionService;

	@Override
	public Bookmark createViewLink(ItemKey itemId)
	{
		return new ItemBookmark(itemId, null);
	}

	@Override
	public Bookmark createViewAttachmentLink(ItemKey itemId, @Nullable String attachmentUuid)
	{
		return new ItemBookmark(itemId, attachmentUuid);
	}

	@Override
	public Bookmark createThumbnailAttachmentLink(ItemKey itemId, @Nullable String attachmentUuid)
	{
		return new ThumbBookmark(itemId, attachmentUuid);
	}

	public class ItemBookmark implements Bookmark
	{
		protected final ItemKey itemKey;
		@Nullable
		protected final String attachmentUuid;

		public ItemBookmark(ItemKey itemKey, @Nullable String attachmentUuid)
		{
			this.itemKey = itemKey;
			this.attachmentUuid = attachmentUuid;
		}

		@SuppressWarnings("nls")
		@Override
		public String getHref()
		{
			Map<String, Object> params = Maps.newHashMap();
			if( attachmentUuid != null )
			{
				params.put(ViewItemConstants.PARAM_ATTACHMENTUUID, attachmentUuid);
			}
			String paramString = "";
			if( !params.isEmpty() )
			{
				paramString = '?' + SectionUtils.getParameterString(SectionUtils.getParameterNameValues(params, false));
			}
			return institutionService
				.institutionalise(ViewItemConstants.PATH_ITEMSERVLET + itemKey + '/' + paramString);
		}
	}

	public class ThumbBookmark extends ItemBookmark
	{
		public ThumbBookmark(ItemKey itemKey, @Nullable String attachmentUuid)
		{
			super(itemKey, attachmentUuid);
		}

		@Override
		public String getHref()
		{
			final StringBuilder url = new StringBuilder("thumbs/").append(itemKey).append('/');
			if( !Strings.isNullOrEmpty(attachmentUuid) )
			{
				url.append(attachmentUuid);
			}
			return institutionService.institutionalise(url.toString());
		}
	}
}
