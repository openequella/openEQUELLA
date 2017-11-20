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

package com.tle.core.cloud.service;

import java.util.List;

import com.tle.common.searching.SimpleSearchResults;
import com.tle.core.cloud.beans.converted.CloudItem;

/**
 * @author Aaron
 */
public class CloudSearchResults extends SimpleSearchResults<CloudItem>
{
	private final int filteredOut;

	public CloudSearchResults(List<CloudItem> results, int count, int offset, int available, int filteredOut)
	{
		super(results, count, offset, available);
		this.filteredOut = filteredOut;
	}

	public int getFilteredOut()
	{
		return filteredOut;
	}
}
