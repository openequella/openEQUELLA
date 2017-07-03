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

package com.tle.web.itemlist.item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.core.i18n.BundleCache;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.itemlist.ListEntry;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.HighlightableBundleLabel;
import com.tle.web.sections.result.util.ItemNameLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractItemlikeListEntry<I extends IItem<?>> extends AbstractListEntry
	implements
		ItemlikeListEntry<I>
{
	@PlugKey("unselectresult")
	private static Label LABEL_UNSELECT;
	@PlugKey("selectresult")
	private static Label LABEL_SELECT;
	@PlugKey("itemlist.label.itemnotfound")
	private static Label LABEL_ITEM_NOT_FOUND;

	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	protected BundleCache bundleCache;

	/* @LazyNonNull */@Nullable
	private List<ViewableResource> viewableResources;
	/* @LazyNonNull */@Nullable
	private UnmodifiableAttachments attachments;
	/* @LazyNonNull */@Nullable
	private ViewableItem<I> viewableItem;
	private I item;
	private final List<SectionRenderable> extras = new ArrayList<>();
	private SectionRenderable toggle;
	private FreetextResult freetextData;
	private boolean selectable;

	protected abstract ViewableItem<I> createViewableItem();

	protected abstract UnmodifiableAttachments loadAttachments();

	protected abstract Bookmark getTitleLink();

	@Override
	public HtmlLinkState getTitle()
	{
		HtmlLinkState link = new HtmlLinkState(getTitleLabel(), getTitleLink());
		I item = getItem();
		if( item != null )
		{
			link.setTitle(new ItemNameLabel(item, bundleCache));
			link.setData("itemuuid", item.getUuid());
			link.setData("itemversion", Integer.toString(item.getVersion()));
			link.setData("extensiontype", getViewableItem().getItemExtensionType());
		}
		else
		{
			link.setDisabled(true);
			link.setTitle(LABEL_ITEM_NOT_FOUND);
		}

		return link;
	}

	public Label getTitleLabel()
	{
		I item = getItem();
		if( item != null )
		{
			return new HighlightableBundleLabel(item.getName(), item.getUuid(), bundleCache,
				listSettings.getHilightedWords(), false);
		}
		return LABEL_ITEM_NOT_FOUND;
	}

	@Override
	public Label getDescription()
	{
		I item = getItem();
		if( item != null )
		{
			return new HighlightableBundleLabel(item.getDescription(), "", bundleCache,
				listSettings.getHilightedWords(), true);
		}
		return new TextLabel("");
	}

	@Override
	public UnmodifiableAttachments getAttachments()
	{
		if( attachments == null )
		{
			attachments = loadAttachments();
		}
		return attachments;
	}

	/**
	 * 
	 * @param item This can be null in the case that the local item no longer exists
	 */
	public void setItem(@Nullable I item)
	{
		this.item = item;
	}

	public ViewableItem<I> getViewableItem()
	{
		if( viewableItem == null )
		{
			viewableItem = createViewableItem();
		}
		return viewableItem;
	}

	public SectionRenderable getLastModified()
	{
		I item = getItem();
		if( item != null )
		{
			Date dateModified = item.getDateModified();
			if( dateModified != null )
			{
				return getTimeRenderer(dateModified);
			}
		}
		return null;
	}

	@Override
	public List<ViewableResource> getViewableResources()
	{
		if( viewableResources == null )
		{
			final ViewableItem<I> vitem = getViewableItem();
			viewableResources = Lists.transform(getAttachments().getList(),
				new Function<IAttachment, ViewableResource>()
				{
					@Override
					public ViewableResource apply(@Nullable IAttachment attachment)
					{
						return attachmentResourceService.getViewableResource(info, vitem, attachment);
					}
				});
		}
		return viewableResources;
	}

	public static <I extends IItem<?>> List<I> getItems(List<? extends ItemlikeListEntry<I>> entries)
	{
		List<I> items = new ArrayList<>();
		for( ItemlikeListEntry<I> entry : entries )
		{
			items.add(entry.getItem());
		}
		return items;
	}

	@Nullable
	public FreetextResult getFreetextData()
	{
		return freetextData;
	}

	public void setFreetextData(FreetextResult freetextData)
	{
		this.freetextData = freetextData;
	}

	@Nullable
	@Override
	public I getItem()
	{
		return item;
	}

	@Override
	public void init(RenderContext context, ListSettings<? extends ListEntry> settings)
	{
		super.init(context, settings);
		if( item != null )
		{
			bundleCache.addBundle(item.getName());
			bundleCache.addBundle(item.getDescription());
		}
	}

	public List<SectionRenderable> getExtras()
	{
		return extras;
	}

	@Override
	public void addExtras(SectionRenderable extra)
	{
		this.extras.add(extra);
	}

	public SectionRenderable getToggle()
	{
		return toggle;
	}

	@Override
	public void setToggle(SectionRenderable toggle)
	{
		this.toggle = toggle;
	}

	@Override
	public Label getSelectLabel()
	{
		return LABEL_SELECT;
	}

	@Override
	public Label getUnselectLabel()
	{
		return LABEL_UNSELECT;
	}

	@Override
	public boolean isSelectable()
	{
		return selectable;
	}

	@Override
	public void setSelectable(boolean selectable)
	{
		this.selectable = selectable;
	}
}
