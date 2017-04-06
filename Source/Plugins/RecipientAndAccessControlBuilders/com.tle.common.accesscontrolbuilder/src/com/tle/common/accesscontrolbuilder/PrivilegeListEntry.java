package com.tle.common.accesscontrolbuilder;

import java.io.Serializable;

import com.tle.common.security.SecurityConstants;

/**
 * @author Nicholas Read
 */
public class PrivilegeListEntry implements Serializable
{
	private static final long serialVersionUID = 1L;
	private boolean granted;
	private String who;
	private boolean override;

	public PrivilegeListEntry()
	{
		super();
	}

	public PrivilegeListEntry(boolean granted, String who, boolean override)
	{
		this.granted = granted;
		this.who = who;
		this.override = override;
	}

	public PrivilegeListEntry(char grantRevoke, String who, int priority)
	{
		this(grantRevoke == SecurityConstants.GRANT, who, priority > 0);
	}

	public boolean isGranted()
	{
		return granted;
	}

	public void setGranted(boolean granted)
	{
		this.granted = granted;
	}

	public boolean isOverride()
	{
		return override;
	}

	public void setOverride(boolean override)
	{
		this.override = override;
	}

	public String getWho()
	{
		return who;
	}

	public void setWho(String who)
	{
		this.who = who;
	}
}
