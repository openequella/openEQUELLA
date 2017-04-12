/*
 * Created on Jun 29, 2005
 */
package com.tle.core.freetext.queries;

import java.util.Objects;

import com.dytech.edge.queries.FreeTextQuery;

/**
 * @author jmaginnis
 */
public class FreeTextFieldQuery extends FreeTextQuery
{
	private static final long serialVersionUID = 1L;

	private String field;
	private String value;
	private boolean tokenise;
	private boolean possibleWildcard;
	private boolean mustExist;

	public FreeTextFieldQuery(String field, String value)
	{
		this.field = getRealField(field);
		this.value = value;
	}

	public FreeTextFieldQuery(String field, String value, boolean mustExist)
	{
		this(field, value);
		this.mustExist = mustExist;
	}

	public String getField()
	{
		return field;
	}

	public String getValue()
	{
		return value;
	}

	public void setTokenise(boolean tokenise)
	{
		this.tokenise = tokenise;
	}

	public boolean isTokenise()
	{
		return tokenise;
	}

	public boolean isMustExist()
	{
		return mustExist;
	}

	public void setMustExist(boolean mustExist)
	{
		this.mustExist = mustExist;
	}

	public boolean isPossibleWildcard()
	{
		return possibleWildcard;
	}

	public void setPossibleWildcard(boolean possibleWildcard)
	{
		this.possibleWildcard = possibleWildcard;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(field, value, tokenise, possibleWildcard, mustExist);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null || !(obj instanceof FreeTextFieldQuery) )
		{
			return false;
		}
		else if( this == obj )
		{
			return true;
		}
		else
		{
			FreeTextFieldQuery rhs = (FreeTextFieldQuery) obj;
			return tokenise == rhs.tokenise && possibleWildcard == rhs.possibleWildcard && mustExist == rhs.mustExist
				&& Objects.equals(field, rhs.field) && Objects.equals(value, rhs.value);
		}
	}
}
