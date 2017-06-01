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

package com.tle.web.viewitem.summary.content;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.events.WorkflowEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;

@NonNullByDefault
@Bind
public class HistoryCommentDialog extends EquellaDialog<HistoryCommentDialog.HistoryCommentModel>
{
	@PlugKey("summary.content.history.comment.title")
	private static Label TITLE_LABEL;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		setAjax(true);
	}

	@Override
	@SuppressWarnings("nls")
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("showComment");
	}

	@EventHandlerMethod
	public void showComment(SectionInfo info, long commentId)
	{
		HistoryCommentModel model = getModel(info);
		model.setCommentId(commentId);
		super.showDialog(info);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return TITLE_LABEL;
	}

	@Nullable
	@Override
	@SuppressWarnings("nls")
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		final long commentId = getModel(context).getCommentId();

		WorkflowStatus ws = ParentViewItemSectionUtils.getItemInfo(context).getWorkflowStatus();
		for( WorkflowEvent we : ws.getEvents() )
		{
			if( we.getId() == commentId )
			{
				return new DivRenderer(new LabelRenderer(new TextLabel(SectionUtils.ent(we.getComment()).replaceAll(
					"\n", "<br>"), true)));
			}
		}

		return null;
	}

	@Override
	@SuppressWarnings("nls")
	public String getWidth()
	{
		return "600px";
	}

	@Override
	public HistoryCommentModel instantiateDialogModel(SectionInfo info)
	{
		return new HistoryCommentModel();
	}

	public static class HistoryCommentModel extends DialogModel
	{
		private long commentId;

		public void setCommentId(long commentId)
		{
			this.commentId = commentId;
		}

		public long getCommentId()
		{
			return commentId;
		}
	}
}
