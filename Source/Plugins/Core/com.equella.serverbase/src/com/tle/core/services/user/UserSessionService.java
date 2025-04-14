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

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@NonNullByDefault
public interface UserSessionService {
  @Nullable
  <T> T getAttribute(String key);

  /**
   * @param attribute WARNING: This object should be immutable!
   */
  void setAttribute(String key, Object attribute);

  void removeAttribute(String key);

  String createUniqueKey();

  boolean isSessionPrevented();

  void preventSessionUse();

  void reenableSessionUse();

  void bindRequest(HttpServletRequest request);

  void unbind();

  void nudgeSession();

  void forceSession();

  Iterable<UserSessionTimestamp> getInstitutionSessions();

  @Nullable
  <T> T getAttributeFromSession(HttpSession session, Institution institution, String attribute);

  HttpServletRequest getAssociatedRequest();

  boolean isSessionAvailable();

  Object getSessionLock();
}
