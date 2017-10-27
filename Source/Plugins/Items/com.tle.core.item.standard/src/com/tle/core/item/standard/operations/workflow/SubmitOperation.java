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

package com.tle.core.item.standard.operations.workflow;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.HistoryEvent.Type;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.security.impl.SecureItemStatus;

/**
 * @author jmaginnis
 */
@SecureItemStatus(ItemStatus.DRAFT)
public class SubmitOperation extends TaskOperation
{
	private String message;

	@AssistedInject
	public SubmitOperation(@Nullable @Assisted String message)
	{
		this.message = message;
	}

	@AssistedInject
	public SubmitOperation()
	{
		// no message;
	}

	@Override
	public boolean execute()
	{
		Workflow workflow = getWorkflow();
		if( workflow != null )
		{
			setState(ItemStatus.MODERATING);
		}
		// ...else, we don't have a workflow so let the resetWorkflow() call
		// make the status change. This will still put us in the "moderating"
		// status if we have a workflow but fall straight through to Live, but
		// that's fine.

		resetWorkflow();
		if( workflow != null && !Check.isEmpty(message) )
		{
			String taskId = workflow.getRoot().getUuid();
			HistoryEvent comment = createHistory(Type.comment);
			comment.setComment(message);
			setStepFromTask(comment, taskId);
			addMessage(taskId, WorkflowMessage.TYPE_SUBMIT, message, null);
		}
		getModerationStatus().setLastAction(params.getDateNow());
		updateModeration();
		return true;
	}
}
