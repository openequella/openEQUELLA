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

import com.tle.beans.item.ItemTaskId;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.beans.workflow.WorkflowStep;
import com.tle.web.sections.equella.converter.AbstractSessionState;

public class TaskListState extends AbstractSessionState
{
	private static final long serialVersionUID = 1L;

	private static final String SESSION_ID = TaskListState.class.getName();
	private final ItemTaskId itemTaskId;
	private WorkflowStatus status;
	private WorkflowStep currentStep;
	private int commentCount;
	private boolean editing;
	private int index;
	private int listSize;

	public TaskListState()
	{
		itemTaskId = null;
	}

	public TaskListState(ItemTaskId itemTaskId, int index, int listSize)
	{
		this.itemTaskId = itemTaskId;
		this.index = index;
		this.listSize = listSize;
		modified = true;
	}

	public WorkflowStep getCurrentStep()
	{
		return currentStep;
	}

	public int getIndex()
	{
		return index;
	}

	public String getTaskId()
	{
		return currentStep.getUuid();
	}

	public ItemTaskId getItemTaskId()
	{
		return itemTaskId;
	}

	@Override
	public String getSessionId()
	{
		return SESSION_ID;
	}

	@Override
	public String getBookmarkString()
	{
		return "1"; //$NON-NLS-1$
	}

	@Override
	public boolean isNew()
	{
		return false;
	}

	public int getListSize()
	{
		return listSize;
	}

	public WorkflowStatus getStatus()
	{
		return status;
	}

	public synchronized void setWorkflowStatus(WorkflowStatus status, WorkflowStep step, int commentCount)
	{
		this.status = status;
		this.currentStep = step;
		this.commentCount = commentCount;
		modified = true;
	}

	public boolean isEditing()
	{
		return editing;
	}

	public void setEditing(boolean editing)
	{
		this.editing = editing;
		modified = true;
	}

	public int getCommentCount()
	{
		return commentCount;
	}

	public void setCommentCount(int commentCount)
	{
		this.commentCount = commentCount;
	}
}
