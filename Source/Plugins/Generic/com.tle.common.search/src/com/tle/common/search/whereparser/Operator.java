package com.tle.common.search.whereparser;

public final class Operator
{
	// Available Instances
	public static final Operator EQUALS = new Operator("=", false); //$NON-NLS-1$
	public static final Operator NOT_EQUALS = new Operator("!=", true); //$NON-NLS-1$
	public static final Operator LESS_THAN = new Operator("<", false); //$NON-NLS-1$
	public static final Operator LESS_THAN_OR_EQUAL_TO = new Operator("<=", false); //$NON-NLS-1$
	public static final Operator GREATER_THAN = new Operator(">", false); //$NON-NLS-1$
	public static final Operator GREATER_THAN_OR_EQUAL_TO = new Operator(">=", false); //$NON-NLS-1$
	public static final Operator LIKE = new Operator("LIKE", false); //$NON-NLS-1$
	public static final Operator NOT_LIKE = new Operator("NOT LIKE", true); //$NON-NLS-1$
	public static final Operator IN = new Operator("IN", false); //$NON-NLS-1$
	public static final Operator NOT_IN = new Operator("NOT IN", true); //$NON-NLS-1$

	// /// IMPLEMENTATION //////////////////////////////////////////////////////

	private final String type;
	private final boolean not;

	private Operator(String type, boolean not)
	{
		this.type = type;
		this.not = not;
	}

	public boolean isNot()
	{
		return not;
	}

	@Override
	public String toString()
	{
		return type;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}
		if( obj == null )
		{
			return false;
		}
		if( getClass() != obj.getClass() )
		{
			return false;
		}
		final Operator other = (Operator) obj;
		if( not != other.not )
		{
			return false;
		}
		if( type == null )
		{
			if( other.type != null )
			{
				return false;
			}
		}
		else if( !type.equals(other.type) )
		{
			return false;
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (not ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
}
