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

package com.tle.web.remoting.rest.service;

import javax.ws.rs.core.UriInfo;

import com.google.common.base.Strings;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;

/**
 * @author Aaron
 */
public abstract class RestImportExportHelper
{
	private RestImportExportHelper()
	{
		throw new Error();
	}

	public static boolean isImport(UriInfo uriInfo) throws AccessDeniedException
	{
		return verifyAccessIfParamFound(uriInfo, true);
	}

	public static boolean isExport(UriInfo uriInfo) throws AccessDeniedException
	{
		return verifyAccessIfParamFound(uriInfo, false);
	}

	/**
	 * Check the query string to see if the import/export flag is present, and
	 * if so, does it parse as true?
	 * 
	 * @param uriInfo
	 * @param isImportOp Are we looking for an import operation, or export?
	 * @return true if sought for directive is both present, and parses as true.
	 *         Otherwise false.
	 * @throws AccessDeniedException if directive is present and true, but user
	 *             does not have System credentials.
	 */
	private static boolean verifyAccessIfParamFound(UriInfo uriInfo, boolean isImportOp) throws AccessDeniedException
	{
		String directiveParam = isImportOp ? "import" : "export";
		final String opParam = uriInfo.getQueryParameters().getFirst(directiveParam);

		boolean operative = !Strings.isNullOrEmpty(opParam);
		operative = operative && Boolean.parseBoolean(opParam);

		if( operative )
		{
			if( !CurrentUser.getUserState().isSystem() )
			{
				throw new AccessDeniedException("Must be system user to " + directiveParam);
			}
		}
		return operative;
	}
}
