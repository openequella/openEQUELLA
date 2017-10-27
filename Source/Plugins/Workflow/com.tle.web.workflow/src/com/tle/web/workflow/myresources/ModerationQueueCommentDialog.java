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

package com.tle.web.workflow.myresources;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.workflow.service.WorkflowService;
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

@NonNullByDefault
@Bind
public class ModerationQueueCommentDialog
	extends
		EquellaDialog<ModerationQueueCommentDialog.ModerationQueueCommentModel>
{
	@PlugKey("modqueue.comment.title")
	private static Label TITLE_LABEL;

	@Inject
	private ItemService itemService;
	@Inject
	private WorkflowService workflowService;

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
	public void showComment(SectionInfo info, String itemId)
	{
		ModerationQueueCommentModel model = getModel(info);
		model.setItemId(itemId);
		super.showDialog(info);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return TITLE_LABEL;
	}

	@Override
	@SuppressWarnings("nls")
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		Item item = itemService.get(new ItemId(getModel(context).getItemId()));
		String message = workflowService.getLastRejectionMessage(item);
		return new DivRenderer(
			new LabelRenderer(new TextLabel(SectionUtils.ent(message).replaceAll("\n", "<br>"), true)));
	}

	@Override
	@SuppressWarnings("nls")
	public String getWidth()
	{
		return "300px";
	}

	@Override
	public ModerationQueueCommentModel instantiateDialogModel(SectionInfo info)
	{
		return new ModerationQueueCommentModel();
	}

	public static class ModerationQueueCommentModel extends DialogModel
	{
		private String itemId;

		public String getItemId()
		{
			return itemId;
		}

		public void setItemId(String itemId)
		{
			this.itemId = itemId;
		}
	}
}
