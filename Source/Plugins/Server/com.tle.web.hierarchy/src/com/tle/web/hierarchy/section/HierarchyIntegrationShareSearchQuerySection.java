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

package com.tle.web.hierarchy.section;

import java.util.Map;

import com.tle.web.search.actions.StandardShareSearchQuerySection;
import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
public class HierarchyIntegrationShareSearchQuerySection extends StandardShareSearchQuerySection
{
	private static final String HIERARCHYURL;

	/**
	 * When creating a institutionalised URL, most essential that the
	 * supplementary path url NOT begin with a '/', as the new URL method with
	 * take it as an absolute value and thereby erase the institutional name in
	 * the final URL. Thus we'll follow the URL set by the RootHierarchySection,
	 * but use a modified copy that avoid the URL combo pitfall.
	 */
	static
	{
		HIERARCHYURL = RootHierarchySection.HIERARCHYURL.startsWith("/")
			? RootHierarchySection.HIERARCHYURL.substring(1) : RootHierarchySection.HIERARCHYURL;
	}

	@AjaxFactory
	private AjaxGenerator ajax;

	@Override
	protected void doSentMessage(SectionInfo info, Label message)
	{
		getModel(info).setEmailMessage(message);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		getSendEmailButton().setClickHandler(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("sendEmail"), "show-email"));
	}

	@Override
	public void setupUrl(InfoBookmark bookmark, RenderContext context)
	{
		url.setValue(context, new BookmarkAndModify(bookmark, new BookmarkModifier()
		{
			@Override
			public void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState)
			{
				bookmarkState.put(SectionInfo.KEY_PATH,
					new String[]{institutionService.institutionalise(HIERARCHYURL)});
			}
		}).getHref());
		url.getState(context).setEditable(false);
	}
}
