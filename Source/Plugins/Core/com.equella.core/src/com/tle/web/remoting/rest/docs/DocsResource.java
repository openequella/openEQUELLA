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

package com.tle.web.remoting.rest.docs;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;

@Bind
@Produces({"text/html"})
@Path("/")
@SuppressWarnings("nls")
public class DocsResource
{
	@Inject
	private InstitutionService institutionService;

	@GET
	@Path("/")
	@Produces("text/html")
	public Response docs()
	{
		return new DocsRedirect(institutionService.institutionalise("apidocs.do"));
	}
}
