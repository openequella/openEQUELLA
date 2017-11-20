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

import java.util.Calendar;
import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.ItemStatus;
import com.tle.core.item.operations.WorkflowOperation;

/**
 * @author jmaginnis
 */
public class RemoveDeletedItemsFilter extends AbstractStandardOperationFilter
{
	private final int daysOld;

	@AssistedInject
	protected RemoveDeletedItemsFilter(@Assisted int daysOld)
	{
		this.daysOld = daysOld;
	}

	@Override
	public WorkflowOperation[] createOperations()
	{
		return new WorkflowOperation[]{operationFactory.purge(false)};
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.add(Calendar.DAY_OF_YEAR, -daysOld);
		values.put("dateModified", cal.getTime());
		values.put("status", ItemStatus.DELETED.name());
	}

	@Override
	public String getWhereClause()
	{
		return "status = :status and dateModified <= :dateModified";
	}
}
