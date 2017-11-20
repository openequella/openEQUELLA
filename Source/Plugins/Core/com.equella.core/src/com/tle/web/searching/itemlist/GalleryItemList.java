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
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.SectionRenderable;

@Bind
@SuppressWarnings("nls")
public class GalleryItemList extends StandardItemList
{
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@PlugURL("scripts/gallerypreview.js")
	private static String SCRIPT_URL;

	private final IncludeFile previewHandler = new IncludeFile(SCRIPT_URL);

	public static final String GALLERY_FLAG = "gallery.result";

	@Override
	protected SectionRenderable getRenderable(RenderEventContext context)
	{
		JSCallAndReference setupPreviews = new ExternallyDefinedFunction("setupPreviews", previewHandler);
		getTag(context).addReadyStatements(setupPreviews);
		return viewFactory.createResult("gallerylist.ftl", this);
	}

	@SuppressWarnings("nls")
	@Override
	protected void customiseListEntries(RenderContext context, List<StandardItemListEntry> entries)
	{
		getListSettings(context).setAttribute(GALLERY_FLAG, true);
		super.customiseListEntries(context, entries);
	}

	@Override
	protected Set<String> getExtensionTypes()
	{
		return Collections.singleton("gallery");
	}

}
