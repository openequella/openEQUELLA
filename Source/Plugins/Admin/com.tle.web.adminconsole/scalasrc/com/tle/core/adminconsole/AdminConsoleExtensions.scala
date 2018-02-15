package com.tle.core.adminconsole

import javax.inject.Inject

import com.tle.common.adminconsole.RemoteAdminService
import com.tle.core.application.StartupBean
import com.tle.core.guice.Bind
import com.tle.web.resources.ResourcesService
import com.tle.web.settings.{SettingsList, SettingsPage}

@Bind
class AdminConsoleExtensions extends StartupBean {
  @Inject
  var adminService : RemoteAdminService = _

  override def startup(): Unit = {
    SettingsList += SettingsPage(ResourcesService.getResourceHelper(getClass), "adminconsole", "admin.link.title", "admin.link.description",
      "jnlp/admin.jnlp", () => !adminService.getAllowedTools.isEmpty)
  }
}
