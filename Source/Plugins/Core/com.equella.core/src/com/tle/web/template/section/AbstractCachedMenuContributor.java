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

package com.tle.web.template.section;

import com.tle.common.ExpiringValue;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.SectionInfo;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

public abstract class AbstractCachedMenuContributor<T> implements MenuContributor {
  private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
  @Inject private UserSessionService userSessionService;

  @Override
  public void clearCachedData() {
    userSessionService.removeAttribute(getSessionKey());
  }

  @Override
  public List<MenuContribution> getMenuContributions(SectionInfo info) {
    if (currentUserCanView(info)) {
      ExpiringValue<T> cachedCount = userSessionService.getAttribute(getSessionKey());
      if (cachedCount == null || cachedCount.isTimedOut()) {
        cachedCount =
            ExpiringValue.expireAfter(getCachedObject(info), cacheMillis(), TimeUnit.MILLISECONDS);
        userSessionService.setAttribute(getSessionKey(), cachedCount);
      }
      MenuContribution contribution = getContribution(info, cachedCount.getValue());
      if (contribution != null) {
        return Collections.singletonList(contribution);
      }
    }
    return Collections.emptyList();
  }

  protected abstract MenuContribution getContribution(SectionInfo info, T value);

  protected abstract T getCachedObject(SectionInfo info);

  protected long cacheMillis() {
    return ONE_MINUTE;
  }

  protected boolean currentUserCanView(SectionInfo info) {
    return true;
  }

  protected abstract String getSessionKey();
}
