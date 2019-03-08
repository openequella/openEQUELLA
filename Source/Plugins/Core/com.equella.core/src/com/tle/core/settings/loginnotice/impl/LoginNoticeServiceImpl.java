/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.settings.loginnotice.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.settings.loginnotice.LoginNoticeService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.PrivilegeRequiredException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

@Singleton
@Bind(LoginNoticeService.class)
public class LoginNoticeServiceImpl implements LoginNoticeService {
  @Inject TLEAclManager tleAclManager;
  @Inject ConfigurationService configurationService;

  @Inject
  protected void setObjectMapperService(ObjectMapperService objectMapperService) {
    objectMapper = objectMapperService.createObjectMapper();
  }

  private ObjectMapper objectMapper;
  private static final String PERMISSION_KEY = "EDIT_SYSTEM_SETTINGS";
  private static final String PRE_LOGIN_NOTICE_KEY = "pre.login.notice";
  private static final String POST_LOGIN_NOTICE_KEY = "post.login.notice";

  @Override
  public PreLoginNotice getPreLoginNotice() throws IOException {
    String preLoginNotice = configurationService.getProperty(PRE_LOGIN_NOTICE_KEY);
    if (Check.isEmpty(preLoginNotice)) {
      return null;
    }
    return objectMapper.readValue(preLoginNotice, PreLoginNotice.class);
  }

  @Override
  public void setPreLoginNotice(PreLoginNotice notice) throws JsonProcessingException {
    checkPermissions();
    if (StringUtils.isBlank(notice.getNotice())) {
      configurationService.deleteProperty(PRE_LOGIN_NOTICE_KEY);
    } else {
      if (notice.getEndDate().before(notice.getStartDate())) {
        throw new BadRequestException(
            "Invalid start and end date. Start date must be on or before end date.");
      }
      configurationService.setProperty(
          PRE_LOGIN_NOTICE_KEY, objectMapper.writeValueAsString(notice));
    }
  }

  @Override
  public void deletePreLoginNotice() {
    checkPermissions();
    configurationService.deleteProperty(PRE_LOGIN_NOTICE_KEY);
  }

  @Override
  public String getPostLoginNotice() {
    return configurationService.getProperty(POST_LOGIN_NOTICE_KEY);
  }

  @Override
  public void setPostLoginNotice(String notice) {
    checkPermissions();
    if (StringUtils.isBlank(notice)) {
      configurationService.deleteProperty(POST_LOGIN_NOTICE_KEY);
    } else {
      configurationService.setProperty(POST_LOGIN_NOTICE_KEY, notice);
    }
  }

  @Override
  public void deletePostLoginNotice() {
    checkPermissions();
    configurationService.deleteProperty(POST_LOGIN_NOTICE_KEY);
  }

  @Override
  public boolean isActive(PreLoginNotice preLoginNotice) {

    switch (preLoginNotice.getScheduleSettings()) {
      case OFF:
        return false;
      case ON:
        return true;
      case SCHEDULED:
        Date now = new Date();
        Date startDateMidnight = preLoginNotice.getStartDate(); // 12am at the start the first day
        DateUtils.truncate(startDateMidnight, Calendar.DAY_OF_MONTH);

        Date endDateMidnight = preLoginNotice.getEndDate(); // 12am at the end of the last day
        DateUtils.truncate(endDateMidnight, Calendar.DAY_OF_MONTH);
        endDateMidnight = DateUtils.addDays(endDateMidnight, 1);
        return !(now.before(startDateMidnight) || now.after(endDateMidnight));
      default:
        return false;
    }
  }

  private void checkPermissions() {
    if (tleAclManager
        .filterNonGrantedPrivileges(Collections.singleton(PERMISSION_KEY), false)
        .isEmpty()) {
      throw new PrivilegeRequiredException(PERMISSION_KEY);
    }
  }
}
