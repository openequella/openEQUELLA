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

package com.tle.web.selection.home.recentsegments;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.ItemId;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.selection.SelectionHistory;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.home.model.RecentSelectionSegmentModel.RecentSelection;
import com.tle.web.viewurl.ViewItemUrlFactory;

public class SelectionsSegment extends AbstractRecentSegment
{
	@PlugKey("recently.selected")
	private static Label TITLE;

	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private SelectionService selectionService;

	@Override
	protected List<RecentSelection> getSelections(SectionInfo info, SelectionSession session, int maximum)
	{
		List<SelectionHistory> list = selectionService.getRecentSelections(info, maximum);
		List<RecentSelection> selections = new ArrayList<RecentSelection>();
		for( SelectionHistory resource : list )
		{
			HtmlLinkState state = new HtmlLinkState(urlFactory.createItemUrl(info, new ItemId(resource.getUuid(),
				resource.getVersion())));
			selections.add(new RecentSelection(resource, state));
		}
		return selections;
	}

	@Override
	public String getTitle(SectionInfo info, SelectionSession session)
	{
		return TITLE.getText(); //$NON-NLS-1$
	}

}
