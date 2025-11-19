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

package com.tle.web.template

import java.util.concurrent.ConcurrentHashMap
import com.tle.common.i18n.{CurrentLocale, LocaleUtils}
import com.tle.common.settings.standard.QuickContributeAndVersionSettings
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.i18n.LocaleLookup
import com.tle.core.plugins.AbstractPluginService
import com.tle.legacy.LegacyGuice
import com.tle.web.DebugSettings
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.resources.ResourcesService
import com.tle.web.sections._
import com.tle.web.sections.equella.ScalaSectionRenderable
import com.tle.web.sections.events._
import com.tle.web.sections.jquery.libraries.JQueryCore
import com.tle.web.sections.js.generic.expression.ObjectExpression
import com.tle.web.sections.js.generic.function.IncludeFile
import com.tle.web.sections.render._
import com.tle.web.selection.section.RootSelectionSection
import com.tle.web.integration.IntegrationSection
import com.tle.web.sections.render.CssInclude.{Priority, include}
import com.tle.web.selection.section.RootSelectionSection.Layout
import com.tle.web.settings.{AnalyticsSettings, UISettings}
import javax.servlet.http.HttpServletRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import scala.jdk.CollectionConverters._

case class ReactPageModel(getReactScript: String)

object RenderNewTemplate {

  val r            = ResourcesService.getResourceHelper(getClass)
  val DisableNewUI = "DISABLE_NEWUI"
  val SetupJSKey   = "setupJSData"
  val ReactHtmlKey = "reactJSHtml"

  val htmlBundleCache = new ConcurrentHashMap[String, (PreRenderable, Document)]()

  val bundleJs = new PreRenderable {
    override def preRender(info: PreRenderContext): Unit = {
      new IncludeFile(
        s"api/language/bundle/${LocaleLookup.selectLocale.getLocale.toLanguageTag}/bundle.js"
      )
        .preRender(info)
      new IncludeFile(s"api/theme/theme.js").preRender(info)
    }
  }

  def parseEntryHtml(filename: String): (PreRenderable, Document) = {
    val reactJsPath = "reactjs/"
    val inpStream   = getClass.getResourceAsStream(s"/web/$reactJsPath$filename")
    if (inpStream == null) sys.error(s"Failed to find $filename react html bundle")
    val htmlDoc = Jsoup.parse(inpStream, "UTF-8", "")

    inpStream.close()

    /** Update URL related attribute of HTML elements specified by tagName
      *
      * @param tagName
      *   the name of the tag to search for (e.g., "script", "link")
      * @param attrKey
      *   the url related attribute to update (e.g., "src", "href")
      */
    def updateElementUrl(tagName: String, attrKey: String) = {
      htmlDoc.getElementsByTag(tagName).asScala.foreach { e =>
        if (e.hasAttr(attrKey)) {
          e.attr(attrKey, r.url(reactJsPath + e.attr(attrKey)))
        }
      }
    }
    // update path for JS files
    updateElementUrl("script", "src")
    // update path for CSS files
    updateElementUrl("link", "href")

    // add head resources
    val prerender: PreRenderable = info => {
      info.preRender(JQueryCore.PRERENDER)
      info.preRender(bundleJs)
      addKalturaCss(info)
      info.addCss(RenderTemplate.CUSTOMER_CSS)
      // add head content from htmlDoc (new UI) for new UI content which is not rendered by react.ftl,
      // such as settings page in old UI.
      // `HeaderMarkup` will be added into `header.ftl`.
      info.addHeaderMarkup(htmlDoc.head().html())
    }
    (prerender, htmlDoc)
  }

  def addKalturaCss(info: PreRenderContext): Unit = {
    val kalturaPluginId = "com.tle.web.wizard.controls.kaltura"
    val pluginService   = AbstractPluginService.get()
    if (pluginService.isActivated(kalturaPluginId)) {
      info.addCss(
        include(
          ResourcesService
            .getResourceHelper(kalturaPluginId)
            .url("js/UploadControlEntry.css")
        ).priority(Priority.LOWEST).make()
      )
    }
  }

  val NewLayoutKey = "NEW_LAYOUT"

  // Check if new UI is enabled.
  def isNewUIEnabled: Boolean = {
    UISettings.getUISettings.newUI.enabled
  }

  // Check if the viewing a resource via integration.
  def isViewingItemFromIntegration(req: HttpServletRequest): Boolean = {
    req.getServletPath == "/integ" && req.getPathInfo.startsWith("/gen/")
  }

  // Check if new Search page is enabled.
  def isNewSearchPageEnabled: Boolean = {
    UISettings.getUISettings.isNewSearchActive
  }

  // Check if New UI is being used, but there is no guarantee that New UI is enabled.
  // An example is when Old UI is turned on, users will see New UI if they open pages
  // that are only available in New UI such as the Facet settings page.
  def isNewLayout(info: SectionInfo): Boolean = {
    Option(info.getAttribute(NewLayoutKey)).getOrElse {
      val paramOverride = Option(info.getRequest.getParameter("old")).map(!_.toBoolean)
      val sessionOverride = paramOverride.fold(
        Option(LegacyGuice.userSessionService.getAttribute[Boolean](NewLayoutKey))
      ) { newUI =>
        LegacyGuice.userSessionService.setAttribute(NewLayoutKey, newUI)
        Some(newUI)
      }
      val newLayout = sessionOverride.getOrElse(isNewUIEnabled)
      info.setAttribute(NewLayoutKey, newLayout)
      newLayout
    }
  }

