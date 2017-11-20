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

package com.tle.web.workflow.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.ItemTaskId;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.beans.workflow.WorkflowStep;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionTree.DelayedRegistration;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.DateRenderer;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.workflow.manage.ViewCommentsDialog;

@SuppressWarnings("nls")
@Bind
public class CurrentModerationContentSection
	extends
		AbstractContentSection<CurrentModerationContentSection.CurrentModerationContentModel>
{
	@PlugKey("tasklist.comments")
	private static String KEY_COMMENTS;
	@PlugKey("summary.content.currentmoderation.pagetitle")
	private static Label TITLE_LABEL;
	@PlugKey("summary.content.currentmoderation.awaiting.taskname")
	private static Label LABEL_TASK_NAME;
	@PlugKey("summary.content.currentmoderation.awaiting.moderators")
	private static Label LABEL_MODERATORS;
	@PlugKey("summary.content.currentmoderation.awaiting.waiting")
	private static Label LABEL_WAITING_FOR;
	@PlugKey("summary.content.currentmoderation.awaiting.unanimous")
	private static Label LABEL_UNANIMOUS;
	@PlugKey("summary.content.currentmoderation.awaiting.onlyone")
	private static Label LABEL_ONLYONE;
	@PlugKey("summary.content.currentmoderation.thumbnail.alt")
	private static Label LABEL_PROG_THUMB_ALT;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component(name = "m")
	private Table moderatorsTable;
	@Component
	private Link flowchartLink;

	@Inject
	private ViewItemUrlFactory viewItemUrlFactory;
	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;
	@Inject
	private CurrentModerationLinkSection linkSection;
	@Inject
	private ViewCommentsDialog commentsDialog;
	@Inject
	private WorkflowService workflowService;
	@Inject
	private DateRendererFactory dateRendererFactory;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		flowchartLink.setStyleClass("flowchart-thumb");

		userLinkSection = userLinkService.register(tree, id);
		tree.registerInnerSection(commentsDialog, id);
		tree.addDelayedRegistration(new DelayedRegistration()
		{

			@Override
			public void register(SectionTree tree)
			{
				String placeHolder = tree.getPlaceHolder("com.tle.web.viewitem.summary.sidebar.DETAILS_GROUP");
				tree.registerSections(linkSection, placeHolder, null, false);
			}
		});

		moderatorsTable.setColumnHeadings(LABEL_TASK_NAME, LABEL_MODERATORS, LABEL_WAITING_FOR);
		moderatorsTable.setColumnSorts(Sort.NONE, Sort.NONE, Sort.PRIMARY_ASC);
	}

	public HtmlLinkState getCommentLink(SectionInfo info, ItemTaskId itemTaskId, int comments)
	{
		return new HtmlLinkState(new PluralKeyLabel(KEY_COMMENTS, comments),
			new OverrideHandler(commentsDialog.getOpenFunction(), itemTaskId));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);
		final WorkflowStatus status = itemInfo.getWorkflowStatus();
		final CurrentModerationContentModel model = getModel(context);

		if( !status.isModerating() )
		{
			return null;
		}
		Date start = itemInfo.getItem().getModeration().getStart();
		model.setTotalTime(dateRendererFactory.createDateRenderer(start, true));

		final TableState moderatorsTableState = moderatorsTable.getState(context);
		for( WorkflowStep step : status.getCurrentSteps() )
		{
			final ItemTaskId itemTaskId = new ItemTaskId(itemInfo.getItemId(), step.getUuid());
			final List<WorkflowMessage> comments = workflowService.getCommentsForTask(itemTaskId);
			HtmlLinkState commentLink = null;
			if( !comments.isEmpty() )
			{
				commentLink = getCommentLink(context, itemTaskId, comments.size());
			}

			final Label moderatorsLabel = (step.isUnanimous() ? LABEL_UNANIMOUS : LABEL_ONLYONE);
			final List<HtmlLinkState> moderatorList = convertUserList(context, step);

			moderatorsTableState
				.addRow(new BundleLabel(step.getDisplayName(), bundleCache),
					viewFactory.createResultWithModelMap("moderatorscolumn.ftl", "moderatorsLabel", moderatorsLabel,
						"moderatorList", moderatorList, "commentLink", commentLink),
				dateRendererFactory.createDateRenderer(step.getStatus().getStarted(), true))
				.setSortData(null, null, step.getStatus().getStarted());
		}

		final ViewItemUrl flowchartUrl = viewItemUrlFactory.createItemUrl(context, itemInfo.getViewableItem(),
			UrlEncodedString.createFromFilePath("statusimage.png"), ViewItemUrl.FLAG_IS_RESOURCE);

		flowchartLink.setBookmark(context, flowchartUrl);
		flowchartLink.getState(context).setTitle(LABEL_PROG_THUMB_ALT);

		model.setFlowchartThumb(new ImageRenderer(flowchartUrl.getHref(), LABEL_PROG_THUMB_ALT));

		addDefaultBreadcrumbs(context, itemInfo, TITLE_LABEL);

		return viewFactory.createResult("currentmoderators.ftl", context);
	}

	private List<HtmlLinkState> convertUserList(SectionInfo info, WorkflowStep step)
	{
		final List<HtmlLinkState> rv = new ArrayList<HtmlLinkState>();

		// All users
		rv.addAll(userLinkSection.createLinks(info, step.getToModerate()));
		rv.addAll(userLinkSection.createRoleLinks(info, step.getRolesToModerate()));
		return rv;
	}

	public Table getModeratorsTable()
	{
		return moderatorsTable;
	}

	public Link getFlowchartLink()
	{
		return flowchartLink;
	}

	@Override
	public Class<CurrentModerationContentModel> getModelClass()
	{
		return CurrentModerationContentModel.class;
	}

	public static class CurrentModerationContentModel
	{
		private DateRenderer totalTime;
		private SectionRenderable flowchartThumb;

		public SectionRenderable getFlowchartThumb()
		{
			return flowchartThumb;
		}

		public void setFlowchartThumb(SectionRenderable flowchartThumb)
		{
			this.flowchartThumb = flowchartThumb;
		}

		public DateRenderer getTotalTime()
		{
			return totalTime;
		}

		public void setTotalTime(DateRenderer dateRenderer)
		{
			this.totalTime = dateRenderer;
		}
	}
}
