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

package com.tle.integration.lti.generic;

import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.core.guice.Bind;
import com.tle.web.lti.usermanagement.LtiWrapperExtension;
import java.util.function.Supplier;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

@Bind
@Singleton
public class GenericLtiWrapperExtension implements LtiWrapperExtension {
  @Override
  public String getUserId(HttpServletRequest request) {
    return null;
  }

  @Override
  public String getUserId(HttpServletRequest request, String param) {
    return getGenericLtiParam(request, param, () -> request.getParameter(param));
  }

  @Override
  public String getUsername(HttpServletRequest request) {
    return getUsername(request, null);
  }

  @Override
  public String getUsername(HttpServletRequest request, String param) {
    return getGenericLtiParam(
        request,
        param,
        () -> {
          String username = request.getParameter(param);
          if (StringUtils.isEmpty(username)) {
            username = request.getParameter(ExternalToolConstants.LIS_PERSON_SOURCEDID);
          }

          return username;
        });
  }

  @Override
  public String getFirstName(HttpServletRequest request) {
    return null;
  }

  @Override
  public String getLastName(HttpServletRequest request) {
    return null;
  }

  @Override
  public String getEmail(HttpServletRequest request) {
    return null;
  }

  @Override
  public boolean isPrefixUserId() {
    return true;
  }

  private String getGenericLtiParam(
      HttpServletRequest request, String param, Supplier<String> supplier) {
    String family =
        request.getParameter(ExternalToolConstants.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE);
    if (StringUtils.isEmpty(family) || StringUtils.isEmpty(param)) {
      // If invalid inputs provided
      return null;
    }

    if (family.matches("(canvas)|(desire2learn)")) {
      // If request is for an LTI provider we have explicit support for
      return null;
    }

    return supplier.get();
  }
}
