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

import javax.inject.Inject;

import com.tle.core.security.TLEAclManager;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.SubmitValuesHandler;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.workflow.manage.TaskResultsDialog;
import com.tle.web.workflow.manage.TaskSelectionSection;

public class ModerateSelectedButton extends TaskSelectionSection
{
	@PlugKey("selectionsbox.moderate")
	private static Label LABEL_MODERATE;

	@Inject
	private TLEAclManager aclService;

	@Component
	private Button moderateSelectedButton;

	private SubmitValuesHandler moderateSelectedHandler;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		moderateSelectedButton.setLabel(LABEL_MODERATE);
		moderateSelectedButton.setStyleClass("moderate-action");
		moderateSelectedButton.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( moderateSelectedHandler != null )
		{
			moderateSelectedButton.setClickHandler(context, moderateSelectedHandler);
		}
		else
		{
			moderateSelectedButton.setClickHandler(context, new OverrideHandler(Js.alert_s(getPleaseSelectLabel())));
		}

		TaskResultsDialog dialog = (TaskResultsDialog) getBulkDialog();
		dialog.getModel(context).setPage("my-task");
		return super.renderHtml(context);
	}

	public void setupModerateSelectedHandler(SubmitValuesHandler handler)
	{
		moderateSelectedHandler = handler;
	}

	@Override
	protected boolean showExecuteButton(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(
			Arrays.asList(new String[]{"APPROVE_BULK_TASKS", "REJECT_BULK_TASKS", "MANAGE_WORKFLOW"}), true).isEmpty();
	}

	public Button getModerateSelectedButton()
	{
		return moderateSelectedButton;
	}
}
