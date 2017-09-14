package com.tle.web.workflow.tasks.dialog;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
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
	protected CurrentTaskSection.CommentType getActionType()
	{
		return CurrentTaskSection.CommentType.ACCEPT;
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

	protected Label validate(SectionInfo info)
	{
		AbstractTaskActionDialogModel model = getModel(info);
		String stagingUuid = model.getStagingFolderUuid();
		StagingFile stagingFolder = new StagingFile(stagingUuid);
		long countFiles = fileSystemService.countFiles(stagingFolder, null);
		if (countFiles > 1 && Check.isEmpty(getCommentField().getValue(info).trim()))
		{
			return LABEL_ENTERMSG_WITHFILES;
		}
		return null;
	}

}
