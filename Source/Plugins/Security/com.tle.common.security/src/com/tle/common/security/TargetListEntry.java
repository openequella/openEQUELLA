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

package com.tle.common.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class TargetListEntry implements Serializable, FieldEquality<TargetListEntry>
{
	private static final long serialVersionUID = 1L;

	private boolean granted;
	private boolean override;
	private String privilege;
	private String who;
	private String postfix = "";

	public TargetListEntry()
	{
		super();
	}

	public TargetListEntry(char grantRevoke, int priority, String privilege, String who)
	{
		this(grantRevoke == SecurityConstants.GRANT, priority > 0, privilege, who, "");
	}

	public TargetListEntry(boolean granted, boolean override, String privilege, String who)
	{
		this(granted, override, privilege, who, ""); //$NON-NLS-1$
	}

	public TargetListEntry(boolean granted, boolean override, String privilege, String who, String postfix)
	{
		this.granted = granted;
		this.override = override;
		this.privilege = privilege;
		this.who = who;
		this.postfix = postfix;
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

	public String getPrivilege()
	{
		return privilege;
	}

	public void setPrivilege(String privilege)
	{
		this.privilege = privilege;
	}

	public String getWho()
	{
		return who;
	}

	public void setWho(String who)
	{
		this.who = who;
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode(new Object[]{granted, override, privilege, who, postfix});
	}

	@Override
	public boolean equals(Object obj)
	{
		return Check.commonEquals(this, obj);
	}

	@Override
	public boolean checkFields(TargetListEntry rhs)
	{
		return granted == rhs.granted && override == rhs.override && Objects.equals(privilege, rhs.privilege)
			&& Objects.equals(who, rhs.who) && Objects.equals(postfix, rhs.postfix);
	}

	public String getPostfix()
	{
		return postfix;
	}

	public void setPostfix(String postfix)
	{
		this.postfix = postfix;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder(privilege);
		sb.append(" ");
		sb.append(granted ? "G:" : "R:");
		sb.append(who);
		sb.append(" ");
		sb.append(postfix);
		if( override )
		{
			sb.append(" (override)");
		}
		return sb.toString();
	}
}
