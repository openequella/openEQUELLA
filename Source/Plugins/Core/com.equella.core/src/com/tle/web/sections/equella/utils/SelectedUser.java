/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.equella.utils;

import java.util.Objects;

public class SelectedUser
{
	protected String uuid;
	// for caching purposes
	protected String displayName;

	public SelectedUser()
	{
	}

	public SelectedUser(String str)
	{
		int pipeIndex = str.indexOf('|');

		if( pipeIndex >= 0 )
		{
			uuid = str.substring(0, pipeIndex);
			displayName = str.substring(pipeIndex + 1);
		}
		else
		{
			uuid = str;
			displayName = ""; //$NON-NLS-1$
		}
	}

	public SelectedUser(String uuid, String displayName)
	{
		this.uuid = uuid;
		this.displayName = displayName;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	@Override
	public String toString()
	{
		return uuid + '|' + displayName;
	}

	@Override
	public boolean equals(Object other)
	{
		if( other instanceof SelectedUser )
		{
			final SelectedUser otherUser = (SelectedUser) other;
			return Objects.equals(uuid, otherUser.uuid);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		if( uuid != null )
		{
			return uuid.hashCode();
		}
		return 0;
	}
}