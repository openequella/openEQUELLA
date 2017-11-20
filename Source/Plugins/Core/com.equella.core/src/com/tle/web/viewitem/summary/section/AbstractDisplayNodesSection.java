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

package com.tle.web.viewitem.summary.section;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.viewable.ViewableItem;

/*
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractDisplayNodesSection<I extends IItem<?>, M extends AbstractDisplayNodesSection.DisplayNodesModel>
	extends
		AbstractPrototypeSection<M> implements HtmlRenderer
{
	protected abstract ViewableItem<I> getViewableItem(SectionInfo info);

	@Nullable
	protected abstract List<Entry> getEntries(RenderEventContext context, ViewableItem<I> vitem);

	@ViewFactory
	private FreemarkerFactory view;

	@Nullable
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final ViewableItem<I> vitem = getViewableItem(context);
		final List<Entry> entries = getEntries(context, vitem);
		if( entries != null && !entries.isEmpty() )
		{
			DisplayNodesModel model = getModel(context);
			model.setEntries(entries);
			return view.createNamedResult("section_displaynodes", "viewitem/displaynodes.ftl", context);
		}

		return null;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "displaynodes";
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new DisplayNodesModel();
	}

	public static class Entry
	{
		private final Label title;
		private final SectionRenderable value;
		private final int truncateLength;
		private boolean fullspan;
		@Nullable
		private String style;

		public Entry(Label title, SectionRenderable value, int truncateLength)
		{
			this.title = title;
			this.value = value;
			this.truncateLength = truncateLength;
		}

		public SectionRenderable getValue()
		{
			return value;
		}

		public boolean isFullspan()
		{
			return fullspan;
		}

		public void setFullspan(boolean fullspan)
		{
			this.fullspan = fullspan;
		}

		public Label getTitle()
		{
			return title;
		}

		public int getTruncateLength()
		{
			return truncateLength;
		}

		public String getStyle()
		{
			return style;
		}

		public void setStyle(String style)
		{
			this.style = style;
		}
	}

	@NonNullByDefault(false)
	public static class DisplayNodesModel
	{
		private List<Entry> entries;

		public List<Entry> getEntries()
		{
			return entries;
		}

		public void setEntries(List<Entry> entries)
		{
			this.entries = entries;
		}
	}
}
