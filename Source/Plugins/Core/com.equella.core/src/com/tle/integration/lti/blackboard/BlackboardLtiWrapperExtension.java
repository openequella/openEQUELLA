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

package com.tle.integration.lti.blackboard;

import com.tle.core.guice.Bind;
import com.tle.web.lti.usermanagement.LtiWrapperExtension;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

/**
 * Checks for Canvas custom_bb_user_login_id param to match an existing user.
 *
 * <p>NOTE 1/21/19 (CB): This was ported over from the Canvas logic. We may not Need this
 * eventually.
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class BlackboardLtiWrapperExtension implements LtiWrapperExtension {
  // TODO - 'lis_sourcedid' looks to provide this detail.  Anything we could gain by allowing it to
  // be custom?
  @Override
  public String getUserId(HttpServletRequest request) {
    return request.getParameter("custom_bb_user_id");
  }

  // TODO - 'lis_sourcedid' looks to provide this detail.  Anything we could gain by allowing it to
  // be custom?
  @Override
  public String getUsername(HttpServletRequest request) {
    return request.getParameter("custom_bb_user_login_id");
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
}
