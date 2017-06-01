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

package com.tle.core.harvester.old;

import java.util.Date;

/**
 * @author Nicholas Read
 */
public class TLEItem
{
	private String uuid;
	private int version;
	private Date created;
	private Date modified;

	public TLEItem(String uuid, int version, Date created, Date modified)
	{
		this.uuid = uuid;
		this.version = version;
		this.created = created;
		this.modified = modified;

	}

	public Date getCreationDate()
	{
		return created;
	}

	public String getUuid()
	{
		return uuid;
	}

	public int getVersion()
	{
		return version;
	}

	public Date getModifiedDate()
	{
		return modified;
	}
}
