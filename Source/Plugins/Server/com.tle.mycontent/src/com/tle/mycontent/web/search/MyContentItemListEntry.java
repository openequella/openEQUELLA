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

package com.tle.mycontent.web.search;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.mycontent.ContentHandler;
import com.tle.mycontent.MyContentConstants;
import com.tle.mycontent.service.MyContentService;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class MyContentItemListEntry extends StandardItemListEntry
{
	static
	{
		PluginResourceHandler.init(MyContentItemListEntry.class);
	}

	@PlugKey("list.tags")
	private static Label LABEL_TAGS;
	@PlugKey("selectscrap")
	private static Label LABEL_SELECT;

	@Inject
	private ItemService itemService;
	@Inject
	private MyContentService myContentService;

	private ContentHandler handler;

	private HtmlLinkState titleLink;

	@SuppressWarnings("nls")
	@Override
	public boolean isFlagSet(String flagKey)
	{
		if( flagKey.equals("com.tle.web.favourites.DontShow") || flagKey.equals("com.tle.web.viewitem.DontShowRating")
			|| flagKey.equals("com.tle.web.hierarchy.DontShow")
			|| flagKey.equals("com.tle.web.itemlist.standard.DontShowAttachments") )
		{
			return true;
		}
		return super.isFlagSet(flagKey);
	}

	@Override
	public HtmlLinkState getTitle()
	{
		if( titleLink != null )
		{
			return titleLink;
		}
		return super.getTitle();
	}

	@Override
	public void init(RenderContext context, ListSettings<?> settings)
	{
		super.init(context, settings);
		PropBagEx itemxml = itemService.getItemXmlPropBag(getItem());
		String tags = itemxml.getNode(MyContentConstants.KEYWORDS_NODE);
		if( !Check.isEmpty(tags) )
		{
			addDelimitedMetadata(LABEL_TAGS, tags);
		}
		String handlerId = itemxml.getNode(MyContentConstants.CONTENT_TYPE_NODE);
		handler = myContentService.getHandlerForId(handlerId);
		titleLink = handler.decorate(info, this);
	}

	@Override
	public Label getSelectLabel()
	{
		return LABEL_SELECT;
	}
}
