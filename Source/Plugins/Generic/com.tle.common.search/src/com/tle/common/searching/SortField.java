package com.tle.common.searching;

import java.io.Serializable;
import java.util.Objects;

/**
 * The real question is.. Do we just want to expose the lucene classes instead?
 * 
 * @author jolz
 */
public class SortField implements Cloneable, Serializable
{
	private static final long serialVersionUID = 1L;

	public enum Type
	{
		STRING, INT, LONG, SCORE
	}

	private final String field;
	private final Type type;
	private boolean reverse;

	public SortField(String field, boolean reverse)
	{
		this(field, reverse, Type.STRING);
	}

	public SortField(String field, boolean reverse, Type type)
	{
		this.field = field;
		this.reverse = reverse;
		this.type = type;
	}

	public String getField()
	{
		return field;
	}

	public boolean isReverse()
	{
		return reverse;
	}

	public Type getType()
	{
		return type;
	}

	public void setReverse(boolean reverse)
	{
		this.reverse = reverse;
	}

	// Explicit catch of CloneNotSupportedException from super.clone()
	@Override
	public SortField clone() // NOSONAR
	{
		try
		{
			return (SortField) super.clone();
		}
		catch( CloneNotSupportedException e )
		{
			throw new Error(e);
		}
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(field, type, reverse);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null || !(obj instanceof SortField) )
		{
			return false;
		}
		else if( this == obj )
		{
			return true;
		}
		else
		{
			SortField rhs = (SortField) obj;
			return reverse == rhs.reverse && Objects.equals(type, rhs.type) && Objects.equals(field, rhs.field);
		}
	}
}