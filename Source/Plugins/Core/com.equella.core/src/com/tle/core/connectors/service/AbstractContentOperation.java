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

package com.tle.core.connectors.service;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.core.item.operations.ItemOperationParams;
import com.tle.core.item.operations.WorkflowOperation;

public abstract class AbstractContentOperation implements WorkflowOperation
{
	@Inject
	private ConnectorRepositoryService repositoryService;

	protected ItemOperationParams params;

	@Override
	public Item getItem()
	{
		return null;
	}

	@Override
	public ItemPack getItemPack()
	{
		return null;
	}

	@Override
	public boolean isReadOnly()
	{
		return true;
	}

	@Override
	public void setParams(ItemOperationParams params)
	{
		this.params = params;
	}

	@Override
	public boolean failedToAutowire()
	{
		return repositoryService == null;
	}

	@Override
	public boolean isDeleteLike()
	{
		return false;
	}
}
