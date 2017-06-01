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
