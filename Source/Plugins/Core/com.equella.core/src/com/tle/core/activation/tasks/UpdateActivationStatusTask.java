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

package com.tle.core.activation.tasks;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.workflow.ActivationStatusOperation;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.filter.AbstractStandardOperationFilter;
import com.tle.core.scheduler.ScheduledTask;

@Bind
@Singleton
public class UpdateActivationStatusTask implements ScheduledTask
{
	@Inject
	private ItemService itemService;
	@Inject
	private Provider<ActivationStatusFilter> filterFactory;

	@Override
	public void execute()
	{
		itemService.operateAll(filterFactory.get());
	}

	@Bind
	public static class ActivationStatusFilter extends AbstractStandardOperationFilter
	{
		@Inject
		private ActivationService activationService;
		@Inject
		private Provider<ActivationStatusOperation> opFactory;

		@Override
		public WorkflowOperation[] createOperations()
		{
			return new WorkflowOperation[]{opFactory.get(), operationFactory.reindexOnly(false)};
		}

		@Override
		public FilterResults getItemIds()
		{
			return new FilterResults(activationService.getAllActivatedItemsForInstitution());
		}
	}
}
