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

package com.tle.web.login;

import com.dytech.edge.web.WebConstants;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.settings.standard.AutoLogin;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.user.UserService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.settings.loginnotice.LoginNoticeService;
import com.tle.core.settings.loginnotice.impl.PreLoginNotice;
import com.tle.exceptions.AccountExpiredException;
import com.tle.exceptions.AuthenticationException;
import com.tle.exceptions.BadCredentialsException;
import com.tle.exceptions.UsernameNotFoundException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.BeforeEventsListener;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.*;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.MenuMode;
import com.tle.web.template.RenderNewTemplate;
import hurl.build.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class LogonSection extends AbstractPrototypeSection<LogonSection.LogonModel>
    implements HtmlRenderer, BeforeEventsListener, BookmarkEventListener {
  public static final String STANDARD_LOGON_PATH = "/logon.do";

  private static final PluginResourceHelper RESOURCE_HELPER =
      ResourcesService.getResourceHelper(LogonSection.class);
  private static final ExternallyDefinedFunction LOGON_READY =
      new ExternallyDefinedFunction(
          "logonReady", new IncludeFile(RESOURCE_HELPER.url("scripts/logon.js")));

  @PlugKey("logon.title")
  private static Label TITLE_LABEL;

  @PlugKey("logon.error.usernamenotfound")
  private static String KEY_ERROR_USERNAME_NOT_FOUND;

  @Inject private UserService userService;
  @Inject private UserSessionService sessionService;
  @Inject private AuditLogService auditLogService;
  @Inject private PluginTracker<LoginLink> loginLinkTracker;
  @Inject LoginNoticeService loginNoticeService;

  @ViewFactory private FreemarkerFactory viewFactory;
  @EventFactory private EventGenerator events;

  @Component private TextField username;

  @Component(stateful = false)
  private TextField password;

  @Component
  @PlugKey("logon.login")
  private Button logonButton;

  @Inject @Component private OidcLoginSection oidcLoginSection;

  @Override
  public String getDefaultPropertyName() {
    return "";
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new LogonModel();
  }

  @Override
  public void beforeEvents(SectionInfo info) {
    AutoLogin autoLogin = userService.getAttribute(AutoLogin.class);
    HttpServletRequest request = info.getRequest();
    if (autoLogin != null && request != null && autoLogin.isLoginViaSSL() && !request.isSecure()) {
      String href = info.getPublicBookmark().getHref();
      UriBuilder uriBuilder = UriBuilder.create(URI.create(href));
      uriBuilder.setScheme("https");
      info.forwardToUrl(uriBuilder.build().toString());
    }
  }

  /**
   * @param info
   * @throws IOException
   */
  @DirectEvent(priority = SectionEvent.PRIORITY_AFTER_EVENTS)
  public void checkAutoLogonAndLogout(SectionInfo info) throws IOException {
    LogonModel model = getModel(info);
    if (!Check.isEmpty(model.getError())) {
      return;
    }
    if (model.isLogout()) {
      model.setLogout(false);
      URI logoutURI = URI.create(info.getPublicBookmark().getHref());

      // Add UM filter params
      logoutURI = userService.logoutURI(CurrentUser.getUserState(), logoutURI);

      // Redirect
      HttpServletRequest request = info.getRequest();
      userService.logoutToGuest(userService.getWebAuthenticationDetails(request), false);
      info.forwardToUrl(userService.logoutRedirect(logoutURI).toString());
      return;
    }
    if (!CurrentUser.isGuest()) {
      redirectToLogonnotice(info);
    }
  }

  /**
   * @param info
   * @return true, always
   */
  private boolean redirectToLogonnotice(SectionInfo info) {
    sessionService.forceSession();
    LogonModel model = getModel(info);
    // Save the page parameter for redirection fun later - see
    // LogonNoticeAction class
    if (model.getPage() != null) {
      sessionService.setAttribute(WebConstants.PAGE_AFTER_LOGON_KEY, model.getPage());
    }
    info.forwardAsBookmark(info.createForward("logonnotice.do"));
    return true;
  }

  @EventHandlerMethod(preventXsrf = false)
  public void authenticate(SectionInfo info) {
    LogonModel model = getModel(info);

    String usernameText = username.getValue(info);
    String passwordText = password.getValue(info);
    WebAuthenticationDetails wad = getDetails(info);

    try {
      userService.login(usernameText, passwordText, wad, true);
      redirectToLogonnotice(info);
    } catch (AuthenticationException ex) {
      String failedMessage;
      if (ex instanceof BadCredentialsException) {
        failedMessage = RESOURCE_HELPER.key("logon.invalid");
      } else if (ex instanceof AccountExpiredException) {
        failedMessage = ex.getMessage();
      } else {
        failedMessage = RESOURCE_HELPER.key("logon.problems");
      }
      auditLogService.logUserFailedAuthentication(usernameText, wad);
      model.setFailed(failedMessage);
      info.preventGET();
    }
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    LogonModel model = getModel(context);
    password.setValue(context, "");
    context.getBody().addReadyStatements(LOGON_READY);
    Decorations decorations = Decorations.getDecorations(context);
    decorations.setTitle(TITLE_LABEL);
    decorations.setMenuMode(MenuMode.HIDDEN);
    PreLoginNotice preLoginNotice = new PreLoginNotice();
    try {
      preLoginNotice = loginNoticeService.getPreLoginNotice();
    } catch (IOException e) {
      model.setFailed(e.getMessage());
    }
    if (preLoginNotice != null && loginNoticeService.isActive(preLoginNotice)) {
      model.setLoginNotice(preLoginNotice.getNotice());
    }
    model.setChildSections(
        renderChildren(context, this, new ResultListCollector(true)).getFirstResult());
    final List<SectionRenderable> loginLinksRenderables = new ArrayList<>();
    for (LoginLink link : loginLinkTracker.getBeanList()) {
      link.setup(context, model);
      final SectionRenderable linkRenderer = renderSection(context, link);
      if (linkRenderer != null) {
        loginLinksRenderables.add(linkRenderer);
      }
    }
    model.setLoginLinks(loginLinksRenderables);

    // In New UI, if a login token error is captured and saved in Session, update the model
    // to show the error and then clear the error from Session.
    Optional.ofNullable(sessionService.<String>getAttribute(WebConstants.KEY_LOGIN_EXCEPTION))
        .filter(err -> RenderNewTemplate.isNewUIEnabled())
        .ifPresent(
            err -> {
              model.setError(err);
              sessionService.removeAttribute(WebConstants.KEY_LOGIN_EXCEPTION);
            });

    return viewFactory.createResult("logon/logon.ftl", context);
  }

  private WebAuthenticationDetails getDetails(SectionInfo info) {
    LogonModel model = getModel(info);
    WebAuthenticationDetails details = model.getDetails();
    if (details == null) {
      // We want to clone the details from the existing user state so that
      // the referrer is where we came from to get to the EQUELLA URL we
      // needed authentication for, rather than the EQUELLA URL that we
      // needed authentication for which is useless.
      details = new WebAuthenticationDetails(CurrentUser.getUserState());
      model.setDetails(details);
    }
    return details;
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    logonButton.setClickHandler(events.getNamedHandler("authenticate"));
    for (LoginLink link : loginLinkTracker.getBeanList()) {
      tree.registerInnerSection(link, id);
    }
  }

  public TextField getUsername() {
    return username;
  }

  public TextField getPassword() {
    return password;
  }

  public Button getLogonButton() {
    return logonButton;
  }

  public OidcLoginSection getOidcLoginSection() {
    return oidcLoginSection;
  }

  public static void forwardToLogon(
      SectionInfo info, @Nullable String relativeUrl, String loginPath) {
    SectionInfo forward = info.createForward(loginPath);
    setupForward(forward, relativeUrl);
    info.forwardAsBookmark(forward);
  }

  public static void forwardToLogon(
      SectionsController controller,
      HttpServletRequest request,
      HttpServletResponse response,
      @Nullable String relativeUrl,
      String loginPath) {
    SectionInfo forward = controller.createInfo(loginPath, request, response, null, null, null);
    setupForward(forward, relativeUrl);
    forward.forceRedirect();
    controller.execute(forward);
  }

  public static Bookmark forwardToLogonBookmark(
      SectionInfo info, @Nullable String relativeUrl, String loginPath) {
    SectionInfo forward = info.createForward(loginPath);
    setupForward(forward, relativeUrl);
    return forward.getPublicBookmark();
  }

  private static void setupForward(SectionInfo forward, @Nullable String relativeUrl) {
    LogonSection logonSection = forward.lookupSection(LogonSection.class);
    LogonModel model = logonSection.getModel(forward);
    model.setPage(relativeUrl);

    Exception exception =
        (Exception) forward.getRequest().getAttribute(WebConstants.KEY_LOGIN_EXCEPTION);
    if (exception != null) {
      if (exception instanceof UsernameNotFoundException) {
        UsernameNotFoundException unnfe = (UsernameNotFoundException) exception;
        model.setError(CurrentLocale.get(KEY_ERROR_USERNAME_NOT_FOUND, unnfe.getUsername()));
      } else {
        model.setError(exception.getLocalizedMessage());
      }
    }
  }

  @NonNullByDefault(false)
  public static class LogonModel {
    @Bookmarked private boolean logout;
    @Bookmarked private String page;
    @Bookmarked private String error;

    private String loginNotice;

    private String failed;
    private WebAuthenticationDetails details;
    private SectionRenderable childSections;
    private List<SectionRenderable> loginLinks;

    public String getFailed() {
      return failed;
    }

    public void setFailed(String failed) {
      this.failed = failed;
    }

    public String getPage() {
      return page;
    }

    public void setPage(String page) {
      this.page = page;
    }

    public boolean isLogout() {
      return logout;
    }

    public void setLogout(boolean logout) {
      this.logout = logout;
    }

    public String getError() {
      return error;
    }

    public void setError(String error) {
      this.error = error;
    }

    public WebAuthenticationDetails getDetails() {
      return details;
    }

    public void setDetails(WebAuthenticationDetails details) {
      this.details = details;
    }

    public SectionRenderable getChildSections() {
      return childSections;
    }

    public void setChildSections(SectionRenderable childSections) {
      this.childSections = childSections;
    }

    public List<SectionRenderable> getLoginLinks() {
      return loginLinks;
    }

    public void setLoginLinks(List<SectionRenderable> loginLinks) {
      this.loginLinks = loginLinks;
    }

    public String getLoginNotice() {
      return loginNotice;
    }

    public void setLoginNotice(String loginNotice) {
      this.loginNotice = loginNotice;
    }
  }

  @Override
  public void bookmark(SectionInfo info, BookmarkEvent event) {
    HttpServletRequest request = info.getRequest();
    if (request != null) {
      Map<String, String[]> state = userService.getAdditionalLogonState(request);
      for (Entry<String, String[]> entry : state.entrySet()) {
        event.setParams(entry.getKey(), entry.getValue());
      }
    }
  }

  @Override
  public void document(SectionInfo info, DocumentParamsEvent event) {
    // nothing
  }
}
