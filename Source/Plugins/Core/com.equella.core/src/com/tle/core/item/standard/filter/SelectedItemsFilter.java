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

import java.util.Collection;
import java.util.Date;

import com.tle.beans.item.ItemId;
import com.tle.core.item.operations.ItemOperationFilter;
import com.tle.core.item.operations.WorkflowOperation;

public class SelectedItemsFilter implements ItemOperationFilter
{
	private final WorkflowOperation[] operations;
	private final Collection<? extends ItemId> keys;

	public SelectedItemsFilter(Collection<? extends ItemId> keys, WorkflowOperation... operations)
	{
		this.keys = keys;
		this.operations = operations;
	}

	@Override
	public WorkflowOperation[] getOperations()
	{
		return operations;
	}

	@Override
	public FilterResults getItemIds()
	{
		return new FilterResults(keys);
	}

	@Override
	public void setDateNow(Date now)
	{
		// nothing
	}

	@Override
	public boolean isReadOnly()
	{
		return false;
	}
}
