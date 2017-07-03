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

import java.util.Date;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.activation.ActivateRequest;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author Aaron
 */
@SecureOnCall(priv = "DEACTIVATE_ACTIVATION_ITEM")
public class DeactivateOperation extends AbstractBulkableActivationOperation
{
	@AssistedInject
	public DeactivateOperation()
	{
		super();
	}

	@AssistedInject
	public DeactivateOperation(@Assisted long id)
	{
		super(id);
	}

	@Override
	protected boolean doOperation(ActivateRequest request, ActivateRequestDao dao)
	{
		Date now = params.getDateNow();

		if( request.getUntil().after(now) && request.getStatus() == ActivateRequest.TYPE_ACTIVE )
		{
			Date cancellationDate = (Date) now.clone();

			// Just before 'from'/'now': Don't want crossover!
			cancellationDate.setTime(cancellationDate.getTime() - 1);

			request.setStatus(ActivateRequest.TYPE_INACTIVE);
			request.setUntil(cancellationDate);
			dao.update(request);
		}
		return true;
	}
}
