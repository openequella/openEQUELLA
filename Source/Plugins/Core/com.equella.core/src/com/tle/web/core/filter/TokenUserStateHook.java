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

package com.tle.web.core.filter;

import com.dytech.edge.exceptions.WebException;
import com.dytech.edge.web.WebConstants;
import com.tle.common.Check;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.core.services.user.UserSessionService;
import com.tle.exceptions.TokenException;
import com.tle.web.core.filter.UserStateResult.Result;
import com.tle.web.template.RenderNewTemplate;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bind
@Singleton
public class TokenUserStateHook implements UserStateHook {
  private static final Logger LOGGER = LoggerFactory.getLogger(TokenUserStateHook.class);

  @Inject private UserService userService;
  @Inject private UserSessionService sessionService;

  @Override
  public UserStateResult getUserState(HttpServletRequest request, UserState userState)
      throws WebException {
    // We want/need token to override the current session
    String token = request.getParameter(WebConstants.TOKEN_AUTHENTICATION_PARAM);
    if (!Check.isEmpty(token)) {
      boolean noExistingSessionOrGuest = userState == null || userState.isGuest();
      if (noExistingSessionOrGuest || !userService.verifyUserStateForToken(userState, token)) {
        try {
          // This call may also throw a UsernameNotFound exception -
          // we do *NOT* want to catch this. UsernameNotFound
          // exception should propagate back to the user screen as an
          // error.
          UserState hookState =
              userService.authenticateWithToken(
                  token, userService.getWebAuthenticationDetails(request));
          if (hookState != null) {
            return new UserStateResult(hookState);
          }
          return null;
        } catch (TokenException ex) {
          LOGGER.warn("Error with token:" + token);

          // We can save the login token error in the request attribute in Old UI, but we can't do
          // this in New UI because it sends another POST request to query the page content.
          // Instead,
          // we need to store it in the current session so that it can be persisted across the two
          // requests.
          if (RenderNewTemplate.isNewUIEnabled()) {
            sessionService.setAttribute(WebConstants.KEY_LOGIN_EXCEPTION, ex.getLocalizedMessage());
          } else {
            request.setAttribute(WebConstants.KEY_LOGIN_EXCEPTION, ex);
          }

          if (!noExistingSessionOrGuest) {
            // We want to make sure they logout of their invalid
            // state.
            return new UserStateResult(Result.GUEST);
          }
        }
      }
    }
    return null;
  }

  @Override
  public boolean isInstitutional() {
    return true;
  }
}
