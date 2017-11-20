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

package com.tle.core.item.standard.filter.workflow;

import java.util.Map;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.filter.AbstractStandardOperationFilter;

/**
 * @author jmaginnis
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SuppressWarnings("nls")
public final class WorkflowChangeFilter extends AbstractStandardOperationFilter // NOSONAR
{
	private final long collectionId;

	@AssistedInject
	private WorkflowChangeFilter(@Assisted long collectionId)
	{
		this.collectionId = collectionId;
	}

	@Override
	public WorkflowOperation[] createOperations()
	{
		// Need save operation to reindex items that are changed
		return new WorkflowOperation[]{operationFactory.reset(), operationFactory.saveUnlock(false)};
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		values.put("collectionId", collectionId);
	}

	@Override
	public String getWhereClause()
	{
		return "moderating = true and itemDefinition.id = :collectionId";
	}
}
