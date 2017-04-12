package com.tle.web.remoting.rest.service;

import javax.ws.rs.core.UriInfo;

import com.google.common.base.Strings;
import com.tle.core.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;

/**
 * @author Aaron
 */
public abstract class RestImportHelper
{
	private RestImportHelper()
	{
		throw new Error();
	}

	public static boolean isImport(UriInfo uriInfo) throws AccessDeniedException
	{
		final String importParam = uriInfo.getQueryParameters().getFirst("import");
		final boolean importing = (!Strings.isNullOrEmpty(importParam) && Boolean.parseBoolean(importParam));
		if( importing )
		{
			if( !CurrentUser.getUserState().isSystem() )
			{
				throw new AccessDeniedException("Must be system user to import");
			}
		}
		return importing;
	}
}
