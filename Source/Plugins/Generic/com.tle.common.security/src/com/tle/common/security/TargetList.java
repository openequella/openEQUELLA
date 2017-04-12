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
