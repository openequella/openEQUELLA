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

package com.tle.web.connectors.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.common.connectors.service.ConnectorItemKey;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.guice.Bind;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.item.operations.FilterResultListener;
import com.tle.core.item.operations.ItemOperationFilter;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.ClusteredTask;
import com.tle.core.services.impl.SingleShotTask;
import com.tle.core.services.impl.Task;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkResult;

@Bind(ConnectorBulkOperationService.class)
@Singleton
public class ConnectorBulkOperationServiceImpl implements ConnectorBulkOperationService
{
	@Inject
	private ItemService itemService;
	@Inject
	private RunAsInstitution runAs;

	@SuppressWarnings("nls")
	@Override
	public ClusteredTask createTask(Collection<? extends ItemKey> items,
		BeanLocator<? extends BulkOperationExecutor> executor)
	{
		BeanClusteredTask clusteredTask = new BeanClusteredTask(null, ConnectorBulkOperationService.class,
			"createNewTask", CurrentUser.getUserState(), (Serializable) items, executor);
		return clusteredTask;
	}

	public Task createNewTask(UserState userState, Collection<ItemIdKey> items,
		BeanLocator<? extends BulkOperationExecutor> executor)
	{
		return new ConnectorBulkOperationTask(userState, items, executor.get());
	}

	public class ConnectorBulkOperationTask extends SingleShotTask
	{
		private final UserState userState;
		private final Collection<? extends ItemKey> items;
		private final BulkOperationExecutor executor;

		public ConnectorBulkOperationTask(UserState userState, Collection<? extends ItemKey> items,
			BulkOperationExecutor executor)
		{
			this.userState = userState;
			this.items = items;
			this.executor = executor;
		}

		@Override
		protected String getTitleKey()
		{
			return null;
		}

		@Override
		public void runTask() throws Exception
		{
			final ConnectorBulkWorkflowFilter filter = new ConnectorBulkWorkflowFilter(this, items, executor);
			runAs.execute(userState, new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					itemService.operateAll(filter, filter);
					return null;
				}
			});
		}
	}

	public static class ConnectorBulkWorkflowFilter implements ItemOperationFilter, FilterResultListener
	{
		private final Collection<? extends ItemKey> items;
		private final BulkOperationExecutor executor;
		private final Task task;

		public ConnectorBulkWorkflowFilter(Task task, Collection<? extends ItemKey> items,
			BulkOperationExecutor executor)
		{
			this.task = task;
			this.items = items;
			this.executor = executor;
		}

		@Override
		public FilterResults getItemIds()
		{
			return new FilterResults(items);
		}

		@Override
		public WorkflowOperation[] getOperations()
		{
			return executor.getOperations();
		}

		@Override
		public boolean isReadOnly()
		{
			return false;
		}

		@Override
		public void succeeded(ItemKey itemId, ItemPack pack)
		{
			ConnectorItemKey itemKey = (ConnectorItemKey) itemId;
			task.addLogEntry(new BulkResult(true, itemKey.getTitle(), null));
		}

		@Override
		public void failed(ItemKey itemId, Item item, ItemPack pack, Throwable e)
		{
			ConnectorItemKey itemKey = (ConnectorItemKey) itemId;
			task.addLogEntry(new BulkResult(false, itemKey.getTitle(), e.getMessage()));

		}

		@Override
		public void setDateNow(Date now)
		{
			// nothing
		}

		@Override
		public void total(int total)
		{
			// Nothing
		}
	}
}
