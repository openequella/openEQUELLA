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
