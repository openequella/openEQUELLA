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

package com.tle.web.workflow.manage;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.workflow.TaskModerator;
import com.tle.common.workflow.TaskModerator.Type;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.DelimitedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class TaskModeratorsDialog extends EquellaDialog<TaskModeratorsDialog.Model>
{
	@PlugKey("moddialog.title")
	private static Label LABEL_TITLE;
	@PlugKey("moddialog.and")
	private static Label LABEL_AND;
	@PlugKey("moddialog.others")
	private static String KEY_OTHERS;

	@Inject
	private WorkflowService workflowService;

	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	public TaskModeratorsDialog()
	{
		setAjax(true);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		userLinkSection = userLinkService.register(tree, id);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	public Model instantiateDialogModel(SectionInfo info)
	{
		return new Model();
	}

	@Override
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("openMods"); //$NON-NLS-1$
	}

	@EventHandlerMethod
	public void openMods(SectionInfo info, ItemTaskId taskId)
	{
		getModel(info).setTaskId(taskId);
		showDialog(info);
	}

	@Override
	public String getWidth()
	{
		return "auto";
	}

	@Override
	public String getHeight()
	{
		return "auto";
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		Model model = getModel(context);
		List<TaskModerator> moderatorList = workflowService.getModeratorList(model.getTaskId(), true);
		Multimap<Boolean, ModRow> rows = ArrayListMultimap.create();
		for( TaskModerator taskModerator : moderatorList )
		{
			HtmlLinkState link = createLinkForModerator(context, taskModerator);
			if( link != null )
			{
				rows.put(taskModerator.isAccepted(), new ModRow(link, false));
			}
		}
		model.setModerators(rows.get(false));
		model.setModeratorsAccepted(rows.get(true));
		return viewFactory.createResult("moddialog.ftl", this);
	}

	@Nullable
	private HtmlLinkState createLinkForModerator(SectionInfo info, TaskModerator moderator)
	{
		HtmlLinkState link = null;
		if( moderator.getType() == Type.USER )
		{
			link = userLinkSection.createLink(info, moderator.getId());
		}
		else if( moderator.getType() == Type.ROLE )
		{
			link = userLinkSection.createRoleLink(info, moderator.getId());
		}
		return link;
	}

	public static class Model extends DialogModel
	{
		@Bookmarked
		private ItemTaskId taskId;

		private Collection<ModRow> moderators;
		private Collection<ModRow> moderatorsAccepted;

		public Collection<ModRow> getModerators()
		{
			return moderators;
		}

		public void setModerators(Collection<ModRow> moderators)
		{
			this.moderators = moderators;
		}

		public ItemTaskId getTaskId()
		{
			return taskId;
		}

		public void setTaskId(ItemTaskId taskId)
		{
			this.taskId = taskId;
		}

		public Collection<ModRow> getModeratorsAccepted()
		{
			return moderatorsAccepted;
		}

		public void setModeratorsAccepted(Collection<ModRow> moderatorsAccepted)
		{
			this.moderatorsAccepted = moderatorsAccepted;
		}

	}

	public static class ModRow
	{
		private final HtmlLinkState moderator;
		private final boolean accepted;

		public ModRow(HtmlLinkState moderator, boolean accetped)
		{
			this.moderator = moderator;
			this.accepted = accetped;
		}

		public HtmlLinkState getModerator()
		{
			return moderator;
		}

		public boolean isAccepted()
		{
			return accepted;
		}

	}

	public SectionRenderable getListLink(SectionInfo info, List<TaskModerator> mods, ItemTaskId taskId)
	{
		if( mods.size() > 0 )
		{
			SectionRenderable listLink = new LinkRenderer(createLinkForModerator(info, mods.get(0)));
			if( mods.size() == 1 )
			{
				return listLink;
			}
			LinkRenderer otherLink = new LinkRenderer(new HtmlLinkState(new PluralKeyLabel(KEY_OTHERS, mods.size() - 1),
				new OverrideHandler(getOpenFunction(), taskId)));
			return new DelimitedRenderer(" ", listLink, LABEL_AND, otherLink);
		}
		return null;
	}
}
