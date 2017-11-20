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

package com.tle.web.itemlist;

import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;

public abstract class AbstractListSection<LE extends ListEntry, M extends AbstractListSection.Model<LE>>
	extends
		AbstractPrototypeSection<M> implements ListEntriesSection<LE>
{
	/**
	 * This should initialise the entries enough for an RSS feed to work, and
	 * should pre-load the bundle cache full of anything required for rendering.
	 * 
	 * @param context
	 * @param entries
	 */
	protected abstract List<LE> initEntries(RenderContext context);

	protected abstract SectionRenderable getRenderable(RenderEventContext context);

	protected void customiseListEntries(RenderContext context, List<LE> entries)
	{
		// nothing by default
	}

	@Override
	public void addListItem(SectionInfo info, LE item)
	{
		getModel(info).getItems().add(item);
	}

	public TagState getTag(SectionInfo info)
	{
		Model<LE> model = getModel(info);
		TagState tagState = model.getTag();
		if( tagState == null )
		{
			tagState = new TagState();
			tagState.addClass("itemlist"); //$NON-NLS-1$
			model.setTag(tagState);
		}
		return tagState;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		M model = getModel(context);
		List<LE> entries = model.getItems();
		initEntries(context);
		customiseListEntries(context, entries);
		TagRenderer tagRenderer = new TagRenderer("div", getTag(context)); //$NON-NLS-1$
		tagRenderer.setNestedRenderable(getRenderable(context));
		return tagRenderer;
	}

	public void setNullItemsRemovedOnModel(SectionInfo info, boolean nullItemsRemoved)
	{
		getModel(info).setNullItemsRemoved(nullItemsRemoved);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model<LE>();
	}

	public static class Model<LE>
	{
		private TagState tag;
		private List<LE> items = new ArrayList<>();
		private boolean nullItemsRemoved;

		public List<LE> getItems()
		{
			return items;
		}

		public TagState getTag()
		{
			return tag;
		}

		public void setTag(TagState tag)
		{
			this.tag = tag;
		}

		public boolean isNullItemsRemoved()
		{
			return nullItemsRemoved;
		}

		public void setNullItemsRemoved(boolean nullItemsRemoved)
		{
			this.nullItemsRemoved = nullItemsRemoved;
		}
	}
}
