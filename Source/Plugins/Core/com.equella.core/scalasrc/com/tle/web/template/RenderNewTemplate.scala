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

import com.tle.common.i18n.{CurrentLocale, LocaleUtils}
import com.tle.common.institution.CurrentInstitution
import com.tle.common.settings.standard.AutoLogin
import com.tle.common.usermanagement.user.{CurrentUser, UserState}
import com.tle.core.db.RunWithDB
import com.tle.core.i18n.{CoreStrings, LocaleLookup}
import com.tle.core.plugins.PluginTracker
import com.tle.legacy.LegacyGuice
import com.tle.web.DebugSettings
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.navigation.MenuService
import com.tle.web.resources.ResourcesService
import com.tle.web.sections._
import com.tle.web.sections.equella.ScalaSectionRenderable
import com.tle.web.sections.equella.layout.OneColumnLayout
import com.tle.web.sections.events._
import com.tle.web.sections.events.js.BookmarkAndModify
import com.tle.web.sections.jquery.JQueryStatement
import com.tle.web.sections.jquery.libraries.JQueryCore
import com.tle.web.sections.js.JSUtils
import com.tle.web.sections.js.generic.expression.{ArrayExpression, ObjectExpression}
import com.tle.web.sections.js.generic.function.IncludeFile
import com.tle.web.sections.render._
import com.tle.web.sections.standard.model.HtmlLinkState
import com.tle.web.sections.standard.renderers.{DivRenderer, LinkRenderer, SpanRenderer}
import com.tle.web.settings.UISettings
import com.tle.web.template.Decorations.MenuMode
import com.tle.web.template.section.HelpAndScreenOptionsSection
import io.circe.generic.auto._

import scala.collection.JavaConverters._

case class ReactPageModel(getReactScript: String)

object RenderNewTemplate {
  val r = ResourcesService.getResourceHelper(getClass)

  val reactTemplate = r.url("reactjs/index.js")

  val bundleJs = new PreRenderable {
    override def preRender(info: PreRenderContext): Unit =
      new IncludeFile(s"api/language/bundle/${LocaleLookup.selectLocale.getLocale.toLanguageTag}/bundle.js").preRender(info)
  }


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

  case object HeaderSection extends ScalaSectionRenderable({
    writer =>
      writer.getInfo() match {
        case src: StandardRenderContext =>
          writer.writeTag("base", "href", writer.getPathGenerator.getBaseHref(writer).toString)
          src.getJsFiles.asScala.foreach {
            s => writer.writeTag("script", "src", s)
              writer.endTag("script")
          }
      }
  })

  def renderNewHtml(context: RenderEventContext, viewFactory: FreemarkerFactory): SectionResult =
  {
    val renderData = new ObjectExpression("baseResources", r.url(""),
      "newUI", java.lang.Boolean.TRUE, "title", "title",
      "user", userObj(CurrentUser.getUserState),
      "menuMode", "",
      "fullscreenMode", "false",
      "hideAppBar", "false",
      "menuItems", new ArrayExpression())

    context.preRender(JQueryCore.PRERENDER)
    if (Option(context.getRequest.getHeader("User-Agent")).exists(_.contains("Trident")))
    {
      context.getPreRenderContext.addJs("https://cdn.polyfill.io/v2/polyfill.min.js?features=es6")
    }
    context.preRender(bundleJs)
    val tempResult = new GenericTemplateResult()
    tempResult.addNamedResult("header", HeaderSection)
    viewFactory.createResultWithModel("layouts/outer/react.ftl",
      TemplateScript(reactTemplate, renderData, tempResult, ""))
  }

  case class TemplateScript(getScriptUrl : String,  getRenderJs: ObjectExpression, getTemplate: TemplateResult, htmlAttributes: String)
  {
    def getLang = LocaleUtils.toHtmlLang(CurrentLocale.getLocale)
    def isRightToLeft = CurrentLocale.isRightToLeft
    def getHtmlAttrs = htmlAttributes
  }

