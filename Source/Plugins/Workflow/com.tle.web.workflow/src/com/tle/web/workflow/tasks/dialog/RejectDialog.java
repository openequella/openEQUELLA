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

package com.tle.web.workflow.tasks.dialog;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.workflow.WorkflowStep;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.workflow.tasks.CurrentTaskSection;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class RejectDialog extends AbstractTaskActionDialog
{
	@Inject
	private BundleCache bundleCache;

	@Component
	private SingleSelectionList<WorkflowStep> rejectSteps;

	@PlugKey("command.taskaction.reject")
	private static Label LABEL_REJECT_BUTTON;
	@PlugKey("command.reject.title")
	private static Label LABEL_REJECTING_TITLE;
	@PlugKey("comments.rejectmsg")
	private static Label LABEL_REJECTMSG;
	@PlugKey("comments.entermsg")
	private static Label LABEL_ENTERMSG;
	@PlugKey("reject.original")
	private static Label LABEL_ORIGINAL;

	@Nullable
	@Override
	public String getWorkflowStepTarget(SectionInfo info)
	{
		return rejectSteps.getSelectedValueAsString(info);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "rejectDialog";
	}

	@Override
	public ButtonRenderer.ButtonType getButtonType()
	{
		return ButtonRenderer.ButtonType.REJECT;
	}

	@Override
	public Label getButtonLabel()
	{
		return LABEL_REJECT_BUTTON;
	}

	@Override
	public CurrentTaskSection.CommentType getActionType()
	{
		return CurrentTaskSection.CommentType.REJECT;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		rejectSteps.setListModel(new StepListModel());

		super.registered(id, tree);
	}

	@Nullable
	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_REJECTING_TITLE;
	}

	@Override
	public Label getPostCommentHeading()
	{
		return LABEL_REJECTMSG;
	}

	public SingleSelectionList<WorkflowStep> getRejectSteps()
	{
		return rejectSteps;
	}

	public class StepListModel extends DynamicHtmlListModel<WorkflowStep>
	{
		@Override
		protected Iterable<WorkflowStep> populateModel(SectionInfo info)
		{
			WorkflowStep step = info.lookupSection(CurrentTaskSection.class).getCurrentStep(info);
			List<WorkflowStep> steps = new ArrayList<WorkflowStep>(step.getRejectPoints());
			//if( !steps.isEmpty() )
			//{
				steps.add(null);
			//}
			return steps;
		}

		@Override
		protected Option<WorkflowStep> convertToOption(SectionInfo info, @Nullable WorkflowStep obj)
		{
			if( obj == null )
			{
				return new LabelOption<WorkflowStep>(LABEL_ORIGINAL, "", null);
			}
			return new NameValueOption<WorkflowStep>(
					new BundleNameValue(obj.getDisplayName(), obj.getUuid(), bundleCache), obj);
		}
	}

	@Override
	public Label validate(SectionInfo info)
	{
		return validateHasMessage(info);
	}

	@Override
	public boolean isMandatoryMessage()
	{
		return true;
	}

}
