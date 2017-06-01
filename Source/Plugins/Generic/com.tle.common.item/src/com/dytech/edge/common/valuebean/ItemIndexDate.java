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

package com.dytech.edge.common.valuebean;

import java.util.Date;

import com.tle.beans.item.ItemIdKey;

public class ItemIndexDate
{
	private final ItemIdKey key;
	private final Date lastIndexed;
	private final long institutionId;

	public ItemIndexDate(long id, String uuid, int version, Date lastIndexed, long institution)
	{
		this(new ItemIdKey(id, uuid, version), lastIndexed, institution);
	}

	public ItemIndexDate(ItemIdKey key, Date lastIndexed, long institutionId)
	{
		this.key = key;
		this.lastIndexed = lastIndexed;
		this.institutionId = institutionId;
	}

	public ItemIdKey getKey()
	{
		return key;
	}

	public Date getLastIndexed()
	{
		return lastIndexed;
	}

	public long getInstitutionId()
	{
		return institutionId;
	}

}
