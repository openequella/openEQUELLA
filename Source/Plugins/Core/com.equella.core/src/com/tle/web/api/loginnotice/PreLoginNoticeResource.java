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

package com.tle.web.api.loginnotice;

import com.tle.core.settings.loginnotice.impl.PreLoginNotice;
import io.swagger.annotations.Api;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("preloginnotice/")
@Api("Pre Login Notice")
public interface PreLoginNoticeResource {
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Response retrievePreLoginNotice() throws IOException;

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  Response setPreLoginNotice(PreLoginNotice loginNotice) throws IOException;

  @GET
  @Path("image/{name}")
  @PathParam("name")
  Response getPreLoginNoticeImage(@PathParam("name") String name) throws IOException;

  @PUT
  @Path("image/{name}")
  @PathParam("name")
  Response uploadPreLoginNoticeImage(
      InputStream imageFile, @PathParam("name") String name, @Context UriInfo info)
      throws IOException;

  @DELETE
  Response deletePreLoginNotice();
}
