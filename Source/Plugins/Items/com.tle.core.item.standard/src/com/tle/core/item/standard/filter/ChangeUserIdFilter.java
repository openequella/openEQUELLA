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

package com.tle.core.item.standard.filter;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.core.item.operations.WorkflowOperation;

/**
 * @author jmaginnis
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SuppressWarnings("nls")
public final class ChangeUserIdFilter extends AbstractUserFilter // NOSONAR
{
	private String toUserId;

	@AssistedInject
	private ChangeUserIdFilter(@Assisted("fromUserId") String fromUserId, @Assisted("toUserId") String toUserId)
	{
		super(fromUserId);
		this.toUserId = toUserId;
	}

	@Override
	public String getWhereClause()
	{
		return super.getWhereClause() + " OR :userId IN ELEMENTS(i.notifications) OR m.rejectedBy = :userId";
	}

	@Override
	public String getJoinClause()
	{
		return " LEFT JOIN i.moderation m";
	}

	@Override
	public WorkflowOperation[] createOperations()
	{
		return new WorkflowOperation[]{operationFactory.changeUserId(getUserID(), toUserId), operationFactory.save()};
	}
}
