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

package com.tle.core.workflow;

public class TaskTrend
{
	private final long workflowItemId;
	private final long nameId;
	private final int waiting;
	private int trend;

	public TaskTrend(long workflowItemId, long nameId, int waiting)
	{
		this.workflowItemId = workflowItemId;
		this.nameId = nameId;
		this.waiting = waiting;
		this.trend = waiting;
	}

	public int getTrend()
	{
		return trend;
	}

	public void setTrend(int trend)
	{
		this.trend = trend;
	}

	public long getNameId()
	{
		return nameId;
	}

	public int getWaiting()
	{
		return waiting;
	}

	public long getWorkflowItemId()
	{
		return workflowItemId;
	}
}