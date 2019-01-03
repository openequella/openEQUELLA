/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.scripting.types;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import com.tle.beans.item.Item;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.common.scripting.types.SearchResultsScriptType;
import com.tle.common.searching.SearchResults;
import com.tle.web.scripting.ScriptTypeFactory;

import javax.inject.Inject;
import java.util.List;

public class SearchResultsScriptTypeImpl implements SearchResultsScriptType
{
	private static final long serialVersionUID = 1L;

	@Inject
	private ScriptTypeFactory scriptTypeFactory;

	private final SearchResults<Item> results;

	// lazy
	private List<ItemScriptType> resultList;

	@Inject
	protected SearchResultsScriptTypeImpl(@Assisted("results") SearchResults<Item> results)
	{
		this.results = results;
	}

	@Override
	public int available()
	{
		return results.getAvailable();
	}

	@Override
	public List<ItemScriptType> getResults()
	{
		if( resultList == null )
		{
			resultList = Lists.transform(results.getResults(), new Function<Item, ItemScriptType>()
			{
				@Override
				public ItemScriptType apply(Item item)
				{
					return scriptTypeFactory.createItem(item);
				}
			});
		}
		return resultList;
	}
}
