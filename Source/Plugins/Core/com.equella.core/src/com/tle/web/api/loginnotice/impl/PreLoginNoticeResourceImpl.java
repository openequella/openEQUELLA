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

package com.tle.web.api.loginnotice.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tle.core.guice.Bind;
import com.tle.core.settings.loginnotice.LoginNoticeService;
import com.tle.core.settings.loginnotice.impl.PreLoginNotice;
import com.tle.web.api.loginnotice.PreLoginNoticeResource;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Bind(PreLoginNoticeResource.class)
@Singleton
public class PreLoginNoticeResourceImpl implements PreLoginNoticeResource {
  @Inject LoginNoticeService noticeService;

  @Override
  public Response retrievePreLoginNotice() throws IOException {
    PreLoginNotice loginNotice = noticeService.getPreLoginNotice();
    if (loginNotice != null) {
      return Response.ok(loginNotice, "application/json").build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).entity("{}").build();
    }
  }

  @Override
  public Response setPreLoginNotice(PreLoginNotice loginNotice) throws JsonProcessingException {
    noticeService.setPreLoginNotice(loginNotice);
    return Response.ok().build();
  }

  @Override
  public Response deletePreLoginNotice() {
    noticeService.deletePreLoginNotice();
    return Response.ok().build();
  }
}
