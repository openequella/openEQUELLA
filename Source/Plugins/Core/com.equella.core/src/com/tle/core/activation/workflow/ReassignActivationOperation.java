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

package com.tle.core.activation.workflow;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.activation.ActivateRequest;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;

public class ReassignActivationOperation extends AbstractStandardWorkflowOperation
{
	@Inject
	private ActivateRequestDao dao;

	private final String fromUserId;
	private final String toUserId;

	@AssistedInject
	public ReassignActivationOperation(@Assisted("fromUserId") String fromUserId, @Assisted("toUserId") String toUserId)
	{
		this.fromUserId = fromUserId;
		this.toUserId = toUserId;
	}

	@Override
	public boolean execute()
	{
		boolean changed = false;
		for( ActivateRequest ar : dao.getAllRequests(params.getItemPack().getItem()) )
		{
			if( ar.getUser().equals(fromUserId) )
			{
				ar.setUser(toUserId);
				dao.update(ar);
				changed = true;
			}
		}
		return changed;
	}
}
