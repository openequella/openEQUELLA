package com.tle.core.security.impl;

import java.util.HashSet;
import java.util.Set;

public class SecurityAttribute
{
	enum OnCallMode
	{
		DOMAIN, TOPLEVEL, ANY
	}

	private Set<String> onCallPrivileges;
	private Set<String> filterPrivileges;
	private OnCallMode onCallmode;
	private boolean systemOnly;
	private boolean filterMatching;
	private int domainArg;

	public boolean isSystemOnly()
	{
		return systemOnly;
	}

	public void setSystemOnly(boolean systemOnly)
	{
		this.systemOnly = systemOnly;
	}

	public boolean isFilterMatching()
	{
		return filterMatching;
	}

	public void setFilterMatching(boolean filterMatching)
	{
		this.filterMatching = filterMatching;
	}

	public int getDomainArg()
	{
		return domainArg;
	}

	public void setDomainArg(int domainArg)
	{
		this.domainArg = domainArg;
	}

	public Set<String> getOnCallPrivileges()
	{
		return onCallPrivileges;
	}

	public Set<String> getFilterPrivileges()
	{
		return filterPrivileges;
	}

	public void addOnCallPrivilege(String priv)
	{
		if( onCallPrivileges == null )
		{
			onCallPrivileges = new HashSet<String>();
		}
		onCallPrivileges.add(priv);
	}

	public void addFilterPrivilege(String priv)
	{
		if( filterPrivileges == null )
		{
			filterPrivileges = new HashSet<String>();
		}
		filterPrivileges.add(priv);
	}

	public OnCallMode getOnCallmode()
	{
		return onCallmode;
	}

	public void setOnCallmode(OnCallMode onCallmode)
	{
		this.onCallmode = onCallmode;
	}
}
