package com.tle.core.freetext.queries;

import java.util.Objects;

import com.dytech.edge.queries.FreeTextQuery;

/**
 * @author jmaginnis
 */
public class FreeTextFieldExistsQuery extends FreeTextQuery
{
	private static final long serialVersionUID = 1L;

	private String field;

	public FreeTextFieldExistsQuery(String field)
	{
		this.field = getRealField(field);
	}

	public String getField()
	{
		return field;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(field);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null || !(obj instanceof FreeTextFieldExistsQuery) )
		{
			return false;
		}
		else if( this == obj )
		{
			return true;
		}
		else
		{
			FreeTextFieldExistsQuery rhs = (FreeTextFieldExistsQuery) obj;
			return Objects.equals(field, rhs.field);
		}
	}
}
