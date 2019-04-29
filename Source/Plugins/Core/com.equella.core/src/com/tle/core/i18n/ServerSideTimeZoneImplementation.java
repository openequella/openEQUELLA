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

package com.tle.core.i18n;

import com.dytech.edge.web.WebConstants;
import com.google.inject.Singleton;
import com.tle.common.i18n.CurrentTimeZone.AbstractCurrentTimeZone;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.services.user.UserSessionService;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Bind
@Singleton
public class ServerSideTimeZoneImplementation extends AbstractCurrentTimeZone {
  private static final InheritableThreadLocal<TimeZoneData> local =
      new InheritableThreadLocal<TimeZoneData>();

  @Inject private UserPreferenceService userPreferenceService;
  @Inject private UserSessionService userSessionService;

  @com.google.inject.Inject(optional = true)
  // can be overrode by the optional-config.properties
  @Named("timeZone.default")
  private String defaultTimeZoneName;

  private TimeZone defaultTimeZone;

  @Override
  public TimeZone get() {
    return getTimeZoneData().getTimeZone();
  }

  private TimeZoneData getTimeZoneData() {
    TimeZoneData timeZoneData = local.get();
    UserState userState = CurrentUser.getUserState();
    if (timeZoneData != null && timeZoneData.getUserState() == userState) {
      return timeZoneData;
    }
    TimeZone tz = null;
    boolean sessionAvailable = userSessionService.isSessionAvailable();
    if (sessionAvailable) {
      tz = userSessionService.getAttribute(WebConstants.KEY_TIMEZONE);
    }
    if (tz == null) {
      if (userState == null) {
        tz = defaultTimeZone;
      } else {
        tz = userPreferenceService.getPreferredTimeZone(defaultTimeZone);
      }
    }
    if (sessionAvailable) {
      userSessionService.setAttribute(WebConstants.KEY_TIMEZONE, tz);
    }
    timeZoneData = new TimeZoneData(userState, tz);
    local.set(timeZoneData);
    return timeZoneData;
  }

  @PostConstruct
  public void setupDefaultTimezone() {
    if (defaultTimeZoneName != null) {
      defaultTimeZone = TimeZone.getTimeZone(defaultTimeZoneName);
    } else {
      defaultTimeZone = TimeZone.getDefault();
    }
  }

  private static class TimeZoneData {
    private final UserState userState;
    private final TimeZone timeZone;

    public TimeZoneData(UserState userState, TimeZone timeZone) {
      this.userState = userState;
      this.timeZone = timeZone;
    }

    public UserState getUserState() {
      return userState;
    }

    public TimeZone getTimeZone() {
      return timeZone;
    }
  }

  public void clearThreadLocals() {
    local.remove();
  }
}
