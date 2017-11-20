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

package com.tle.web.search.filter;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;

@NonNullByDefault
@SuppressWarnings("nls")
public class FilterByOwnerSection extends AbstractFilterByUserSection<FreetextSearchEvent>
{
	private boolean showOrphaned = false;

	@PlugKey("filter.byowner.showorphaned")
	@Component(name = "orph", parameter = "orphaned", supported = true)
	private Checkbox orphaned;

	public Checkbox getOrphaned()
	{
		return orphaned;
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		if( showOrphaned )
		{
			orphaned.setClickHandler(new OverrideHandler(searchResults.getResultsUpdater(tree,
				events.getEventHandler("orphanedSearch"), getAjaxDiv())));
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( showOrphaned )
		{
			disableUserDialog(context);
		}
		return super.renderHtml(context);
	}

	@EventHandlerMethod
	public void orphanedSearch(SectionInfo info)
	{
		hidden.setValue(info, null);
		disableUserDialog(info);
	}

	@Override
	public void reset(SectionInfo info)
	{
		if( showOrphaned )
		{
			orphaned.setChecked(info, false);
		}
		super.reset(info);
	}

	private void disableUserDialog(SectionInfo info)
	{
		boolean doOrphaned = orphaned.isChecked(info);
		if( doOrphaned )
		{
			selOwner.getOpener().setDisabled(info, doOrphaned);
		}
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		event.filterByOwner(orphaned.isChecked(info) ? "" : getSelectedUserId(info));
	}

	@Override
	protected String getPublicParam()
	{
		return "owner";
	}

	@Override
	public String getAjaxDiv()
	{
		return "owner";
	}

	@Override
	public boolean isShowOrphaned()
	{
		return showOrphaned;
	}

	public void setShowOrphaned(boolean showOrphaned)
	{
		this.showOrphaned = showOrphaned;
	}
}
