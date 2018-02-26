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

package com.tle.web.settings

import java.util
import java.util.concurrent.ConcurrentLinkedQueue

import com.tle.common.connectors.ConnectorConstants.{PRIV_CREATE_CONNECTOR, PRIV_EDIT_CONNECTOR}
import com.tle.common.externaltools.constants.ExternalToolConstants
import com.tle.common.lti.consumers.LtiConsumerConstants
import com.tle.common.userscripts.UserScriptsConstants
import com.tle.core.db.{DB, RunWithDB}
import com.tle.core.echo.EchoConstants
import com.tle.core.i18n.CoreStrings
import com.tle.core.oauth.OAuthConstants
import com.tle.core.security.AclChecks
import com.tle.legacy.LegacyGuice._
import com.tle.web.mimetypes.MimeEditorUtils
import com.tle.web.sections.render.TextLabel
import com.tle.web.sections.standard.model.{HtmlLinkState, SimpleBookmark}

import scala.collection.JavaConverters._
import scala.collection.mutable

object CoreSettingsPage
{
  def apply(id: String, group: String, nameKey: String, descKey: String, page: String, editable: () => Boolean) =
    SettingsPage(CoreStrings.lookup, id, group, nameKey, descKey, page, "web", editable)
}

object CoreSettingsRest
{
  def apply(id: String, group: String, nameKey: String, descKey: String, endpoint: String, editable: DB[Set[String]]) =
    SettingsPage(CoreStrings.lookup, id, group, nameKey, descKey, endpoint, "rest", () =>
      RunWithDB.execute(editable).nonEmpty)
}

object SettingsList {

  def +=(setting: EditableSettings): Unit = synchronized {
    allSettings += setting
  }

  def asLinkOrNull(settings: EditableSettings) : HtmlLinkState = {
    new HtmlLinkState(new TextLabel(settings.name), new SimpleBookmark(settings.uri))
  }

  val uiSettings = CoreSettingsRest("ui", "ui", "uisettings.name", "uisettings.desc", "api/settings/ui",
    AclChecks.filterNonGrantedPrivileges(Iterable("EDIT_SYSTEM_SETTINGS"), false))

  val echoSettings = CoreSettingsPage("echo", "integration", "echo.settings.title", "echo.settings.description",
    "access/echoservers.do", () => !aclManager.filterNonGrantedPrivileges(EchoConstants.PRIV_CREATE_ECHO, EchoConstants.PRIV_EDIT_ECHO).isEmpty)

  val connectorSettings = CoreSettingsPage("connectors", "integration","connector.setting.title", "connector.setting.description",
    "access/connectors.do", () => !aclManager.filterNonGrantedPrivileges(PRIV_CREATE_CONNECTOR, PRIV_EDIT_CONNECTOR).isEmpty)

  val ltiConsumersSettings = CoreSettingsPage("lti", "integration", "lti.settings.title", "lti.settings.description",
    "access/lticonsumers.do", () => !aclManager.filterNonGrantedPrivileges(LtiConsumerConstants.PRIV_CREATE_CONUSMER,
      LtiConsumerConstants.PRIV_EDIT_CONSUMER).isEmpty)

  val userScriptSettings = CoreSettingsPage("scripts", "general", "scripts.settings.title", "scripts.settings.description",
    "access/userscripts.do", () => !aclManager.filterNonGrantedPrivileges(UserScriptsConstants.PRIV_CREATE_SCRIPT,
      UserScriptsConstants.PRIV_EDIT_SCRIPT).isEmpty)

  val oauthSettings = CoreSettingsPage("oauth", "integration", "oauth.page.title", "oauth.setting.description",
    "access/oauthadmin.do", () => !aclManager.filterNonGrantedPrivileges(OAuthConstants.PRIV_CREATE_OAUTH_CLIENT,
      OAuthConstants.PRIV_EDIT_OAUTH_CLIENT, OAuthConstants.PRIV_ADMINISTER_OAUTH_TOKENS).isEmpty)

  val htmlEditorSettings = CoreSettingsPage("htmleditor", "general", "htmledit.settings.title", "htmledit.settings.description",
    "access/editoradmin.do", htmlEditorPrivProvider.isAuthorised)


  val externalToolsSettings = CoreSettingsPage("externaltools", "integration", "tools.settings.title", "tools.settings.description",
    "access/externaltools.do", () => !aclManager.filterNonGrantedPrivileges(ExternalToolConstants.PRIV_CREATE_TOOL, ExternalToolConstants.PRIV_EDIT_TOOL).isEmpty)

