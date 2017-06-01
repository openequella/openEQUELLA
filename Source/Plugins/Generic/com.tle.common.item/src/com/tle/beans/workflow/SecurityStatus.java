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

package com.tle.beans.workflow;

import java.io.Serializable;
import java.util.Set;

import com.tle.beans.item.ItemLock;

public class SecurityStatus implements Serializable
{
	private static final long serialVersionUID = 1L;

	private ItemLock lock;
	private Set<String> allowedPrivileges;

	public Set<String> getAllowedPrivileges()
	{
		return allowedPrivileges;
	}

	public void setAllowedPrivileges(Set<String> allowedPrivileges)
	{
		this.allowedPrivileges = allowedPrivileges;
	}

	public boolean isLocked()
	{
		return lock != null;
	}

	public ItemLock getLock()
	{
		return lock;
	}

	public void setLock(ItemLock lock)
	{
		this.lock = lock;
	}

	public String getLockedBy()
	{
		if( lock != null )
		{
			return lock.getUserID();
		}
		return null;
	}
}
