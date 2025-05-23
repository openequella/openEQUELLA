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

package com.tle.web.api.server;

import bean.ServerInfo;
import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Bind
@Path("status")
@Api(value = "Server status", description = "status")
@Produces({"application/json"})
@Singleton
public class ServerResource {
  @GET
  @Path("")
  @ApiOperation("Check server health")
  public Response isAlive() {
    return Response.ok(new ServerInfo()).build();
  }

  @GET
  @Path("heartbeat")
  @Produces({"text/plain"})
  @ApiOperation("Keep your session alive")
  public String heartbeat() {
    return "OK";
  }
}
