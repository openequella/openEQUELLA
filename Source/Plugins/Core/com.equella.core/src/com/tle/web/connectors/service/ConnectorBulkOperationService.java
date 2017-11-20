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

import java.util.Collection;

import com.tle.beans.item.ItemKey;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.services.impl.ClusteredTask;
import com.tle.web.bulk.operation.BulkOperationExecutor;

public interface ConnectorBulkOperationService
{
	@SuppressWarnings("nls")
	String KEY_OPERATION_TITLE = "operationTitle";

	ClusteredTask createTask(Collection<? extends ItemKey> items, BeanLocator<? extends BulkOperationExecutor> executor);
}