  val allSettings : mutable.Buffer[EditableSettings] = mutable.Buffer(
    connectorSettings, echoSettings, ltiConsumersSettings, userScriptSettings,
    oauthSettings, htmlEditorSettings, externalToolsSettings, uiSettings,

    CoreSettingsPage("shortcuts", "general", "shortcuts.settings.title", "shortcuts.settings.description",
      "access/shortcuturlssettings.do", shortcutPrivProvider.isAuthorised),

    CoreSettingsPage("language", "general", "language.title", "language.description",
      "access/language.do", langPrivProvider.isAuthorised),

    CoreSettingsPage("googleapi", "general", "google.settings.title", "google.settings.description",
      "access/googleapisettings.do", googlePrivProvider.isAuthorised),

    CoreSettingsPage("search", "general", "settings.link.title", "settings.link.description",
      "access/searchsettings.do", searchPrivProvider.isAuthorised),

    CoreSettingsPage("login", "general", "login.title", "login.description",
      "access/loginsettings.do", loginPrivProvider.isAuthorised),

    CoreSettingsPage("quickcontrib", "general", "quickcontributeandversionsettings.title", "quickcontributeandversionsettings.description",
      "access/quickcontributeandversionsettings.do", quickContribPrivProvider.isAuthorised),

    CoreSettingsPage("datafixes", "diagnostic", "fix.settings.title", "fix.settings.description",
      "access/manualdatafixes.do", manualFixPrivProvider.isAuthorised),

    CoreSettingsPage("oai", "integration", "oai.title", "oaiidentifier.description",
      "access/oaiidentifiersettings.do", oaiPrivProvider.isAuthorised),

    CoreSettingsPage("harvester", "general", "harvesterskipdrmsettings.title", "harvesterskipdrmsettings.description",
      "access/harvesterskipdrmsettings.do", harvesterPrivProvider.isAuthorised),

    CoreSettingsPage("scheduler", "general", "scheduler.settings.title", "scheduler.settings.description",
      "access/settings/scheduledtasks.do", scheduledPrivProvider.isAuthorised),

    CoreSettingsPage("mimetypes", "general", "mimetypes.settings.title", "mimetypes.settings.description",
      MimeEditorUtils.MIME_BOOKMARK, mimePrivProvider.isAuthorised),

    CoreSettingsPage("dates", "general", "dates.settings.title", "dates.settings.description",
      "access/dateformatsettings.do", datePrivProvider.isAuthorised),

    CoreSettingsPage("theme", "general", "customisation.settings.title", "customisation.settings.description",
      "access/themesettings.do", themePrivProvider.isAuthorised),

    CoreSettingsPage("googleanalytics", "general", "analytics.settings.title", "analytics.settings.description",
      "access/googleAnalyticsPage.do", analyticsPrivProvider.isAuthorised),

    CoreSettingsPage("diagnostics", "diagnostics", "diagnostics.settings.title", "diagnostics.settings.description",
      "access/diagnostics.do", diagnosticPrivProvider.isAuthorised),

    CoreSettingsPage("mail", "general", "settings.title", "settings.description",
      "access/mailsettings.do", mailPrivProvider.isAuthorised),

    CoreSettingsPage("customlinks", "general", "menu.title", "menu.description",
      "access/customlinks.do", () => !aclManager.filterNonGrantedPrivileges("EDIT_CUSTOM_LINK").isEmpty),

    CoreSettingsPage("remotecaching", "general", "remotecaching.title", "remotecaching.description",
      "access/remotecaching.do", remoteCachePrivProvider.isAuthorised),

    CoreSettingsPage("loggedin", "diagnostics", "liu.settings.title", "liu.settings.description",
      "access/liu.do", liuPrivProvider.isAuthorised),

    CoreSettingsPage("copyright", "general", "coursedefaults.title", "coursedefaults.description",
      "access/coursedefaultssettings.do", courseDefPrivProvider.isAuthorised),

    CoreSettingsPage("contentrestrictions", "general", "contentrestrictions.title", "contentrestrictions.description",
      "access/contentrestrictions.do", contentRestricPrivProvider.isAuthorised),

    CoreSettingsPage("portals", "general", "setting.title", "setting.description",
      "access/portaladmin.do", portletWebService.canAdminister),

  )

  def anyEditable = allSettings.exists(_.isEditable)
}
