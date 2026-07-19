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

package com.tle.core.services.user;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.beans.user.UserInfoBackup;
import com.tle.common.usermanagement.user.ModifiableUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.plugins.ump.UserDirectoryQueries;
import com.tle.web.dispatcher.FilterResult;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This interface provides methods to retrieve information about users, groups and roles from the
 * user management system. The user management system is a plugin system, so the implementation of
 * this interface will be provided by a plugin. Typically, that is a collection of UserDirectory
 * plugins which are interacted with sequentially and the results aggregated.
 *
 * <p>This is different from the TLEUserService, which is specifically focused on the internal TLE
 * Users. (And sits alongside TLEGroupService and TLERoleService.)
 *
 * <p>The <code>authenticate*</code> methods do not will only do authentication and setup a
 * UserState object; this must be passed back into the <code>login(UserState)</code> method if you
 * actually want to login a user. Alternatively, you can simply call any of the other <code>login*
 * </code> methods which do both of these steps in one go.
 */
public interface UserService extends UserDirectoryQueries {
  /**
   * Notify each configured UserDirectory plugin that the current session is still active, giving
   * plugins a chance to keep any external connections or sessions alive.
   */
  void keepAlive();

  /**
   * Get the IDs of all shared secrets configured in the current user management chain.
   *
   * @return The shared secret IDs aggregated across all configured plugins
   */
  List<String> getTokenSecretIds();

  /**
   * Get the configuration of the user management plugin identified by the given settings class.
   * Requires the {@code EDIT_USER_MANAGEMENT} privilege.
   *
   * @param settingsConfig The fully qualified class name of the plugin's settings class
   * @return The plugin's configuration, or null if no matching plugin is found
   */
  UserManagementSettings getPluginConfig(String settingsConfig);

  /**
   * Same as {@link #getPluginConfig(String)}, but without the privilege check. Intended for
   * internal read-only access to the configuration.
   *
   * @param settingsConfig The fully qualified class name of the plugin's settings class
   * @return The plugin's configuration, or null if no matching plugin is found
   */
  UserManagementSettings getReadOnlyPluginConfig(String settingsConfig);

  /**
   * Save the given user management plugin configuration. Requires the {@code EDIT_USER_MANAGEMENT}
   * privilege.
   *
   * @param config The configuration to save
   */
  void setPluginConfig(UserManagementSettings config);

  UserState login(
      String username, String password, WebAuthenticationDetails details, boolean forceSession);

  UserState loginWithToken(String token, WebAuthenticationDetails details, boolean forceSession);

  UserState loginAsUser(String username, WebAuthenticationDetails details, boolean forceSession);

  UserState authenticate(String username, String password, WebAuthenticationDetails details);

  UserState authenticateWithToken(String token, WebAuthenticationDetails details);

  UserState authenticateAsUser(String username, WebAuthenticationDetails details);

  UserState authenticateAsGuest(WebAuthenticationDetails details);

  UserState authenticateRequest(HttpServletRequest request);

  boolean verifyUserStateForToken(UserState userState, String token);

  void useUser(UserState userState);

  void login(UserState userState, boolean forceSession);

  void logoutToGuest(WebAuthenticationDetails details, boolean forceSession);

  boolean isWrapperEnabled(String settingsConfig);

  String getGeneratedToken(String secretId, String username);

  WebAuthenticationDetails getWebAuthenticationDetails(HttpServletRequest request);

  FilterResult runLogonFilters(HttpServletRequest request, HttpServletResponse response)
      throws IOException;

  Map<String, String[]> getAdditionalLogonState(HttpServletRequest request);

  URI logoutURI(UserState userState, URI loggedoutUri);

  URI logoutRedirect(URI loggedoutUri);

  <T> T getAttribute(Object key);

  // Only for autologin settings
  void refreshSettings();

  UserInfoBackup findUserInfoBackup(String username);

  void saveUserInfoBackup(UserBean userBean);

  /** This really is just for debugging. */
  String convertUserStateToString(UserState us);

  ModifiableUserState setupUserState(
      ModifiableUserState auth, WebAuthenticationDetails details, boolean authenticated);
}
