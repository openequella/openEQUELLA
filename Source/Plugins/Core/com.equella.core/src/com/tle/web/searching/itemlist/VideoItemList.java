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

package com.tle.web.searching.itemlist;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.item.StandardItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

@SuppressWarnings("nls")
@Bind
public class VideoItemList extends StandardItemList
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	public static final String VIDEO_FLAG = "video.result";

	@Override
	protected SectionRenderable getRenderable(RenderEventContext context)
	{
		return viewFactory.createResult("videolist.ftl", this);
	}

	@Override
	protected void customiseListEntries(RenderContext context, List<StandardItemListEntry> entries)
	{
		getListSettings(context).setAttribute(VIDEO_FLAG, true);
		super.customiseListEntries(context, entries);
	}

	@Override
	protected Set<String> getExtensionTypes()
	{
		return Collections.singleton("video");
	}
}
