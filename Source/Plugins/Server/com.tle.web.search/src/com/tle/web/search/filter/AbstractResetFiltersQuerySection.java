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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
public abstract class AbstractResetFiltersQuerySection<M, SE extends AbstractSearchEvent<SE>>
	extends
		AbstractQuerySection<M, SE> implements ResetFiltersParent
{
	@Inject
	private ResetFiltersSection<?> resetFiltersSection;

	protected final List<SectionId> queryActionSections = new ArrayList<SectionId>();

	@Override
	public ResetFiltersSection<?> getResetFiltersSection()
	{
		return resetFiltersSection;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(resetFiltersSection, id);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		queryActionSections.addAll(tree.getChildIds(id));
	}
	
	@Override
	public void addResetDiv(SectionTree tree, List<String> ajaxList)
	{
		String ajaxDiv = getAjaxDiv();
		if( ajaxDiv == null || !ajaxList.contains(ajaxDiv) )
		{
			resetFiltersSection.addAjaxDiv(ajaxList);
		}
	}

	protected void renderQueryActions(RenderEventContext context, AbstractQuerySectionModel model)
	{
		model.setQueryActions(SectionUtils.renderSectionIds(context, queryActionSections));
	}

	@Nullable
	protected String getAjaxDiv()
	{
		return null;
	}

	public static class AbstractQuerySectionModel
	{
		private List<SectionRenderable> queryActions;

		public List<SectionRenderable> getQueryActions()
		{
			return queryActions;
		}

		public void setQueryActions(List<SectionRenderable> queryActions)
		{
			this.queryActions = queryActions;
		}
	}
}
