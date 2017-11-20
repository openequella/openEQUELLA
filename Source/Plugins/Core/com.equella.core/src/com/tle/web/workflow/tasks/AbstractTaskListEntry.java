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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.TaskResult;
import com.tle.core.services.user.UserService;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.itemlist.item.AbstractItemListEntry;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.equella.render.JQueryTimeAgo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.renderers.ImageRenderer;

public class AbstractTaskListEntry extends AbstractItemListEntry
{
	private static final String KEY_STEPMAPS = "STEP_MAPS"; //$NON-NLS-1$

	@PlugKey("tasklist.step")
	private static Label LABEL_STEP;
	@PlugKey("tasklist.timeat")
	private static Label LABEL_TIMEAT;
	@PlugKey("tasklist.duedate")
	private static Label LABEL_DUEDATE;
	@PlugKey("tasklist.expired")
	private static Label LABEL_EXPIRED;
	@PlugKey("tasklist.priority")
	private static Label LABEL_PRIORITY;
	@PlugKey("tasklist.priority.val.")
	private static String KEY_PRIORITY_VAL;
	@PlugKey("tasklist.workflow")
	private static Label LABEL_WORKFLOW;
	@PlugKey("tasklist.unknownstep")
	private static Label LABEL_UNKNOWN_STEP;
	@PlugKey("tasklist.assignee")
	private static Label LABEL_ASSIGNED_TO;
	@PlugKey("moderate.assignedtome")
	private static Label LABEL_TOME;
	@PlugKey("moderate.unassigned")
	private static Label LABEL_UNASSIGNED;

	@PlugURL("images/icons/warning-large.png")
	private static String URL_HIGH;
	@PlugURL("images/icons/assigned-to.png")
	private static String ASSIGNED_TO_ICON;

	@Inject
	private WorkflowService workflowService;
	@Inject
	private DateRendererFactory dateRendererFactory;
	@Inject
	protected UserService userService;

	private ImageRenderer image;
	private ImageRenderer assignedToImage;

	public ItemTaskId getItemTaskId()
	{
		TaskResult taskResult = getAttribute(FreetextResult.class);
		String taskId = taskResult.getTaskId();
		return new ItemTaskId(getItem().getItemId(), taskId);
	}

	@Override
	protected void setupMetadata(RenderContext info)
	{
		super.setupMetadata(info);

		Map<Long, Map<String, WorkflowItem>> stepMaps = listSettings.getAttribute(KEY_STEPMAPS);
		if( stepMaps == null )
		{
			stepMaps = new HashMap<Long, Map<String, WorkflowItem>>();
			listSettings.setAttribute(KEY_STEPMAPS, stepMaps);
			JQueryTimeAgo.enableFutureTimes(info);
		}
		Workflow workflow = getItem().getItemDefinition().getWorkflow();
		addDelimitedMetadata(LABEL_WORKFLOW, new BundleLabel(workflow.getName(), bundleCache));
		Map<String, WorkflowItem> stepMap = stepMaps.get(workflow.getId());
		if( stepMap == null )
		{
			stepMap = workflow.getAllWorkflowItems();
			stepMaps.put(workflow.getId(), stepMap);
		}
		ItemTaskId itemTaskId = getItemTaskId();
		WorkflowItem witem = stepMap.get(itemTaskId.getTaskId());
		WorkflowItemStatus itemStatus = workflowService.getIncompleteStatus(itemTaskId);
		if( itemStatus != null )
		{
			List<Object> stepData = Lists.newArrayList();
			stepData.add(new BundleLabel(witem.getName(), bundleCache));
			Date dateDue = itemStatus.getDateDue();
			if( dateDue != null )
			{
				Label dueLabel = LABEL_EXPIRED;
				if( dateDue.getTime() > System.currentTimeMillis() )
				{
					dueLabel = LABEL_DUEDATE;
				}
				stepData.add(rendererFactory.convertToRenderer(dueLabel, getTimeRenderer(dateDue)));
			}

			addDelimitedMetadata(LABEL_STEP, stepData);
			addDelimitedMetadata(LABEL_TIMEAT, dateRendererFactory.createDateRenderer(itemStatus.getStarted(), true));
		}
		else
		{
			// very occasionally itemStatus is null. Fixed on a page refresh
			addDelimitedMetadata(LABEL_STEP, LABEL_UNKNOWN_STEP);
		}
		int priority = witem.getPriority();
		KeyLabel priLabel = new KeyLabel(KEY_PRIORITY_VAL + priority);
		if( priority == WorkflowItem.Priority.HIGH.intValue() )
		{
			image = new ImageRenderer(URL_HIGH, priLabel);
			image.addClass("cornerimage"); //$NON-NLS-1$
		}
		addDelimitedMetadata(LABEL_PRIORITY, priLabel);
		if( itemStatus != null )
		{
			String assignedTo = itemStatus.getAssignedTo();
			if( assignedTo != null && assignedTo.equals(CurrentUser.getUserID()) )
			{
				addDelimitedMetadata(LABEL_ASSIGNED_TO, LABEL_TOME);
				assignedToImage = new ImageRenderer(ASSIGNED_TO_ICON, LABEL_ASSIGNED_TO);
				assignedToImage.addClass("cornerimage");
			}
			else
			{
				if( assignedTo == null )
				{
					addDelimitedMetadata(LABEL_ASSIGNED_TO, LABEL_UNASSIGNED);
				}
				else
				{
					UserBean user = userService.getInformationForUser(assignedTo);
					addDelimitedMetadata(LABEL_ASSIGNED_TO, user.getFirstName() + " " + user.getLastName());
				}

			}

		}

	}

	@Override
	public SectionRenderable getIcon()
	{
		return image;
	}

	public SectionRenderable getAssignedIcon()
	{
		return assignedToImage;
	}
}
