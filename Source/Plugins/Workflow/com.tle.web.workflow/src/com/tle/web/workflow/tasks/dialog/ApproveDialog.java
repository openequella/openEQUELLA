package com.tle.web.workflow.tasks.dialog;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.workflow.tasks.comments.CommentsSection;

@SuppressWarnings("nls")
@NonNullByDefault
public class ApproveDialog extends AbstractTaskActionDialog<AbstractTaskActionDialog.AbstractTaskActionDialogModel>
{
	@PlugKey("command.taskaction.approve")
	private static Label LABEL_APPROVE_BUTTON;
	@PlugKey("command.approve.title")
	private static Label LABEL_APPROVING_TITLE;
	@PlugKey("comments.acceptmsg")
	private static Label LABEL_ACCEPTMSG;
	@PlugKey("comments.entermsg.withfiles")
	private static Label LABEL_ENTERMSG_WITHFILES;

	@Override
	public String getDefaultPropertyName()
	{
		return "approveDialog";
	}

	@Override
	protected ButtonRenderer.ButtonType getButtonType()
	{
		return ButtonRenderer.ButtonType.ACCEPT;
	}

	@Override
	protected Label getButtonLabel()
	{
		return LABEL_APPROVE_BUTTON;
	}

	@Override
	protected CommentsSection.CommentType getActionType()
	{
		return CommentsSection.CommentType.ACCEPT;
	}

	@Nullable
	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_APPROVING_TITLE;
	}

	@Override
	public Label getPostCommentHeading()
	{
		return LABEL_ACCEPTMSG;
	}
}
