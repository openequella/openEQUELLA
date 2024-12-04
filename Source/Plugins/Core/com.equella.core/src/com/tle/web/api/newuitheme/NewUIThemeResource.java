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

package com.tle.web.api.newuitheme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tle.web.api.newuitheme.impl.NewUITheme;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("theme/")
@Api("New UI Theme Resource")
public interface NewUIThemeResource {

  @GET
  @Path("theme.js")
  @ApiParam("Retrieves the current institution's theme and logo settings from the database.")
  @Produces("application/javascript")
  Response retrieveThemeInfo(@Context UriInfo info) throws IOException;

  @GET
  @Path("newLogo.png")
  @ApiParam("Retrieves the current institution's logo from the filestore.")
  @Produces("image/png")
  Response retrieveLogo() throws IOException;

  @GET
  @Path("settings")
  @ApiParam("Retrieves the current institution's theme settings from the database.")
  @Produces("application/json")
  Response retrieveTheme() throws IOException;

  @PUT
  @Path("settings")
  @ApiParam("Changes the theme settings of the current institution.")
  Response updateTheme(NewUITheme theme) throws JsonProcessingException, IOException;

  @PUT
  @Path("logo")
  @ApiParam("Takes an image file, resizes it to become a logo and saves it to the filestore.")
  Response updateLogo(File logo) throws IOException;

  @DELETE
  @Path("logo")
  @ApiParam("Resets the institution's logo to the default openEQUELLA logo.")
  Response resetLogo();

  @GET
  @Path("legacy.css")
  @ApiParam("Retrieves the current institution's legacy css file")
  @Produces("text/css")
  Response retrieveLegacyCss() throws IOException;
}
