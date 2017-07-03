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

package com.tle.web.bulk.operation;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.common.i18n.CurrentLocale;
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

import it.uniroma3.mat.extendedset.wrappers.LongSet;

@Singleton
@Bind(BulkOperationService.class)
@SuppressWarnings("nls")
public class BulkOperationServiceImpl implements BulkOperationService
{
	@Inject
	private ItemService itemService;
	@Inject
	private RunAsInstitution runAs;

	@Override
	public ClusteredTask createTask(Collection<? extends ItemKey> items,
		BeanLocator<? extends BulkOperationExecutor> executor)
	{
		BeanClusteredTask clusteredTask = new BeanClusteredTask(true, null, BulkOperationService.class, "createNewTask",
			CurrentUser.getUserState(), (Serializable) items, executor);
		return clusteredTask;
	}

	@Override
	public ClusteredTask createTask(LongSet items, BeanLocator<? extends BulkOperationExecutor> executor)
	{
		BeanClusteredTask clusteredTask = new BeanClusteredTask(true, null, BulkOperationService.class, "createNewTask",
			CurrentUser.getUserState(), items, executor);
		return clusteredTask;
	}

	public Task createNewTask(UserState userState, Collection<? extends ItemId> items,
		BeanLocator<? extends BulkOperationExecutor> executor)
	{
		return new BulkOperationTask(userState, items, executor.get());
	}

	public Task createNewTask(UserState userState, LongSet items, BeanLocator<? extends BulkOperationExecutor> executor)
	{
		return new BulkOperationTask(userState, items, executor.get());
	}

	public class BulkOperationTask extends SingleShotTask
	{
		private final UserState userState;
		private final Collection<? extends ItemId> items;
		private final LongSet itemsBitSet;
		private final BulkOperationExecutor executor;

		public BulkOperationTask(UserState userState, Collection<? extends ItemId> items,
			BulkOperationExecutor executor)
		{
			this.userState = userState;
			this.items = items;
			this.executor = executor;
			this.itemsBitSet = null;
		}

		public BulkOperationTask(UserState userState, LongSet itemsBitSet, BulkOperationExecutor executor)
		{
			this.userState = userState;
			this.itemsBitSet = itemsBitSet;
			this.executor = executor;
			this.items = null;
		}

		@Override
		protected String getTitleKey()
		{
			if( executor == null )
			{
				return null;
			}
			return executor.getTitleKey();
		}

		@Override
		public Priority getPriority()
		{
			return Priority.INTERACTIVE;
		}

		@Override
		public void runTask() throws Exception
		{
			final BulkWorkflowFilter filter;
			if( items != null )
			{
				filter = new BulkWorkflowFilter(this, items, executor);
			}
			else
			{
				filter = new BulkWorkflowFilter(this, itemsBitSet, executor);

			}
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

	public class BulkWorkflowFilter implements ItemOperationFilter, FilterResultListener
	{
		private final Collection<? extends ItemId> items;
		private final LongSet itemsBitSet;
		private final BulkOperationExecutor executor;
		private final Task task;

		public BulkWorkflowFilter(Task task, Collection<? extends ItemId> items, BulkOperationExecutor executor)
		{
			this.task = task;
			this.items = items;
			this.executor = executor;
			this.itemsBitSet = null;
		}

		public BulkWorkflowFilter(Task task, LongSet itemsBitSet, BulkOperationExecutor executor)
		{
			this.task = task;
			this.items = null;
			this.itemsBitSet = itemsBitSet;
			this.executor = executor;
		}

		@Override
		public FilterResults getItemIds()
		{
			if( items != null )
			{
				return new FilterResults(items);
			}
			else
			{
				return new FilterResults(itemsBitSet.size(),
					Iterators.transform(itemsBitSet.iterator(), new Function<Long, ItemKey>()
					{
						@Override
						public ItemKey apply(Long id)
						{
							return itemService.getItemIdKey(id);
						}
					}));
			}
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

		private String getResultName(Item item, ItemPack<Item> pack)
		{
			if( pack != null && pack.getAttribute(KEY_ITEM_RESULT_TITLE) != null )
			{
				return pack.getAttribute(KEY_ITEM_RESULT_TITLE);
			}
			else
			{
				return CurrentLocale.get(item.getName(), item.getUuid());
			}
		}

		@Override
		public void succeeded(ItemKey itemId, ItemPack<Item> pack)
		{
			Item item = pack.getItem();
			task.addLogEntry(new BulkResult(true, getResultName(item, pack), null));
		}

		@Override
		public void failed(ItemKey itemId, Item item, ItemPack<Item> pack, Throwable e)
		{
			task.addLogEntry(new BulkResult(false, getResultName(item, pack), e.getMessage()));
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
