/*
 * Created on Oct 26, 2005
 */
package com.tle.common;

import java.io.Serializable;

public class Pair<FIRST, SECOND> implements Serializable
{
	private static final long serialVersionUID = 1;

	public static <U, V> Pair<U, V> pair(U first, V second)
	{
		return new Pair<U, V>(first, second);
	}

	private FIRST first;
	private SECOND second;

	public Pair()
	{
		super();
	}

	public Pair(FIRST first, SECOND second)
	{
		this.first = first;
		this.second = second;
	}

	public FIRST getFirst()
	{
		return first;
	}

	public void setFirst(FIRST first)
	{
		this.first = first;
	}

	public SECOND getSecond()
	{
		return second;
	}

	public void setSecond(SECOND second)
	{
		this.second = second;
	}

	@Override
	public String toString()
	{
		return first == null ? "" : first.toString(); //$NON-NLS-1$
	}

	@Override
	public int hashCode()
	{
		return first.hashCode() + second.hashCode();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			// Reflexitivity
			return true;
		}
		else if( obj == null )
		{
			// Non-null
			return false;
		}
		else if( this.getClass() != obj.getClass() )
		{
			// Symmetry
			return false;
		}
		else
		{
			return checkFields((Pair) obj);
		}
	}

	public boolean checkFields(Pair<FIRST, SECOND> rhs)
	{
		return Check.bothNullOrEqual(rhs.getFirst(), getFirst()) && Check.bothNullOrEqual(rhs.getSecond(), getSecond());
	}
}
