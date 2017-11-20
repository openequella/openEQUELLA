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

import static com.tle.web.sections.js.generic.statement.FunctionCallStatement.jscall;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.Check;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.AbstractFilterByUserSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;

public class FilterByAssignee extends AbstractFilterByUserSection<FreetextSearchEvent>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> resultsSection;

	@AjaxFactory
	private AjaxGenerator ajax;

	@Component(parameter = "unan", supported = true)
	@PlugKey("unassigned.only")
	private Checkbox unassOnlyCheckbox;

	@PlugKey("filter.assign.title")
	private static Label LABEL_TITLE;

	@PlugKey("filter.assign.dialog")
	private static Label LABEL_DIALOG_TITLE;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		String assigneeUuid = hidden.getValue(context);
		if( assigneeUuid != null )
		{
			getModel(context).setOwner(userLinkSection.createLink(context, assigneeUuid));
		}
		return viewFactory.createResult("assigneefilter.ftl", context);
	}

	@SuppressWarnings("nls")
	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		if( unassOnlyCheckbox.isChecked(info) )
		{
			super.reset(info);
			if( getModel(info).getOwner() != null )
			{
				getModel(info).setOwner(null);
			}
			event.filterByTerm(false, FreeTextQuery.FIELD_WORKFLOW_ASSIGNEDTO, "");
		}
		else
		{
			String userIds = getSelectedUserId(info);
			if( !Check.isEmpty(userIds) )
			{
				event.filterByTerm(false, FreeTextQuery.FIELD_WORKFLOW_ASSIGNEDTO, userIds);
			}
		}
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		JSHandler restarter = new StatementHandler(resultsSection.getRestartSearchHandler(tree));
		unassOnlyCheckbox.setClickHandler(restarter);

		JSCallable unassignedClicked = ajax.getAjaxUpdateDomFunction(tree, null,
			events.getEventHandler("unassignedClicked"), ajax.getEffectFunction(EffectType.ACTIVITY), getAjaxDiv());
		unassOnlyCheckbox.addClickStatements(jscall(unassignedClicked));

		selOwner.setOkCallback(searchResults.getResultsUpdater(tree, events.getEventHandler("uncheckUnassigned"),
			getAjaxDiv()));
	}

	@EventHandlerMethod
	public void unassignedClicked(SectionInfo info)
	{
		if( unassOnlyCheckbox.isChecked(info) )
		{
			super.reset(info);
		}
	}

	/**
	 * Whether the user selector dialogbox has been cancelled, or closed without
	 * selection, we don't reset the unassigned user checkbox. But if there's
	 * any selection, we allow that selection to trigger a fresh set of result
	 * which renders the unassigned filter irrelevant, hence we need to ensure
	 * the checkbox is cleared.
	 * 
	 * @param info
	 */
	@EventHandlerMethod
	public void uncheckUnassigned(SectionInfo info, String user)
	{
		super.ownerSelected(info, user);
		boolean selectionMade = !Check.isEmpty(getSelectedUserId(info));
		if( selectionMade )
		{
			unassOnlyCheckbox.setChecked(info, false);
		}
	}

	public Checkbox getUnassOnlyCheckbox()
	{
		return unassOnlyCheckbox;
	}

	@Override
	protected String getPublicParam()
	{
		return "assign";
	}

	@Override
	public Label getTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	public Label getDialogTitle()
	{
		return LABEL_DIALOG_TITLE;
	}

	@Override
	public String getAjaxDiv()
	{
		return "assigneeflt";
	}
}
