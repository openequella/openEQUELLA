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

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
public abstract class StandardListSection<LE extends ListEntry, M extends StandardListSection.Model<LE>>
	extends
		AbstractListSection<LE, M>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@SuppressWarnings("nls")
	@Override
	protected SectionRenderable getRenderable(RenderEventContext context)
	{
		return viewFactory.createResult("list/standardlist.ftl", this);
	}

	@Override
	public List<LE> initEntries(RenderContext context)
	{
		final M model = getModel(context);
		final List<LE> entries = model.getItems();
		final ListSettings<LE> settings = model.getListSettings();
		settings.setEntries(entries);
		for( LE t : entries )
		{
			t.init(context, settings);
		}
		return entries;
	}

	public ListSettings<LE> getListSettings(SectionInfo info)
	{
		return getModel(info).getListSettings();
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model<LE>();
	}

	public static class Model<LE extends ListEntry> extends AbstractListSection.Model<LE>
	{
		private ListSettings<LE> listSettings = new ListSettings<>();

		public ListSettings<LE> getListSettings()
		{
			return listSettings;
		}

		public void setListSettings(ListSettings<LE> listSettings)
		{
			this.listSettings = listSettings;
		}
	}
}
