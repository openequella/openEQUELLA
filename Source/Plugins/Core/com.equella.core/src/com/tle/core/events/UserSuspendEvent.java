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

import com.tle.core.events.listeners.UserSuspendListener;
import java.util.Set;

/**
 * Event fired when one or more user accounts are suspended. UUIDs of the suspended accounts are
 * provided by the event.
 */
public class UserSuspendEvent extends ApplicationEvent<UserSuspendListener> {
  private static final long serialVersionUID = 1L;

  private final Set<String> suspendedUserId;

  public UserSuspendEvent(Set<String> suspendedUserId) {
    super(PostTo.POST_TO_ALL_CLUSTER_NODES);

    this.suspendedUserId = suspendedUserId;
  }

  public Set<String> getSuspendedUserId() {
    return suspendedUserId;
  }

  @Override
  public Class<UserSuspendListener> getListener() {
    return UserSuspendListener.class;
  }

  @Override
  public void postEvent(UserSuspendListener listener) {
    listener.userSuspendEvent(this);
  }
}
