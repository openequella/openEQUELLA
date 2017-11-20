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

package com.tle.web.workflow.tasks;

import java.util.Arrays;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;

public class FilterByAssignment extends AbstractPrototypeSection<Object>
	implements
		HtmlRenderer,
		ResetFiltersListener,
		SearchEventListener<FreetextSearchEvent>
{
	public enum Assign
	{
		ANY, ME, OTHERS, NOONE
	}

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component(parameter = "unan", supported = true)
	@PlugKey("unanimous.must")
	private Checkbox mustCheckbox;

	@Component(parameter = "asgn", supported = true)
	private SingleSelectionList<Assign> assignList;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> resultsSection;

	@PlugKey("assign.")
	private static String KEY_ASSIGN;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		assignList.setListModel(new EnumListModel<Assign>(KEY_ASSIGN, Assign.values()));
		assignList.addChangeEventHandler(resultsSection.getRestartSearchHandler(tree));
		mustCheckbox.setClickHandler(new StatementHandler(resultsSection.getRestartSearchHandler(tree)));
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("assignfilter.ftl", this);
	}

	@SuppressWarnings("nls")
	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		if( mustCheckbox.isChecked(info) )
		{
			event.filterByTerm(false, FreeTextQuery.FIELD_WORKFLOW_UNANIMOUS, "true"); //$NON-NLS-1$
		}
		Assign assign = assignList.getSelectedValue(info);
		if( assign != null )
		{
			String currentUserId = CurrentUser.getUserID();
			switch( assign )
			{
				case OTHERS:
					event
						.filterByTerms(true, FreeTextQuery.FIELD_WORKFLOW_ASSIGNEDTO, Arrays.asList(currentUserId, ""));
					break;
				case ME:
					event.filterByTerm(false, FreeTextQuery.FIELD_WORKFLOW_ASSIGNEDTO, currentUserId);
					break;
				case NOONE:
					event.filterByTerm(false, FreeTextQuery.FIELD_WORKFLOW_ASSIGNEDTO, "");
					break;

				default:
					break;
			}
		}
	}

	public SingleSelectionList<Assign> getAssignList()
	{
		return assignList;
	}

	public Checkbox getMustCheckbox()
	{
		return mustCheckbox;
	}

	@Override
	public void reset(SectionInfo info)
	{
		assignList.setSelectedStringValue(info, null);
		mustCheckbox.setChecked(info, false);
	}
}
