package com.tle.web.workflow.tasks.dialog;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.workflow.WorkflowStep;
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
import com.tle.web.workflow.tasks.comments.CommentsSection;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("nls")
@NonNullByDefault
public class RejectDialog extends AbstractTaskActionDialog<AbstractTaskActionDialog.AbstractTaskActionDialogModel>
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
	protected JSExpression getWorkflowStepExpression()
	{
		return rejectSteps.createGetExpression();
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "rejectDialog";
	}

	@Override
	protected ButtonRenderer.ButtonType getButtonType()
	{
		return ButtonRenderer.ButtonType.REJECT;
	}

	@Override
	protected Label getButtonLabel()
	{
		return LABEL_REJECT_BUTTON;
	}

	@Override
	protected CommentsSection.CommentType getActionType()
	{
		return CommentsSection.CommentType.REJECT;
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
}
