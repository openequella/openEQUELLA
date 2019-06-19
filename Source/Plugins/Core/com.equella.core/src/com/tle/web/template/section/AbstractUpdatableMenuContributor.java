package com.tle.web.template.section;

import com.tle.core.services.user.UserSessionService;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractUpdatableMenuContributor implements MenuContributor {
  @Inject protected UserSessionService userSessionService;

  protected abstract String getSessionKey();

  @Override
  public final void clearCachedData() {
    HttpServletRequest request = userSessionService.getAssociatedRequest();
    if (request != null) {
      request.setAttribute(MenuContributor.KEY_MENU_UPDATED, true);
    }
    userSessionService.removeAttribute(getSessionKey());
  }
}
