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
