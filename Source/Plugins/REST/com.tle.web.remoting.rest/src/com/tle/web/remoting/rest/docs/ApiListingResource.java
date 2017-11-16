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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import io.swagger.annotations.Api;
import io.swagger.models.Swagger;

@Bind
@Path("/resources")
@Api("/resources")
@Produces({MediaType.APPLICATION_JSON})
public class ApiListingResource extends io.swagger.jaxrs.listing.ApiListingResource
{

	@Override
	protected Swagger process(Application app, ServletContext servletContext, ServletConfig sc, HttpHeaders headers, UriInfo uriInfo)
	{
		String path = CurrentInstitution.get().getUrlAsUrl().getPath();
		Swagger swags = super.process(app, servletContext, sc, headers, uriInfo);
		swags.setBasePath(path+"api");
		return swags;
	}
}