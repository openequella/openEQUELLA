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

package com.tle.web.workflow.tasks.portal;

import java.util.Arrays;

import javax.inject.Inject;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.inject.assistedinject.Assisted;
import com.tle.common.search.DefaultSearch;
import com.tle.core.guice.BindFactory;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.workflow.freetext.TaskListSearch;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.workflow.portal.TaskListSubsearch;
import com.tle.web.workflow.tasks.FilterByAssignment;
import com.tle.web.workflow.tasks.FilterByAssignment.Assign;
import com.tle.web.workflow.tasks.RootTaskListSection;

public class TaskListFilter implements TaskListSubsearch
{
	private final String identifier;
	private final Assign assigned;
	private final boolean mustModerate;
	private final boolean secondLevel;
	private final SectionsController sectionsController;

	@PlugKey("filternames.")
	private static String KEY_FILTERNAMES;

	@Inject
	public TaskListFilter(SectionsController controller, @Assisted String identifier, @Assisted Assign assigned,
		@Assisted("mustModerate") boolean mustModerate, @Assisted("secondLevel") boolean secondLevel)
	{
		this.identifier = identifier;
		this.assigned = assigned;
		this.mustModerate = mustModerate;
		this.secondLevel = secondLevel;
		this.sectionsController = controller;
	}

	@Override
	public String getIdentifier()
	{
		return identifier;
	}

	@SuppressWarnings("nls")
	@Override
	public DefaultSearch getSearch()
	{
		TaskListSearch searcher = new TaskListSearch();

		if( mustModerate )
		{
			searcher.addMust(FreeTextQuery.FIELD_WORKFLOW_UNANIMOUS, "true"); //$NON-NLS-1$
		}
		String currentUserId = CurrentUser.getUserID();
		switch( assigned )
		{
			case OTHERS:
				searcher.addMustNot(FreeTextQuery.FIELD_WORKFLOW_ASSIGNEDTO, Arrays.asList(currentUserId, ""));
				break;
			case ME:
				searcher.addMust(FreeTextQuery.FIELD_WORKFLOW_ASSIGNEDTO, currentUserId);
				break;
			case NOONE:
				searcher.addMust(FreeTextQuery.FIELD_WORKFLOW_ASSIGNEDTO, "");
				break;

			default:
				// ANY
				break;

		}
		return searcher;
	}

	@Override
	public boolean isSecondLevel()
	{
		return secondLevel;
	}

	public void setupFilter(SectionInfo info)
	{
		// noop
	}

	@Override
	public SectionInfo setupForward(SectionInfo from)
	{
		SectionInfo forward;
		if( from != null )
		{
			forward = from.createForward(RootTaskListSection.URL);
		}
		else
		{
			forward = sectionsController.createForward(RootTaskListSection.URL);
		}
		FilterByAssignment filterSection = forward.lookupSection(FilterByAssignment.class);
		filterSection.getAssignList().setSelectedStringValue(forward, assigned.name());
		filterSection.getMustCheckbox().setChecked(forward, mustModerate);
		return forward;
	}

	@Override
	public Label getName()
	{
		return new KeyLabel(KEY_FILTERNAMES + getIdentifier());
	}

	@Override
	public String getParentIdentifier()
	{
		return "taskall"; //$NON-NLS-1$
	}

	@BindFactory
	public interface TaskListFilterFactory
	{
		TaskListFilter create(String identifier, Assign assigned, @Assisted("mustModerate") boolean mustModerate,
			@Assisted("secondLevel") boolean secondLevel);
	}

}
