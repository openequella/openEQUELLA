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

package com.tle.freetext;

import com.dytech.edge.common.valuebean.ItemIndexDate;

public class FullIdKey
{
	private final long id;
	private final long instId;

	public FullIdKey(ItemIndexDate dkey)
	{
		this.id = dkey.getKey().getKey();
		this.instId = dkey.getInstitutionId();
	}

	public FullIdKey(long id, long instId)
	{
		this.id = id;
		this.instId = instId;
	}

	@Override
	public int hashCode()
	{
		return (int) ((id ^ (id >>> 32)) ^ (instId ^ (instId >>> 32)));
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof FullIdKey) )
		{
			return false;
		}

		FullIdKey other = (FullIdKey) obj;
		return other.id == id && other.instId == instId;
	}
}