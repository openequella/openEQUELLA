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
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemStatus;
import com.tle.core.item.standard.filter.AbstractStandardOperationFilter;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
public class ArchiveOldFilter extends AbstractStandardOperationFilter
{
	private final ItemKey key;

	@AssistedInject
	protected ArchiveOldFilter(@Assisted ItemKey key)
	{
		this.key = key;
	}

	@Override
	public AbstractStandardWorkflowOperation[] createOperations()
	{
		return new AbstractStandardWorkflowOperation[]{operationFactory.insecureArchive(), operationFactory.save()};
	}

	@Override
	public void queryValues(Map<String, Object> values)
	{
		values.put("uuid", key.getUuid());
		values.put("status", ItemStatus.LIVE.name());
		values.put("version", key.getVersion());
	}

	@Override
	public String getWhereClause()
	{
		return "uuid = :uuid and status = :status and version < :version";
	}
}
