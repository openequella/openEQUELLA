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
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.tle.core.guice.Bind;
import com.wordnik.swagger.annotations.Api;

@Bind
@Path("/resources")
@Api("/resources")
@Produces({MediaType.APPLICATION_JSON})
public class ApiListingResource extends com.wordnik.swagger.jaxrs.listing.ApiListingResource
{
	@Override
	public Response resourceListing(Application app, ServletConfig sc, HttpHeaders headers, UriInfo uriInfo)
	{
		Response resourceListing = super.resourceListing(app, sc, headers, uriInfo);
		return resourceListing;
	}
}
