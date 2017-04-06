package com.tle.exceptions;

import java.util.Arrays;
import java.util.Collection;

public class PrivilegeRequiredException extends AccessDeniedException
{
	private static final long serialVersionUID = 1L;

	public PrivilegeRequiredException(String priv)
	{
		super("Privilege " + priv + " is required to perform the requested operation");
	}

	public PrivilegeRequiredException(Collection<String> privs)
	{
		super("One of [" + privList(privs) + "] is required to perform requested operation");
	}

	public PrivilegeRequiredException(String... privs)
	{
		this(Arrays.asList(privs));
	}

	private static String privList(Collection<String> privs)
	{
		StringBuilder sbuf = new StringBuilder();
		boolean first = true;
		for( String priv : privs )
		{
			if( !first )
			{
				sbuf.append(", "); //$NON-NLS-1$
			}
			else
			{
				first = false;
			}
			sbuf.append(priv);
		}
		return sbuf.toString();
	}
}
