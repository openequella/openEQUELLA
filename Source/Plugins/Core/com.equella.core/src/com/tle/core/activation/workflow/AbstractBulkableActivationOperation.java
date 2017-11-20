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

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.ItemActivationId;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;

public abstract class AbstractBulkableActivationOperation extends AbstractStandardWorkflowOperation
{
	private final long activationId;

	@Inject
	private ActivateRequestDao dao;

	public AbstractBulkableActivationOperation(long activationId)
	{
		this.activationId = activationId;
	}

	public AbstractBulkableActivationOperation()
	{
		this(0);
	}

	@Override
	public boolean execute()
	{
		long actId = getActivationId();
		ActivateRequest request = dao.findById(actId);
		return doOperation(request, dao);
	}

	protected abstract boolean doOperation(ActivateRequest request, ActivateRequestDao dao);

	public long getActivationId()
	{
		if( activationId != 0 )
		{
			return activationId;
		}
		else
		{
			return Long.valueOf(params.getAttributes().get(ItemActivationId.PARAM_KEY));
		}
	}
}
