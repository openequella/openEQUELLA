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
public class ApproveDialog extends AbstractTaskActionDialog
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
	public ButtonRenderer.ButtonType getButtonType()
	{
		return ButtonRenderer.ButtonType.ACCEPT;
	}

	@Override
	public Label getButtonLabel()
	{
		return LABEL_APPROVE_BUTTON;
	}

	@Override
	public CurrentTaskSection.CommentType getActionType()
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

	public Label validate(SectionInfo info)
	{
		TaskActionDialogModel model = getModel(info);
		String stagingUuid = model.stagingFolderUuid();
		StagingFile stagingFolder = new StagingFile(stagingUuid);
		long countFiles = fileSystemService().countFiles(stagingFolder, null);
		if (countFiles > 1 && Check.isEmpty(_commentField().getValue(info).trim()))
		{
			return LABEL_ENTERMSG_WITHFILES;
		}
		return null;
	}

	@Override
	public boolean isMandatoryMessage()
	{
		return false;
	}

}
