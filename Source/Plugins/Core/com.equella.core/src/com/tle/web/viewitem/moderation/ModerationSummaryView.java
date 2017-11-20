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

package com.tle.web.viewitem.moderation;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.ItemTaskId;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.NewDefaultViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewable.servlet.ItemServlet;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.workflow.tasks.ModerationService;
import com.tle.web.workflow.tasks.ModerationView;
import com.tle.web.workflow.view.CurrentModerationLinkSection;

@Bind
@Singleton
public class ModerationSummaryView implements ModerationView
{

	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private ViewableItemFactory viewableItemFactory;

	@Override
	public SectionInfo getViewForward(SectionInfo info, ItemTaskId itemTaskId, String view)
	{
		NewDefaultViewableItem viewable = viewableItemFactory.createNewViewableItem(itemTaskId);
		ViewItemUrl vurl = urlFactory.createItemUrl(info, viewable);
		vurl.getQueryString();
		SectionInfo sinfo = vurl.getSectionInfo();
		if( view.equals(ModerationService.VIEW_PROGRESS) )
		{
			sinfo.lookupSection(CurrentModerationLinkSection.class).execute(sinfo);
		}
		sinfo.setAttribute(ItemServlet.VIEWABLE_ITEM, viewable);
		return sinfo;
	}
}
