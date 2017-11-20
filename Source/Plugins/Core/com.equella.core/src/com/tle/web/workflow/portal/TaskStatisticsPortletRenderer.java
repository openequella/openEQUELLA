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

package com.tle.web.workflow.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.security.SettingsTarget;
import com.tle.common.workflow.Trend;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.workflow.TaskTrend;
import com.tle.core.workflow.service.TaskStatisticsService;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemadmin.section.ItemAdminFilterByItemStatusSection;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.search.filter.FilterByItemStatusSection;
import com.tle.web.search.filter.ResetFiltersParent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.workflow.manage.FilterByWorkflowSection;
import com.tle.web.workflow.manage.RootTaskManagementSection;
import com.tle.web.workflow.manage.WorkflowListModel;
import com.tle.web.workflow.tasks.FilterByWorkflowTaskSection;

@Bind
@SuppressWarnings("nls")
public class TaskStatisticsPortletRenderer
	extends
		PortletContentRenderer<TaskStatisticsPortletRenderer.TaskStatisticsPortletRendererModel>
{
	private static final String KEY_DEFAULT_TREND = "trend";
	private static final String KEY_DEFAULT_WORKFLOW = "default.workflow";

	@PlugKey("portal.taskstats.trend.")
	private static String PREFIX;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	private TaskStatisticsService taskStatsService;
	@Inject
	private UserPreferenceService userPreferenceService;
	@Inject
	private WorkflowService workflowService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private TLEAclManager aclService;

	@Component(stateful = false)
	private SingleSelectionList<BaseEntityLabel> workflowSelector;
	@Component(stateful = false)
	private SingleSelectionList<Trend> trendSelector;
	@PlugKey("portal.taskstats.itemcount")
	@Component
	private Link itemsInWorkflowLink;

	@Override
	public void registered(final String id, SectionTree tree)
	{
		super.registered(id, tree);
		trendSelector.setListModel(new EnumListModel<Trend>(PREFIX, Trend.values())
		{
			@Override
			public String getDefaultValue(SectionInfo info)
			{
				return portlet.getAttribute(KEY_DEFAULT_TREND);
			}
		});
		trendSelector.setAlwaysSelect(true);
		trendSelector.addChangeEventHandler(new OverrideHandler(ajax.getAjaxUpdateDomFunction(tree, this, null,
			ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), id + "taskstatsresults", id + "trendselector")));

		workflowSelector.setListModel(new WorkflowListModel(workflowService, bundleCache)
		{
			@Override
			public String getDefaultValue(SectionInfo info)
			{
				return userPreferenceService.getPreference(KEY_DEFAULT_WORKFLOW + '.' + id);
			}
		});
		workflowSelector.setAlwaysSelect(true);
		workflowSelector.addChangeEventHandler(
			new OverrideHandler(ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("workflowChanged"),
				ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), id + "taskstatsresults")));

		itemsInWorkflowLink.setClickHandler(events.getNamedHandler("showItemsInWorkflow"));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		TaskStatisticsPortletRendererModel model = getModel(context);
		boolean hasManageable = workflowSelector.getListModel().getOptions(context).size() > 1;
		model.setShowManageable(hasManageable);
		if( hasManageable )
		{
			String workflow = workflowSelector.getSelectedValueAsString(context);
			List<TaskStatRow> stats = new ArrayList<TaskStatRow>();
			List<TaskTrend> waitingTasks;

			if( !Check.isEmpty(workflow) )
			{
				waitingTasks = taskStatsService.getWaitingTasksForWorkflow(workflow,
					trendSelector.getSelectedValue(context));
				model.setItemCount(workflowService.getItemCountForWorkflow(workflow));
				model.setShowCount(true);
			}
			else
			{
				waitingTasks = taskStatsService.getWaitingTasks(trendSelector.getSelectedValue(context));
				model.setShowCount(false);
			}

			buildTaskStatisticRows(stats, waitingTasks);
			model.setTaskstats(stats);

			model.setShowItemsInWorkflow(!aclService.filterNonGrantedPrivileges(new SettingsTarget("itemadmin"),
				Collections.singleton("VIEW_MANAGEMENT_PAGE")).isEmpty());
		}

		return viewFactory.createResult("portal/taskstatistics.ftl", context);
	}

	private void buildTaskStatisticRows(List<TaskStatRow> stats, List<TaskTrend> waitingTasks)
	{
		for( TaskTrend task : waitingTasks )
		{
			stats.add(new TaskStatRow(task.getWorkflowItemId(), task.getNameId(), task.getWaiting(), task.getTrend()));
		}
	}

	@EventHandlerMethod
	public void workflowChanged(SectionInfo info)
	{
		String selWorkflow = workflowSelector.getSelectedValueAsString(info);
		userPreferenceService.setPreference(KEY_DEFAULT_WORKFLOW + '.' + getSectionId(), selWorkflow);
	}

	@EventHandlerMethod
	public void showItemsInWorkflow(SectionInfo info)
	{
		SectionInfo fwd = info.createForward("/access/itemadmin.do");

		ResetFiltersParent resetFilters = fwd.lookupSection(ResetFiltersParent.class);
		resetFilters.getResetFiltersSection().resetFilters(fwd);

		// NB - map lookup requires specifying exact subclass of
		// FilterByItemStatusSection
		FilterByItemStatusSection statusSection = fwd.lookupSection(ItemAdminFilterByItemStatusSection.class);
		statusSection.setOnlyInModeration(fwd, true);

		FilterByWorkflowSection workflowSection = fwd.lookupSection(FilterByWorkflowSection.class);
		workflowSection.setWorkflow(fwd, workflowSelector.getSelectedValueAsString(info));

		info.forward(fwd);
	}

	@EventHandlerMethod
	public void showTaskFilter(SectionInfo oldInfo, long taskId)
	{
		SectionInfo info = RootTaskManagementSection.create(oldInfo);
		WorkflowItem manageableTask = workflowService.getManageableTask(taskId);
		FilterByWorkflowTaskSection workflowTaskSection = info.lookupSection(FilterByWorkflowTaskSection.class);
		workflowTaskSection.setWorkflowTask(info, manageableTask);
		oldInfo.forwardAsBookmark(info);
	}

	@Override
	public Class<TaskStatisticsPortletRendererModel> getModelClass()
	{
		return TaskStatisticsPortletRendererModel.class;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	public static class TaskStatisticsPortletRendererModel
	{
		private List<TaskStatRow> taskstats = new ArrayList<TaskStatRow>();
		private boolean showManageable;
		private boolean showCount;
		private boolean showItemsInWorkflow;
		private int itemCount;

		public List<TaskStatRow> getTaskstats()
		{
			return taskstats;
		}

		public void setTaskstats(List<TaskStatRow> taskstats)
		{
			this.taskstats = taskstats;
		}

		public int getItemCount()
		{
			return itemCount;
		}

		public void setItemCount(int itemCount)
		{
			this.itemCount = itemCount;
		}

		public void setShowCount(boolean showCount)
		{
			this.showCount = showCount;
		}

		public boolean isShowCount()
		{
			return showCount;
		}

		public boolean isShowManageable()
		{
			return showManageable;
		}

		public void setShowManageable(boolean showManageable)
		{
			this.showManageable = showManageable;
		}

		public boolean isShowItemsInWorkflow()
		{
			return showItemsInWorkflow;
		}

		public void setShowItemsInWorkflow(boolean showItemsInWorkflow)
		{
			this.showItemsInWorkflow = showItemsInWorkflow;
		}
	}

	public SingleSelectionList<BaseEntityLabel> getWorkflowSelector()
	{
		return workflowSelector;
	}

	public SingleSelectionList<Trend> getTrendSelector()
	{
		return trendSelector;
	}

	public Link getItemsInWorkflowLink()
	{
		return itemsInWorkflowLink;
	}

	public class TaskStatRow
	{
		private final Label label;
		private final int waiting;
		private final String trend;
		private final TagRenderer row;

		public TaskStatRow(long taskId, long nameId, int waiting, int trend)
		{
			this.label = new BundleLabel(nameId, bundleCache);
			this.waiting = waiting;
			this.trend = trend > 0 ? "+" + trend : Integer.toString(trend);
			TagState state = new TagState();
			state.setClickHandler(new OverrideHandler(events.getNamedHandler("showTaskFilter", taskId)));
			this.row = new TagRenderer("tr", state);
		}

		public Label getLabel()
		{
			return label;
		}

		public int getWaiting()
		{
			return waiting;
		}

		public String getTrend()
		{
			return trend;
		}

		public TagRenderer getRow()
		{
			return row;
		}
	}
}
