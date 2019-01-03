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

package com.tle.web.template.section;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public abstract class AbstractCachedTopbarLink implements TopbarLink
{
	private Cache<String, Integer> countCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
		.softValues().build();

	public int getCachedValue()
	{
		Integer count = countCache.getIfPresent(getSessionKey());
		if( count == null )
		{
			count = getCount();
			countCache.put(getSessionKey(), new Integer(count));
		}
		return count;
	}

	public void clearCachedCount()
	{
		countCache.invalidate(getSessionKey());
	}

	public abstract int getCount();

	public abstract String getSessionKey();
}
