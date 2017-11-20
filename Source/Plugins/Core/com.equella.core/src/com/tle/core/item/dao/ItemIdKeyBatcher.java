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

package com.tle.core.item.dao;

import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNull;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.dao.helpers.BatchingIterator;

/**
 * Iterates over possibly enormous numbers of ItemIdKey results by doing
 * batching behind the scenes.
 * 
 * @author Nick Read
 */
public abstract class ItemIdKeyBatcher extends BatchingIterator<ItemIdKey>
{
	private static final int BATCH_SIZE = 100;

	private final ItemDao dao;

	public ItemIdKeyBatcher(@NonNull ItemDao dao)
	{
		this.dao = dao;
	}

	public long getTotalCount()
	{
		return dao.getCount(joinClause(), wrapWhereClause(), getParams());
	}

	@Override
	protected Iterator<ItemIdKey> getMore(Optional<ItemIdKey> lastObj)
	{
		long startId = lastObj.isPresent() ? lastObj.get().getKey() + 1 : 1;
		return dao.getItemKeyBatch(joinClause(), wrapWhereClause(), getParams(), startId, BATCH_SIZE).iterator();
	}

	@SuppressWarnings("nls")
	private String wrapWhereClause()
	{
		return "(" + whereClause() + ") AND i.institution = :institution";
	}

	@SuppressWarnings("nls")
	private Map<String, Object> getParams()
	{
		Map<String, Object> params = Maps.newHashMap();
		params.put("institution", CurrentInstitution.get());
		addParameters(params);
		return params;
	}

	protected abstract String joinClause();

	protected abstract String whereClause();

	protected abstract void addParameters(Map<String, Object> params);
}
