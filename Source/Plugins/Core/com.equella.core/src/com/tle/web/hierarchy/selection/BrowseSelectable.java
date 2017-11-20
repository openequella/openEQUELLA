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

package com.tle.web.hierarchy.selection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.AbstractSelectionNavAction;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class BrowseSelectable extends AbstractSelectionNavAction
{
	static
	{
		PluginResourceHandler.init(BrowseSelectable.class);
	}
	@PlugKey("label.selection.action")
	private static Label LABEL_BROWSE;

	@Inject
	private HierarchyService hierarchyService;

	@Override
	public SectionInfo createSectionInfo(SectionInfo info, SelectionSession session)
	{
		return createForwardForNavAction(info, session);
	}

	@Override
	public Label getLabelForNavAction(SectionInfo info)
	{
		return LABEL_BROWSE;
	}

	@Override
	public SectionInfo createForwardForNavAction(SectionInfo fromInfo, SelectionSession session)
	{
		return fromInfo.createForward("/hierarchy.do");
	}

	@Override
	public boolean isActionAvailable(SectionInfo info, SelectionSession session)
	{
		if( !super.isActionAvailable(info, session) )
		{
			return false;
		}
		if( session.getLayout() != Layout.NORMAL )
		{
			return false;
		}
		// This seems really expensive...
		return (session.isAllCollections() || !Check.isEmpty(session.getCollectionUuids()))
			&& !hierarchyService.expandVirtualisedTopics(hierarchyService.getChildTopics(null), null, null).isEmpty();
	}

	@Override
	public String getActionType()
	{
		return "browse";
	}

	@Override
	public boolean isShowBreadcrumbs()
	{
		return true;
	}
}
