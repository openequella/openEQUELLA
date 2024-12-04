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

package com.tle.web.usermanagement.canvas;

import com.tle.beans.usermanagement.canvas.CanvasWrapperSettings;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.guice.Bind;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.plugins.ump.UserManagementLogonFilter;
import com.tle.web.dispatcher.FilterResult;
import hurl.build.QueryBuilder;
import hurl.build.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bind
@SuppressWarnings("nls")
public class CanvasLogonFilter implements UserManagementLogonFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(CanvasLogonFilter.class);
  private static final String PARAM_NO_AUTO_LOGIN = "NO_AUTO_LOGIN";

  @Inject private ConfigurationService configurationService;

  private CanvasWrapperSettings settings;

  @Override
  public boolean init(Map<Object, Object> attributes) {
    settings = configurationService.getProperties(new CanvasWrapperSettings());
    if (settings.isEnabled() && LOGGER.isDebugEnabled()) {
      LOGGER.debug("Canvas URL: " + settings.getCanvasUrl());
      LOGGER.debug("Canvas Client ID: " + settings.getClientId());
    }
    return settings.isEnabled();
  }

  @Override
  public void addStateParameters(HttpServletRequest request, Map<String, String[]> params) {
    if (request.getParameter(PARAM_NO_AUTO_LOGIN) != null) {
      params.put(PARAM_NO_AUTO_LOGIN, new String[] {"true"});
    }
  }

  @Override
  public FilterResult filter(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if (!CurrentUser.isGuest()
        || request.getParameter(PARAM_NO_AUTO_LOGIN) != null
        || !settings.isBypassLogonPage()) {
      return null;
    }
    return new FilterResult("/canvassso");
  }

  @Override
  public URI logoutRedirect(URI loggedOutURI) {
    return null;
  }

  @Override
  public URI logoutURI(UserState state, URI loggedOutURI) {
    QueryBuilder loggedOutQB = QueryBuilder.create();
    String loggedOutQuery = loggedOutURI.getQuery();
    if (loggedOutQuery != null) {
      loggedOutQB.parse(loggedOutQuery);
    }
    loggedOutQB.addParam(PARAM_NO_AUTO_LOGIN, "true");
    UriBuilder builder = UriBuilder.create(loggedOutURI);
    builder.setQuery(loggedOutQB);
    return builder.build();
  }
}
