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

package com.tle.web.api.loginnotice.impl;

import com.google.gson.JsonObject;
import com.tle.common.URLUtils;
import com.tle.core.guice.Bind;
import com.tle.core.settings.loginnotice.LoginNoticeService;
import com.tle.core.settings.loginnotice.impl.PreLoginNotice;
import com.tle.web.api.loginnotice.PreLoginNoticeResource;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Bind(PreLoginNoticeResource.class)
@Singleton
public class PreLoginNoticeResourceImpl implements PreLoginNoticeResource {
  @Inject LoginNoticeService noticeService;

  @Inject
  private static PluginResourceHelper helper =
      ResourcesService.getResourceHelper(PreLoginNoticeResourceImpl.class);

  @Override
  public Response retrievePreLoginNotice() throws IOException {
    noticeService.checkPermissions();
    PreLoginNotice loginNotice = noticeService.getPreLoginNotice();
    if (loginNotice != null) {
      return Response.ok(loginNotice, "application/json").build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).type("text/plain").build();
    }
  }

  @Override
  public Response setPreLoginNotice(PreLoginNotice loginNotice) throws IOException {
    noticeService.setPreLoginNotice(loginNotice);
    return Response.ok().build();
  }

  @Override
  public Response deletePreLoginNotice() {
    noticeService.deletePreLoginNotice();
    return Response.ok().build();
  }

  @Override
  public Response getPreLoginNoticeImage(String name) throws IOException {
    return Response.ok(noticeService.getPreLoginNoticeImage(name), noticeService.getMimeType(name))
        .build();
  }

  @Override
  public Response uploadPreLoginNoticeImage(
      InputStream imageFile, String imageName, @Context UriInfo info) throws IOException {
    noticeService.checkPermissions();
    JsonObject returnLink = new JsonObject();
    String imageFileName = noticeService.uploadPreLoginNoticeImage(imageFile, imageName);
    String getImageAPIURL =
        info.getBaseUriBuilder().path(PreLoginNoticeResource.class).build()
            + URLUtils.urlEncode("image/" + imageFileName, false);
    returnLink.addProperty("link", getImageAPIURL);
    return Response.ok(returnLink.toString(), "application/json").build();
  }
}
