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
