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