  case object HeaderSection
      extends ScalaSectionRenderable({ writer =>
        writer.getInfo() match {
          case src: StandardRenderContext =>
            src.getJsFiles.asScala.foreach { s =>
              writer.writeTag("script", "src", s)
              writer.endTag("script")
            }
            src.getCssFiles.asScala.foreach { s: CssInclude =>
              writer
                .writeTag("link", "rel", "stylesheet", "type", "text/css", "href", s.getHref(src))
            }
        }
      })

  private def getSelectionSessionInfo(context: RenderEventContext): Option[ObjectExpression] = {
    val currentSession = LegacyGuice.selectionService.get().getCurrentSession(context)
    Option(currentSession).map(session => {
      val layout = session.getLayout match {
        case Layout.COURSE => "coursesearch"
        case Layout.NORMAL => "search"
        case Layout.SKINNY => "skinnysearch"
      }

      val rootSelectionSection: RootSelectionSection =
        context.lookupSection(classOf[RootSelectionSection])
      val stateId = rootSelectionSection.getSessionId(context)

      val integrationSection: IntegrationSection =
        context.lookupSection(classOf[IntegrationSection])
      val integId = Option(integrationSection).map(_.getStateId(context)).orNull

      def isSelectSummaryButtonDisabled: Boolean = {
        // 'Select summary page' button can be disabled by both oEQ setting and LMS (at least Moodle)
        // oEQ plugin setting - Restrict selections.
        // Three booleans: selectItem, selectAttachments and selectPackage, defined in 'SelectionSession.java',
        // provide the access to the LMS setting.

        def getOeqSetting: Boolean =
          LegacyGuice.configService
            .getProperties(new QuickContributeAndVersionSettings())
            .isButtonDisable

        Option(integrationSection) match {
          case Some(_) =>
            session.isSelectItem match {
              // When Restrict selections is either "Attachments only" or "Package only", the button is always disabled.
              case false => true
              // When Restrict selections is "Items only", the button is always NOT disabled.
              case true if !session.isSelectAttachments && !session.isSelectPackage => false
              // When Restrict selections is "No restrictions", whether disabled or not depends on the oEQ setting.
              case _ => getOeqSetting
            }
          case None =>
            getOeqSetting // Not in an Integration, whether disabled or not depends on the oEQ setting.
        }
      }

      val selectionSessionInfo = new ObjectExpression
      selectionSessionInfo.put("stateId", stateId)
      selectionSessionInfo.put("integId", integId)
      selectionSessionInfo.put("layout", layout)
      selectionSessionInfo.put("isSelectSummaryButtonDisabled", isSelectSummaryButtonDisabled)
      selectionSessionInfo
    })
  }

  def renderNewHtml(context: RenderEventContext, viewFactory: FreemarkerFactory): SectionResult = {
    val req = context.getRequest
    val _renderData =
      new ObjectExpression(
        "baseResources",
        r.url(""),
        "newUI",
        java.lang.Boolean.valueOf(isNewUIEnabled),
        "autotestMode",
        java.lang.Boolean.valueOf(DebugSettings.isAutoTestMode),
        "newSearch",
        java.lang.Boolean.valueOf(isNewSearchPageEnabled),
        "selectionSessionInfo",
        getSelectionSessionInfo(context).orNull,
        "viewedFromIntegration",
        java.lang.Boolean.valueOf(isViewingItemFromIntegration(req)),
        "analyticsId",
        AnalyticsSettings.getAnalyticsId.orNull,
        "hasAuthenticated",
        CurrentUser.getUserState.isAuthenticated
      )
    val renderData =
      Option(req.getAttribute(SetupJSKey).asInstanceOf[ObjectExpression => ObjectExpression])
        .map(_.apply(_renderData))
        .getOrElse(_renderData)

    val htmlPage =
      Option(req.getAttribute(ReactHtmlKey).asInstanceOf[String]).getOrElse("index.html")
    val (scriptPreRender, html) =
      if (DebugSettings.isDevMode) parseEntryHtml(htmlPage)
      else htmlBundleCache.computeIfAbsent(htmlPage, parseEntryHtml)
    context.preRender(scriptPreRender)
    renderReact(context, viewFactory, renderData, html.head().html(), html.body().toString)
  }

  private def isInternetExplorer(request: HttpServletRequest): Boolean =
    Option(request.getHeader("User-Agent")).exists(_.contains("Trident"))

  private def renderReact(
      context: RenderEventContext,
      viewFactory: FreemarkerFactory,
      renderData: ObjectExpression,
      head: String,
      body: String
  ): SectionResult = {
    if (DebugSettings.isAutoTestMode) {
      context.preRender(RenderTemplate.AUTOTEST_JS)
    }
    val tempResult = new GenericTemplateResult()
    tempResult.addNamedResult("header", HeaderSection)
    val template =
      if (isInternetExplorer(context.getRequest)) "layouts/outer/unsupportedbrowser.ftl"
      else "layouts/outer/react.ftl"

    viewFactory.createResultWithModel(
      template,
      TemplateScript(head, body, renderData, tempResult, "")
    )

  }

  case class TemplateScript(
      getHead: String,
      getBody: String,
      getRenderJs: ObjectExpression,
      getTemplate: TemplateResult,
      htmlAttributes: String
  ) {
    def getLang = LocaleUtils.toHtmlLang(CurrentLocale.getLocale)

    def isRightToLeft = CurrentLocale.isRightToLeft

    def getHtmlAttrs = htmlAttributes
  }

}
