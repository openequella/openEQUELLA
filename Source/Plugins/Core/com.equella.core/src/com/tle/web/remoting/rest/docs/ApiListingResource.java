/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import io.swagger.annotations.ApiOperation;
import io.swagger.jaxrs.listing.BaseApiListingResource;
import io.swagger.models.Swagger;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import org.apache.commons.lang3.StringUtils;

@Path("/swagger.{type:json|yaml}")
@Bind
@Singleton
public class ApiListingResource extends BaseApiListingResource {

  @Context ServletContext context;

  @GET
  @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
  @ApiOperation(value = "The swagger definition in either JSON or YAML", hidden = true)
  public Response getListing(
      @Context Application app,
      @Context ServletConfig sc,
      @Context HttpHeaders headers,
      @Context UriInfo uriInfo,
      @PathParam("type") String type)
      throws JsonProcessingException {
    if (StringUtils.isNotBlank(type) && type.trim().equalsIgnoreCase("yaml")) {
      return getListingYamlResponse(app, context, sc, headers, uriInfo);
    } else {
      return getListingJsonResponse(app, context, sc, headers, uriInfo);
    }
  }

  @Override
  protected Swagger process(
      Application app,
      ServletContext servletContext,
      ServletConfig sc,
      HttpHeaders headers,
      UriInfo uriInfo) {
    String path = CurrentInstitution.get().getUrlAsUrl().getPath();
    Swagger swags = super.process(app, servletContext, sc, headers, uriInfo);
    swags.setBasePath(path + "api");
    return swags;
  }
}
