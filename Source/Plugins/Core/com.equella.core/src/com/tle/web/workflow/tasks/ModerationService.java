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

package com.tle.web.workflow.tasks;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.beans.item.ItemTaskId;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.beans.workflow.WorkflowStep;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.operations.workflow.StatusOperation;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.generic.AbstractSectionFilter;
import com.tle.web.sections.registry.TreeRegistry;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ModerationService extends AbstractSectionFilter
{
	static
	{
		PluginResourceHandler.init(ModerationService.class);
	}

	public static final String VIEW_METADATA = "metadata";
	public static final String VIEW_SUMMARY = "summary";
	public static final String VIEW_PROGRESS = "progress";

	private static final String TREE_NAME = "$CURRENTTASK$"; //$NON-NLS-1$

	@Inject
	private TreeRegistry treeRegistry;
	@Inject
	private ItemService itemService;
	@Inject
	private WorkflowService workflowService;
	@Inject
	private Provider<StatusOperation> statusOpFactpry;

	private PluginTracker<ModerationView> tracker;

	@Override
	protected SectionTree getFilterTree()
	{
		return treeRegistry.getTreeForPath(TREE_NAME);
	}

	public TaskListState refreshCurrentTask(SectionInfo info, TaskListState state)
	{
		StatusOperation statusOp = statusOpFactpry.get();
		ItemTaskId taskId = state.getItemTaskId();
		itemService.operation(taskId, statusOp);
		WorkflowStatus status = statusOp.getStatus();
		state.setWorkflowStatus(status, status.getStepForId(taskId.getTaskId()),
			workflowService.getMessageCount(taskId));
		return state;
	}

	public void moderate(SectionInfo info, String viewType, ItemTaskId taskId, int index, int listSize)
	{
		StatusOperation statusOp = statusOpFactpry.get();
		itemService.operation(taskId, statusOp);
		WorkflowStatus status = statusOp.getStatus();
		TaskListState state = new TaskListState(taskId, index, listSize);
		WorkflowStep currentStep = status.getStepForId(taskId.getTaskId());
		if( currentStep == null )
		{
			throw new NotModeratingStepException("Moderating task no longer exists: " + taskId);
		}
		state.setWorkflowStatus(status, currentStep, workflowService.getMessageCount(taskId));
		forwardToView(info, state, viewType);
	}

	private void forwardToView(SectionInfo info, TaskListState state, String viewer)
	{
		ModerationView modViewer = tracker.getBeanByExtension(tracker.getExtension(viewer));
		SectionInfo forward = modViewer.getViewForward(info, state.getItemTaskId(), viewer);
		CurrentTaskSection taskSection = forward.lookupSection(CurrentTaskSection.class);
		taskSection.setupState(forward, state);
		info.forwardAsBookmark(forward);
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		tracker = new PluginTracker<ModerationView>(pluginService, "com.tle.web.workflow", "moderationView", "id")
			.setBeanKey("bean");
	}

	public void setEditing(SectionInfo info, boolean editing)
	{
		TaskListState state = getCurrentTaskState(info);
		if( state != null )
		{
			state.setEditing(editing);
		}
	}

	public boolean isModerating(SectionInfo info)
	{
		CurrentTaskSection currentTask = info.lookupSection(CurrentTaskSection.class);
		return currentTask != null && currentTask.getCurrentState(info) != null;
	}

	private TaskListState getCurrentTaskState(SectionInfo info)
	{
		CurrentTaskSection taskSection = info.lookupSection(CurrentTaskSection.class);
		if( taskSection == null )
		{
			return null;
		}
		return taskSection.getCurrentState(info);
	}

	public void viewSummary(SectionInfo info)
	{
		forwardToView(info, getCurrentTaskState(info), VIEW_SUMMARY);
	}

	public void viewMetadata(SectionInfo info)
	{
		forwardToView(info, getCurrentTaskState(info), VIEW_METADATA);
	}

	public ItemTaskId getCurrentTaskId(SectionInfo info)
	{
		return getCurrentTaskState(info).getItemTaskId();
	}
}
