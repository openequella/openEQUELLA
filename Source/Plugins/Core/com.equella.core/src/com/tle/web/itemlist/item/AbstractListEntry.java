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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.tle.web.itemlist.DelimitedMetadata;
import com.tle.web.itemlist.ListEntry;
import com.tle.web.itemlist.MetadataEntry;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.InnerFade;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.OrderedRenderer;
import com.tle.web.sections.render.OrderedRenderer.RendererOrder;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;

public abstract class AbstractListEntry implements ListEntry
{
	private static final RendererOrder SORT = new OrderedRenderer.RendererOrder();

	@PlugKey("itemlist.thumb.alt")
	private static String THUMB_ALT_KEY;

	@Inject
	protected RendererFactory rendererFactory;
	@Inject
	private DateRendererFactory dateRendererFactory;

	private boolean hilighted;
	private boolean selected;
	private Map<Object, Object> attrs;
	private final TagState tag = new TagState();

	private SectionRenderable thumbnail;
	private final List<SectionRenderable> thumbnails = new ArrayList<>();
	private final List<OrderedRenderer> ratingBarLeft = new ArrayList<OrderedRenderer>();
	private final List<OrderedRenderer> ratingBarRight = new ArrayList<OrderedRenderer>();
	private DivRenderer thumbnailCount = null;

	protected final List<MetadataEntry> entries = new ArrayList<MetadataEntry>();
	protected ListSettings<?> listSettings;
	protected SectionInfo info;

	@Override
	public void addThumbnail(SectionRenderable image)
	{
		thumbnails.add(image);
	}

	@Override
	public HtmlBooleanState getCheckbox()
	{
		return new HtmlBooleanState();
	}

	@Override
	public List<MetadataEntry> getMetadata()
	{
		return entries;
	}

	protected SectionRenderable getTimeRenderer(Date date)
	{
		return dateRendererFactory.createDateRenderer(date);
	}

	protected void setupMetadata(RenderContext context)
	{
		// nothing
	}

	@Override
	public void addMetadata(MetadataEntry entry)
	{
		entries.add(entry);
	}

	@Override
	public void addDelimitedMetadata(Label label, Object... data)
	{
		entries.add(new DelimitedMetadata(label, Arrays.asList(rendererFactory.convertToRenderers(data))));
	}

	@Override
	public void addDelimitedMetadata(Label label, Collection<?> data)
	{
		entries.add(new DelimitedMetadata(label, rendererFactory.convertToRenderers(data)));
	}

	@Override
	public void setInfo(SectionInfo info)
	{
		this.info = info;
	}

	@Override
	public boolean isHilighted()
	{
		return hilighted;
	}

	@Override
	public void setHilighted(boolean hilighted)
	{
		this.hilighted = hilighted;
	}

	@Override
	public void init(RenderContext context, ListSettings<? extends ListEntry> settings)
	{
		this.listSettings = settings;
		setupMetadata(context);
	}

	@Override
	public void setAttribute(Object key, Object data)
	{
		ensureAttrs();
		attrs.put(key, data);
	}

	@SuppressWarnings("unchecked")
	public <V> V getAttribute(Object key)
	{
		if( attrs == null )
		{
			return null;
		}
		return (V) attrs.get(key);
	}

	@Override
	public boolean isFlagSet(String flagKey)
	{
		if( attrs == null )
		{
			return false;
		}
		return attrs.containsKey(flagKey);
	}

	public void setFlag(String flagKey, boolean b)
	{
		ensureAttrs();
		if( !b )
		{
			attrs.remove(flagKey);
		}
		else
		{
			attrs.put(flagKey, true);
		}
	}

	private void ensureAttrs()
	{
		if( attrs == null )
		{
			attrs = new HashMap<Object, Object>();
		}
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public SectionRenderable getThumbnail()
	{
		if( thumbnail == null && !thumbnails.isEmpty() )
		{
			final CombinedRenderer cr = new CombinedRenderer();

			for( SectionRenderable thumb : thumbnails )
			{
				LinkRenderer thumbLink = new LinkRenderer(getTitle());
				if( thumb instanceof ImageRenderer && ((ImageRenderer) thumb).getAlt() == null )
				{
					((ImageRenderer) thumb).setAlt(new KeyLabel(THUMB_ALT_KEY, getTitle().getLabel()));
				}
				thumbLink.setNestedRenderable(thumb);
				cr.addRenderer(thumbLink);
			}

			TagState thumbsTag = new TagState();
			InnerFade.fade(thumbsTag);
			thumbnail = new DivRenderer(thumbsTag, cr);
		}
		return thumbnail;
	}

	public SectionRenderable getIcon()
	{
		return null;
	}

	public void setThumbnail(SectionRenderable thumbnail)
	{
		this.thumbnail = thumbnail;
	}

	public TagState getTag()
	{
		return tag;
	}

	@Override
	public void addRatingMetadata(Object... ratingData)
	{
		addRatingMetadataWithOrder(0, ratingData);
	}

	@Override
	public void addRatingMetadataWithOrder(int order, Object... ratingData)
	{
		ratingBarLeft.add(new OrderedRenderer(order, rendererFactory.convertToRenderer(ratingData)));
	}

	@Override
	public void addRatingAction(Object... ratingData)
	{
		addRatingAction(0, ratingData);
	}

	@Override
	public void addRatingAction(int order, Object... ratingData)
	{
		ratingBarRight.add(new OrderedRenderer(order, rendererFactory.convertToRenderer(ratingData)));
	}

	@Override
	public void setThumbnailCount(DivRenderer count)
	{
		this.thumbnailCount = count;
	}

	public List<? extends SectionRenderable> getRatingBarLeft()
	{
		Collections.sort(ratingBarLeft, SORT);
		return ratingBarLeft;
	}

	public List<? extends SectionRenderable> getRatingBarRight()
	{
		Collections.sort(ratingBarRight, SORT);
		return ratingBarRight;
	}

	public DivRenderer getThumbnailCount()
	{
		return thumbnailCount;
	}
}