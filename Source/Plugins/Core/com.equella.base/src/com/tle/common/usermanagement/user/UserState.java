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

package com.tle.common.usermanagement.user;

import com.tle.beans.Institution;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/** @author Nicholas Read */
public interface UserState extends Cloneable, Serializable {
  String getSessionID();

  UserBean getUserBean();

  Institution getInstitution();

  Set<String> getUsersGroups();

  Set<String> getUsersRoles();

  String getIpAddress();

  String getHostAddress();

  String getHostReferrer();

  String getSharePassEmail();

  String getToken();

  String getTokenSecretId();

  Collection<Long> getCommonAclExpressions();

  Collection<Long> getOwnerAclExpressions();

  Collection<Long> getNotOwnerAclExpressions();

  boolean isGuest();

  boolean isSystem();

  boolean isInternal();

  boolean wasAutoLoggedIn();

  void setWasAutoLoggedIn(boolean b);

  boolean isAuthenticated();

  void setAuthenticated(boolean b);

  boolean isAuditable();

  void setAuditable(boolean b);

  boolean isNeedsSessionUpdate();

  UserState clone() throws CloneNotSupportedException;

  void updatedInSession();

  <T> T getCachedAttribute(Object key);

  void setCachedAttribute(Object key, Object value);

  void removeCachedAttribute(Object key);

  String getImpersonatedBy();
}
