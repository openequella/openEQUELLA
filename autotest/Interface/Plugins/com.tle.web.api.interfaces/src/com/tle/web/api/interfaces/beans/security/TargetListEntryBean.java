package com.tle.web.api.interfaces.beans.security;

public class TargetListEntryBean
{
	private boolean granted;
	private boolean override;
	private String privilege;
	private String who;

	public boolean isGranted()
	{
		return granted;
	}

	public TargetListEntryBean setGranted(boolean granted)
	{
		this.granted = granted;
		return this;
	}

	public boolean isOverride()
	{
		return override;
	}

	public TargetListEntryBean setOverride(boolean override)
	{
		this.override = override;
		return this;
	}

	public String getPrivilege()
	{
		return privilege;
	}

	public TargetListEntryBean setPrivilege(String privilege)
	{
		this.privilege = privilege;
		return this;
	}

	public String getWho()
	{
		return who;
	}

	public TargetListEntryBean setWho(String who)
	{
		this.who = who;
		return this;
	}
}
