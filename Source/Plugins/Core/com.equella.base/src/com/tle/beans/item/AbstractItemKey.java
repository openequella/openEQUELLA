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

package com.tle.beans.item;

import com.tle.annotation.NonNullByDefault;

@NonNullByDefault
public abstract class AbstractItemKey implements ItemKey
{
	protected String uuid;
	protected int version;

	public AbstractItemKey(String uuid, int version)
	{
		this.uuid = uuid;
		this.version = version;
	}

	public AbstractItemKey()
	{
		// for parsers
	}

	@Override
	public String getUuid()
	{
		return uuid;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	@Override
	public final String toString()
	{
		return toString(version);
	}

	@Override
	public String toString(int version)
	{
		return uuid + '/' + version;
	}

	@Override
	public int hashCode()
	{
		return uuid.hashCode() ^ version;
	}

	@SuppressWarnings("nls")
	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( obj == null )
		{
			return false;
		}
		if( obj.getClass() != getClass() )
		{
			throw new RuntimeException("Should not be comparing different ItemKey classes");
		}
		AbstractItemKey rhs = (AbstractItemKey) obj;
		return uuid.equals(rhs.uuid) && version == rhs.version;
	}

	public boolean isDRMApplicable()
	{
		return true;
	}

}
