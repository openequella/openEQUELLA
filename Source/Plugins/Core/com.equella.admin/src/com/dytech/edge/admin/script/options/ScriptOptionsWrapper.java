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

package com.dytech.edge.admin.script.options;

import java.util.List;

import com.tle.common.NameValue;

/**
 * @author nread
 */
public class ScriptOptionsWrapper implements ScriptOptions
{
	private final ScriptOptions parent;

	public ScriptOptionsWrapper(ScriptOptions parent)
	{
		this.parent = parent;
	}

	@Override
	public boolean hasWorkflow()
	{
		return parent.hasWorkflow();
	}

	@Override
	public boolean hasItemStatus()
	{
		return parent.hasItemStatus();
	}

	@Override
	public boolean restrictItemStatusForModeration()
	{
		return parent.restrictItemStatusForModeration();
	}

	@Override
	public List<NameValue> getWorkflowSteps()
	{
		return parent.getWorkflowSteps();
	}

	@Override
	public String getWorkflowStepName(String value)
	{
		return parent.getWorkflowStepName(value);
	}

	@Override
	public boolean hasUserIsModerator()
	{
		return parent.hasUserIsModerator();
	}
}
