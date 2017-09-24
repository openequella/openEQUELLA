package com.tle.web.workflow.tasks.dialog;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.workflow.tasks.CurrentTaskSection;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class CommentDialog extends AbstractTaskActionDialog
{
	@PlugKey("command.taskaction.comment")
	private static Label LABEL_COMMENT_BUTTON;
	@PlugKey("command.comment.title")
	private static Label LABEL_COMMENTING_TITLE;
	@PlugKey("comments.entermsg")
	private static Label LABEL_ENTERMSG;

	@Override
	public String getDefaultPropertyName()
	{
		return "commentDialog";
	}

	@Override
	public ButtonRenderer.ButtonType getButtonType()
	{
		return ButtonRenderer.ButtonType.SAVE;
	}

	@Override
	public Label getButtonLabel()
	{
		return LABEL_COMMENT_BUTTON;
	}

	@Override
	public CurrentTaskSection.CommentType getActionType()
	{
		return CurrentTaskSection.CommentType.COMMENT;
	}

	@Nullable
	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_COMMENTING_TITLE;
	}

	@Override
	public Label getPostCommentHeading()
	{
		return LABEL_ENTERMSG;
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
