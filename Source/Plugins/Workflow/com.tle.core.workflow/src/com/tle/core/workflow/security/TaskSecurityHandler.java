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

package com.tle.core.workflow.security;

import static com.tle.common.security.SecurityConstants.TARGET_WORKFLOW_DYNAMIC_TASK;
import static com.tle.common.security.SecurityConstants.TARGET_WORKFLOW_TASK;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemTask;
import com.tle.common.workflow.Workflow;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.impl.ItemSecurityTargetHandler;
import com.tle.core.security.SecurityTargetHandler;

@Bind
@Singleton
public class TaskSecurityHandler implements SecurityTargetHandler
{
	@Inject
	private ItemSecurityTargetHandler itemSecurityHandler;

	@SuppressWarnings("nls")
	@Override
	public void gatherAllLabels(Set<String> labels, Object target)
	{
		ItemTask itemTask = (ItemTask) target;

		Item item = itemTask.getItem();
		Workflow workflow = item.getItemDefinition().getWorkflow();
		if( workflow != null )
		{
			String taskId = itemTask.getTaskId();
			labels.add(TARGET_WORKFLOW_TASK + ":" + workflow.getId() + ":" + taskId);
			labels.add(TARGET_WORKFLOW_DYNAMIC_TASK + ":" + item.getId() + ":" + taskId);
		}
		itemSecurityHandler.gatherAllLabels(labels, item);
	}

	@Override
	public String getPrimaryLabel(Object target)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object transform(Object target)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOwner(Object target, String userId)
	{
		throw new UnsupportedOperationException();
	}

}
