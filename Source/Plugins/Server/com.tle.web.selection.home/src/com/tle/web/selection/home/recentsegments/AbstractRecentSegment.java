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

import java.util.List;

import javax.inject.Inject;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.home.RecentSelectionsSegment;
import com.tle.web.selection.home.model.RecentSegmentModel;
import com.tle.web.selection.home.model.RecentSelectionSegmentModel.RecentSelection;

@SuppressWarnings("nls")
public abstract class AbstractRecentSegment extends AbstractPrototypeSection<RecentSegmentModel>
	implements
		HtmlRenderer,
		RecentSelectionsSegment
{
	protected static final int MAX_RESULTS = 5;

	@Inject
	private SelectionService selectionService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		SelectionSession session = selectionService.getCurrentSession(context);
		getModel(context).setRecent(getSelections(context, session, MAX_RESULTS));
		return viewFactory.createResult("recentsegment.ftl", this);
	}

	protected abstract List<RecentSelection> getSelections(SectionInfo info, SelectionSession session, int maximum);

	@Override
	public Class<RecentSegmentModel> getModelClass()
	{
		return RecentSegmentModel.class;
	}
}