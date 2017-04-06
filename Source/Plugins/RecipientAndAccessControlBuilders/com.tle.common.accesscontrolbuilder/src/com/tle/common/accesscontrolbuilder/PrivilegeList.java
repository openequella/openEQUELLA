package com.tle.common.accesscontrolbuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicholas Read
 */
public class PrivilegeList implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String privilege;
	private List<PrivilegeListEntry> entries;

	public PrivilegeList()
	{
		entries = new ArrayList<PrivilegeListEntry>();
	}

	public PrivilegeList(String privilege)
	{
		this();
		this.privilege = privilege;
	}

	public List<PrivilegeListEntry> getEntries()
	{
		return entries;
	}

	public void setEntries(List<PrivilegeListEntry> entries)
	{
		this.entries = entries;
	}

	public String getPrivilege()
	{
		return privilege;
	}

	public void setPrivilege(String privilege)
	{
		this.privilege = privilege;
	}
}
