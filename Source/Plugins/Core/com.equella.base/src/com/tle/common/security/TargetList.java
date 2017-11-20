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
import java.util.List;
import java.util.Objects;

import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;

/**
 * @author Nicholas Read
 */
public class TargetList implements Serializable, FieldEquality<TargetList>
{
	private static final long serialVersionUID = 1L;

	private List<TargetListEntry> entries;
	private boolean partial;

	public TargetList()
	{
		// nothing
	}

	public TargetList(TargetListEntry... entries)
	{
		this.entries = Arrays.asList(entries);
	}

	public TargetList(List<TargetListEntry> entries)
	{
		this.entries = entries;
	}

	public List<TargetListEntry> getEntries()
	{
		return entries;
	}

	public void setEntries(List<TargetListEntry> entries)
	{
		this.entries = entries;
	}

	@Override
	public int hashCode()
	{
		return Check.getHashCode(entries, partial);
	}

	@Override
	public boolean equals(Object obj)
	{
		return Check.commonEquals(this, obj);
	}

	@Override
	public boolean checkFields(TargetList rhs)
	{
		return Objects.equals(entries, rhs.entries) && partial == rhs.partial;
	}

	public boolean isPartial()
	{
		return partial;
	}

	public void setPartial(boolean partial)
	{
		this.partial = partial;
	}
}