  def renderHtml(viewFactory: FreemarkerFactory, context: RenderEventContext,
                 tempResult: TemplateResult, menuService: MenuService, htmlAttributes: String): SectionResult = {


    context.preRender(JQueryCore.PRERENDER)
    if (Option(context.getRequest.getHeader("User-Agent")).exists(_.contains("Trident")))
    {
      context.getPreRenderContext.addJs("https://cdn.polyfill.io/v2/polyfill.min.js?features=es6")
    }
    context.preRender(bundleJs)

    val decs = Decorations.getDecorations(context)
    val htmlVals = if (!decs.isSinglePageApp) {
      val precontext = context.getPreRenderContext
      if (DebugSettings.isAutoTestMode) precontext.preRender(RenderTemplate.AUTOTEST_JS)
      precontext.preRender(RenderTemplate.STYLES_CSS)

      def wrapBody(body: SectionRenderable) : SectionRenderable = {
        val citag = new TagState("content-inner").addClass[TagState](decs.getPageLayoutDisplayClass)
        val cbtag = new TagState("content-body").addClasses[TagState](decs.getContentBodyClasses)

        new DivRenderer(citag, new DivRenderer(cbtag, body))
      }

      val _bodyResult = CombinedRenderer.combineResults(tempResult.getNamedResult(context, "body"),
        tempResult.getNamedResult(context, "unnamed"))
      val bodyResult = if (decs.getMenuMode != MenuMode.HIDDEN) wrapBody(_bodyResult) else _bodyResult

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
      val htmlMap = new ObjectExpression()
      val hasoMap = HelpAndScreenOptionsSection.getContent(context).asScala
      val scrops = hasoMap.get("screenoptions").map(bbr => SectionUtils.renderToString(context, bbr.getRenderable))
      val upper = Option(SectionUtils.renderToString(context,
        tempResult.getNamedResult(context, OneColumnLayout.UPPERBODY))).filter(_.nonEmpty)
      renderCrumbs(context, decs).foreach(c => htmlMap.put("crumbs", SectionUtils.renderToString(context, c)))
      val bodyHtml = SectionUtils.renderToString(context, bodyTag)
      htmlMap.put("body", bodyHtml)
      scrops.foreach(htmlMap.put("so", _))
      upper.foreach(htmlMap.put(OneColumnLayout.UPPERBODY, _))
      htmlMap
    } else null

    val title = Option(decs.getTitle).map(_.getText).getOrElse("")
    val menuValues = menuOptions(context, menuService)
    val renderData = new ObjectExpression("baseResources", r.url(""),
      "newUI", java.lang.Boolean.TRUE, "html", htmlVals, "title", title,
      "user", userObj(CurrentUser.getUserState),
      "menuMode", decs.getMenuMode.toString,
      "fullscreenMode", decs.isFullscreen.toString,
      "hideAppBar", java.lang.Boolean.valueOf(!(decs.isBanner || !decs.isMenuHidden || decs.isContent)),
      "menuItems", new ArrayExpression(JSUtils.convertExpressions(menuValues.toSeq: _*)))
    viewFactory.createResultWithModel("layouts/outer/react.ftl",
      TemplateScript(reactTemplate, renderData, tempResult, htmlAttributes))

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
            new ObjectExpression("title", menuLink.getLabelText, "href", href, "systemIcon", mc.getSystemIcon, "route", mc.getRoute)
          }.asJava
          new ArrayExpression(menuLinks)
      }
    }
  }

  def renderCrumbs(context: RenderEventContext, d: Decorations): Option[SectionRenderable] = {
    val bc = Breadcrumbs.get(context)
    if (d.isForceBreadcrumbsOn || (d.isBreadcrumbs && !bc.getLinks.isEmpty)) Option {
      val ct = new TagState("breadcrumb-inner")
      val allCrumbs = bc.getLinks.asScala.map {
        case ls: HtmlLinkState => new LinkRenderer(ls)
        case o => new TagRenderer("span", o)
      } :+ Option(bc.getForcedLastCrumb).getOrElse(d.getTitle)
      new SpanRenderer(ct, new DelimitedRenderer(" " + CoreStrings.text("breadcrumb.separator") + " ", allCrumbs: _*))
    } else None
  }
}
