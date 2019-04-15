package com.tle.web.loginnotice;

import com.tle.common.security.SettingsTarget;
import com.tle.core.guice.Bind;
import com.tle.core.settings.security.AbstractSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
public class LoginNoticeEditorPrivilegeTreeProvider extends AbstractSettingsPrivilegeTreeProvider {

  public LoginNoticeEditorPrivilegeTreeProvider() {
    super(
        Type.SYSTEM_SETTING,
        ResourcesService.getResourceHelper(LoginNoticeEditorPrivilegeTreeProvider.class)
            .key("securitytree.loginnoticeeditor"),
        new SettingsTarget("loginnoticeeditor"));
  }
}
