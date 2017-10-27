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

import javax.inject.Inject;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.filter.AbstractStandardOperationFilter;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;
import com.tle.core.item.standard.operations.workflow.EscalateOperation;

@Bind
public class CheckModerationFilter extends AbstractStandardOperationFilter
{
	@Inject
	private Provider<EscalateOperation> escalateFactory;
	@Inject
	private ItemOperationFactory itemOperationFactory;

	@Override
	public AbstractStandardWorkflowOperation[] createOperations()
	{
		return new AbstractStandardWorkflowOperation[]{escalateFactory.get(), itemOperationFactory.checkSteps(),
				operationFactory.save()};
	}

	@Override
	public String getWhereClause()
	{
		return "moderating = true"; //$NON-NLS-1$
	}
}
