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

package com.tle.core.events;

import com.tle.core.events.listeners.UserChangeListener;

public class UserEditEvent extends ApplicationEvent<UserChangeListener> {
  private static final long serialVersionUID = 1L;

  private final String userID;

  public UserEditEvent(String userID) {
    super(PostTo.POST_ONLY_TO_SELF);
    this.userID = userID;
  }

  public String getUserID() {
    return userID;
  }

  @Override
  public Class<UserChangeListener> getListener() {
    return UserChangeListener.class;
  }

  @Override
  public void postEvent(UserChangeListener listener) {
    listener.userEditedEvent(this);
  }
}
