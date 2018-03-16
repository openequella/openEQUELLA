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

package com.tle.web.template

import com.tle.common.institution.CurrentInstitution
import com.tle.common.settings.standard.AutoLogin
import com.tle.common.usermanagement.user.{CurrentUser, UserState}
import com.tle.core.db.RunWithDB
import com.tle.core.i18n.LocaleLookup
import com.tle.core.plugins.PluginTracker
import com.tle.legacy.LegacyGuice
import com.tle.web.DebugSettings
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.navigation.MenuService
import com.tle.web.resources.ResourcesService
import com.tle.web.sections._
import com.tle.web.sections.events._
import com.tle.web.sections.events.js.BookmarkAndModify
import com.tle.web.sections.jquery.JQueryStatement
import com.tle.web.sections.jquery.libraries.JQueryCore
import com.tle.web.sections.js.JSUtils
import com.tle.web.sections.js.generic.expression.{ArrayExpression, ObjectExpression}
import com.tle.web.sections.js.generic.function.IncludeFile
import com.tle.web.sections.render._
import com.tle.web.settings.UISettings
import com.tle.web.template.Decorations.MenuMode
import com.tle.web.template.section.HelpAndScreenOptionsSection
import io.circe.generic.auto._

import scala.collection.JavaConverters._

case class ReactPageModel(getReactScript: String)

object RenderNewTemplate {
  val r = ResourcesService.getResourceHelper(getClass)

  val reactTemplate = r.url("reactjs/index.js")

  val NewLayoutKey = "NEW_LAYOUT"

  def isNewLayout(info: SectionInfo): Boolean = {
    Option(info.getAttribute(NewLayoutKey)).getOrElse {
      val paramOverride = Option(info.getRequest.getParameter("old")).map(!_.toBoolean)
      val sessionOverride = paramOverride.fold(Option(LegacyGuice.userSessionService.getAttribute[Boolean](NewLayoutKey))) {
        newUI => LegacyGuice.userSessionService.setAttribute(NewLayoutKey, newUI)
          Some(newUI)
      }
      val newLayout = sessionOverride.getOrElse {
        RunWithDB.executeIfInInstitution(UISettings.cachedUISettings).getOrElse(UISettings.defaultSettings).newUI.enabled
      }
      info.setAttribute(NewLayoutKey, newLayout)
      newLayout
    }
  }

  def userObj(state: UserState) : ObjectExpression = {
    val prefsEditable = !(state.isSystem || state.isGuest) && !(state.wasAutoLoggedIn &&
      LegacyGuice.configService.getProperties(new AutoLogin).isEditDetailsDisallowed)
    new ObjectExpression(
      "id", state.getUserBean.getUniqueID,
      "guest", java.lang.Boolean.valueOf(state.isGuest),
      "autoLogin", java.lang.Boolean.valueOf(state.wasAutoLoggedIn),
      "prefsEditable", java.lang.Boolean.valueOf(prefsEditable)
    )
  }


  def renderHtml(viewFactory: FreemarkerFactory, context: RenderEventContext,
                 tempResult: TemplateResult, menuService: MenuService): SectionResult = {

    case class TemplateScript(getScriptUrl : String,  getRenderJs: ObjectExpression, getTemplate: TemplateResult)

    context.preRender(JQueryCore.PRERENDER)
    context.preRender(new IncludeFile(s"api/language/bundle/${LocaleLookup.selectLocale.getLocale.toLanguageTag}/bundle.js"))

    val decs = Decorations.getDecorations(context)
    val htmlVals = if (!decs.isSinglePageApp) {
      val precontext = context.getPreRenderContext
      if (DebugSettings.isAutoTestMode) precontext.preRender(RenderTemplate.AUTOTEST_JS)
      precontext.preRender(RenderTemplate.STYLES_CSS)
      precontext.preRender(RenderTemplate.CUSTOMER_CSS)

      val _bodyResult = tempResult.getNamedResult(context, "body")
      val unnamedResult = tempResult.getNamedResult(context, "unnamed")
      val bodyResult = CombinedRenderer.combineResults(_bodyResult, unnamedResult)

      val bodyTag = context.getBody
      if (!decs.isExcludeForm) {
        val formTag = context.getForm
        formTag.addReadyStatements(new JQueryStatement(formTag,
          "bind('submit', function(){if (!g_bSubmitting) return false; })"))

        formTag.setNestedRenderable(bodyResult)
        bodyTag.setNestedRenderable(formTag)
      } else {
        bodyTag.setNestedRenderable(bodyResult)
      }
      val hasoMap = HelpAndScreenOptionsSection.getContent(context).asScala
      val scrops = hasoMap.get("screenoptions").map(bbr => SectionUtils.renderToString(context, bbr.getRenderable))
      val bodyHtml = SectionUtils.renderToString(context, bodyTag)
      val htmlMap = new ObjectExpression("body", bodyHtml)
      scrops.foreach(htmlMap.put("so", _))
      htmlMap
    } else null

    val title = Option(decs.getTitle).map(_.getText).getOrElse("")
    val menuValues = menuOptions(context, menuService)
    val renderData = new ObjectExpression("baseResources", r.url(""),
      "newUI", java.lang.Boolean.TRUE, "html", htmlVals, "title", title,
      "user", userObj(CurrentUser.getUserState),
      "menuItems", new ArrayExpression(JSUtils.convertExpressions(menuValues.toSeq: _*)))
    viewFactory.createResultWithModel("layouts/outer/react.ftl", TemplateScript(reactTemplate, renderData, tempResult))
  }

  private val GUEST_FILTER = new PluginTracker.ParamFilter("enabledFor", "guest")
  private val SERVER_ADMIN_FILTER = new PluginTracker.ParamFilter("enabledFor", "serverAdmin")
  private val LOGGED_IN_FILTER = new PluginTracker.ParamFilter("enabledFor", true, "loggedIn")

  def menuOptions(context: RenderEventContext, menuService: MenuService): Iterable[ArrayExpression] = {
    val decorations = Decorations.getDecorations(context)
    val menuMode = decorations.getMenuMode
    if (menuMode == MenuMode.HIDDEN) Iterable.empty
    else {
      val contributors = menuService.getContributors
      val filter = if (CurrentInstitution.get == null) SERVER_ADMIN_FILTER
      else if (CurrentUser.isGuest) GUEST_FILTER
      else LOGGED_IN_FILTER
      contributors.getExtensions(filter).asScala.flatMap { ext =>
        contributors.getBeanByExtension(ext).getMenuContributions(context).asScala
      }.groupBy(_.getGroupPriority).toSeq.sortBy(_._1).map {
        case (_, links) =>
          val menuLinks = links.sortBy(_.getLinkPriority).map { mc =>
            val menuLink = mc.getLink
            val href = Option(menuLink.getBookmark).getOrElse(
              new BookmarkAndModify(context, menuLink.getHandlerMap.getHandler("click").getModifier)).getHref
            new ObjectExpression("title", menuLink.getLabelText, "href", href, "systemIcon", mc.getSystemIcon)
          }.asJava
          new ArrayExpression(menuLinks)
      }
    }
  }
}
