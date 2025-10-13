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

package com.tle.web.api

import com.dytech.common.io.DevNullWriter
import com.dytech.edge.web.WebConstants
import com.tle.beans.item.{ItemId, ItemKey, ItemTaskId}
import com.tle.common.institution.CurrentInstitution
import com.tle.common.security.SecurityConstants
import com.tle.common.settings.standard.AutoLogin
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.i18n.CoreStrings
import com.tle.core.notification.standard.indexer.NotificationSearch
import com.tle.core.plugins.{AbstractPluginService, PluginTracker}
import com.tle.core.security.ACLChecks.hasAcl
import com.tle.core.workflow.freetext.TaskListSearch
import com.tle.legacy.LegacyGuice
import com.tle.legacy.LegacyGuice.accessibilityModeService
import com.tle.web.api.LegacyContentController.getBookmarkState
import com.tle.web.sections._
import com.tle.web.sections.ajax.{AjaxGenerator, AjaxRenderContext}
import com.tle.web.sections.equella.js.StandardExpressions
import com.tle.web.sections.events._
import com.tle.web.sections.events.js.{BookmarkAndModify, JSHandler}
import com.tle.web.sections.generic.InfoBookmark
import com.tle.web.sections.header.{InfoFormAction, MutableHeaderHelper}
import com.tle.web.sections.jquery.libraries.JQueryCore
import com.tle.web.sections.js.JSStatements
import com.tle.web.sections.js.generic.function.{AnonymousFunction, ExternallyDefinedFunction}
import com.tle.web.sections.js.generic.statement.{FunctionCallStatement, StatementBlock}
import com.tle.web.sections.registry.{
  AbstractSectionsController,
  ExceptionHandlerMatch,
  ExtensionExceptionHandlerMatch,
  SectionsControllerImpl
}
import com.tle.web.sections.render._
import com.tle.web.sections.standard.model.HtmlLinkState
import com.tle.web.sections.standard.renderers.{DivRenderer, LinkRenderer, SpanRenderer}
import com.tle.web.template.Decorations.MenuMode
import com.tle.web.template.section.{HelpAndScreenOptionsSection, MenuContributor}
import com.tle.web.template.{Breadcrumbs, Decorations, RenderTemplate}
import com.tle.web.viewable.servlet.ItemServlet
import com.tle.web.viewable.{NewDefaultViewableItem, PreviewableItem}
import com.tle.web.viewitem.section.RootItemFileSection
import io.lemonlabs.uri.{Path => _, _}
import io.swagger.annotations.{Api, ApiOperation}
import org.slf4j.LoggerFactory
import java.net.URI
import java.util
import java.util.Collections
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.ws.rs._
import javax.ws.rs.core.Response.{ResponseBuilder, Status}
import javax.ws.rs.core.{CacheControl, Context, Response, UriInfo}
import scala.jdk.CollectionConverters._
import scala.collection.mutable

case class InternalRedirect(route: String, userUpdated: Boolean)

case class ExternalRedirect(href: String)

case class MenuItem(
    title: String,
    href: Option[String],
    systemIcon: Option[String],
    route: Option[String],
    iconUrl: Option[String],
    newWindow: Boolean
)

case class LegacyContent(
    html: Map[String, String],
    css: Option[Iterable[String]],
    js: Iterable[String],
    script: String,
    state: Map[String, Array[String]],
    title: String,
    metaTags: String,
    menuMode: String,
    fullscreenMode: String,
    hideAppBar: Boolean,
    userUpdated: Boolean,
    preventUnload: Boolean,
    noForm: Boolean,
    accessibilityMode: Boolean
)

case class ItemCounts(tasks: Int, notifications: Int)

/** Details about the current user.
  *
  * @param id
  *   Identifier of the user.
  * @param username
  *   Username.
  * @param firstName
  *   First name.
  * @param lastName
  *   Last name.
  * @param emailAddress
  *   Email address.
  * @param accessibilityMode
  *   `true` if the user has accessibility mode enabled (some controls will be rendered in a more
  *   screen-reader friendly manner)
  * @param autoLoggedIn
  *   `true` if the user was automatically logged in.
  * @param guest
  *   `true` if the user is a guest user.
  * @param prefsEditable
  *   `true` if the user can edit their preferences.
  * @param menuGroups
  *   A list of menu groups available that the user have permission to access.
  * @param counts
  *   The user's item counts (tasks, notifications), or `None` if the user is a guest.
  * @param canDownloadSearchResult
  *   `true` if the user can download search results.
  * @param roles
  *   UUIDs of the roles assigned to the user - as well as `TLE_LOGGED_IN_USER_ROLE` where
  *   applicable.
  * @param scrapbookEnabled
  *   `true` if access to Scrapbook is enabled.
  * @param isSystem
  *   `true` if the user is a system user (mainly TLE_ADMINISTRATOR).
  */
case class CurrentUserDetails(
    id: String,
    username: String,
    firstName: String,
    lastName: String,
    emailAddress: String,
    accessibilityMode: Boolean,
    autoLoggedIn: Boolean,
    guest: Boolean,
    prefsEditable: Boolean,
    menuGroups: Iterable[Iterable[MenuItem]],
    counts: Option[ItemCounts],
    canDownloadSearchResult: Boolean,
    roles: Iterable[String],
    scrapbookEnabled: Boolean,
    isSystem: Boolean
)

object LegacyContentController extends AbstractSectionsController with SectionFilter {

  import LegacyGuice.urlService

  def isClientPath(relUrl: RelativeUrl): Boolean = {
    val UUID = "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"
    // This regex matches the relative url of Item Summary page
    // For example 'items/95075bdd-4049-46ab-a1aa-043902e239a3/3/'
    // The last forward slash does not exist in some cases

    // This regex also works for those that have query params.
    // For example, in Selection Session, the URL would be
    // 'items/95075bdd-4049-46ab-a1aa-043902e239a3/3/?_sl.stateId=1&_int.id=2'.

    val itemSummaryUrlPattern = ("items/" + UUID + "/\\d+/?\\??.+").r

    // This regex explicitly matches the relative Url of logon
    // For example, 'logon.do' or 'logon.do?.page=home.do'
    val logonUrlPattern = "logon\\.do\\??.*".r

    // This regex matches the relative Urls of other Legacy pages
    // For example, 'home.do' or 'access/runwizard.do?.wizid...'
    val otherUrlPattern = ".+\\.do\\??.*".r

    // Matches all the new UI pages such as page/search?q=abc and page/hierarchy/uuid
    // UUID is optional so put it in a non-capturing group.
    val newUIPages = ("page/\\w+(?:/" + UUID + ")?.*").r

    relUrl.toString() match {
      case logonUrlPattern()       => false
      case itemSummaryUrlPattern() => true
      case otherUrlPattern()       => true
      case newUIPages()            => true
      case _                       => false
    }
  }

  def internalRoute(uri: String): Option[String] = {
    relativeURI(uri).filter(isClientPath).map(r => "/" + r.toString())
  }

  override lazy val getExceptionHandlers: util.Collection[ExceptionHandlerMatch] = {
    val disableHandlers = Set("ajaxExceptionHandler", "defaultEquellaErrorHandler")
    val tracker         = SectionsControllerImpl.createExceptionTracker(AbstractPluginService.get())
    tracker.getExtensions.asScala
      .filter(e => !disableHandlers.contains(e.getId))
      .map(e => new ExtensionExceptionHandlerMatch(e, tracker): ExceptionHandlerMatch)
      .asJavaCollection
  }

  def relativeURI(uri: String): Option[RelativeUrl] = {
    val baseUrl   = AbsoluteUrl.parse(urlService.getBaseInstitutionURI.toString)
    val Host      = baseUrl.host
    val Port      = baseUrl.port
    val basePaths = baseUrl.path.parts.filter(_.length > 0)
    val parsedUri = Url.parse(uri)

    parsedUri match {
      case r @ RelativeUrl(path: RootlessPath, _, _) => Some(r)
      case AbsoluteUrl(_, Authority(_, Host, Port), path, q, f)
          if path.parts.startsWith(basePaths) =>
        Some(RelativeUrl(RootlessPath(path.parts.drop(basePaths.length)), q, f))
      case _ => None
    }
  }

  override val getSectionFilters: util.List[SectionFilter] = {
    val sf = new util.ArrayList[SectionFilter]()
    sf.add(this)
    sf.addAll(LegacyGuice.sectionsController.asInstanceOf[SectionsControllerImpl].getSectionFilters)
    sf.remove(LegacyGuice.templateFilter)
    sf
  }

  override def filter(info: MutableSectionInfo): Unit = {
    info.setAttribute(SectionInfo.KEY_BASE_HREF, baseUri(info.getRequest))
    info.setAttribute(
      classOf[EventAuthoriser],
      new EventAuthoriser {
        override def checkAuthorisation(info: SectionInfo): Unit = {}

        override def addToBookmark(
            info: SectionInfo,
            bookmarkState: util.Map[String, Array[String]]
        ): Unit =
          bookmarkState.put(RenderTemplate.XSRF_PARAM, Array[String](CurrentUser.getSessionID))
      }
    )
  }

  def baseUri(req: HttpServletRequest): URI = urlService.getBaseUriFromRequest(req)

  val RedirectedAttr = "REDIRECTED"

  val DISABLE_LEGACY_CSS = "DISABLE_LEGACY_CSS"
  val SKIP_BOOTSTRAP     = "skip_bootstrap"

  override protected def getTreeForPath(path: String): SectionTree =
    LegacyGuice.treeRegistry.getTreeForPath(path)

  def getBookmarkState(info: SectionInfo, event: BookmarkEvent): Map[String, Array[String]] = {
    val q = new InfoBookmark(info, event).getBookmarkParams
    q.asScala.toMap
  }

  def prepareJSContext(info: MutableSectionInfo): StandardRenderContext = {
    val context = info.getRootRenderContext
    val helper  = context.getHelper.asInstanceOf[MutableHeaderHelper]
    helper.setElementFunction(StandardExpressions.ELEMENT_FUNCTION)

    val formTag = context.getForm
    if (helper.getFormExpression == null) {
      formTag.setId(StandardExpressions.FORM_NAME)
      helper.setFormExpression(StandardExpressions.FORM_EXPRESSION)
    }
    RenderTemplate.addFormSubmitBinding(formTag)
    if (!helper.isSubmitFunctionsSet) {
      helper.setSubmitFunctions(
        new ExternallyDefinedFunction("EQ.event"),
        new ExternallyDefinedFunction("EQ.eventnv"),
        new ExternallyDefinedFunction("EQ.event"),
        new ExternallyDefinedFunction("EQ.eventnv")
      )
    }
    helper.setTriggerEventFunction(StandardExpressions.TRIGGER_EVENT_FUNCTION)
    val standardContext = context.getAttributeForClass(classOf[StandardRenderContext])
    standardContext.setBindFunction(StandardExpressions.BIND_EVENT_FUNCTION)
    standardContext.setBindW3CFunction(StandardExpressions.BIND_W3C_FUNCTION)
    standardContext.preRender(RenderTemplate.STYLES_CSS)
    standardContext
  }

  override def renderFromRoot(info: SectionInfo): Unit = {
    if (info.getAttributeForClass(classOf[AjaxRenderContext]) == null) {
      prepareJSContext(info.getAttributeForClass(classOf[MutableSectionInfo]))
      val context = info.getRootRenderContext
      Option(context.getRenderedBody)
        .map(b => context.getRootResultListener.returnResult(b, null))
        .getOrElse {
          super.renderFromRoot(info)
        }
    }
  }

  override def forwardToUrl(info: SectionInfo, link: String, code: Int): Unit = {
    info.setRendered()
    info.getRequest.setAttribute(RedirectedAttr, link)
  }
}

@Api("Legacy content")
@Path("content")
@Produces(value = Array("application/json"))
class LegacyContentApi {
  val LOGGER = LoggerFactory.getLogger(classOf[LegacyContentApi])

  def parsePath(path: String): (String, MutableSectionInfo => MutableSectionInfo) = {
    // Parse the given URL and return a Tuple2 where First is ItemKey and Second is a string representing
    // file name of the content to be viewed.
    def parseItemViewerPath(p: String): (ItemKey, Option[String]) = {
      // Regex for the URL of viewing content which has 4 groups.
      // 1. Item UUID
      // 2. Item version
      // 3. Content file name which is 'viewcontent/uuid' or 'viewims.jsp'.
      // 4. additional query strings

      // Examples:
      // For normal content: ef1c6d7d-7aec-4743-9126-f847913de3f2/1/viewcontent/42eead4b-cfac-46df-8927-f8e58f4cd491
      // For IMS package: 677a4bbc-defc-4dc1-b68e-4e2473b66a6a/1/viewims.jsp?viewMethod=download
      val viewContentPattern = "(.+)/(\\d)/(viewcontent/.+|viewims.jsp)(.*)".r

      p match {
        case viewContentPattern(itemUUID, itemVersion, fileName, _) =>
          (new ItemId(itemUUID, itemVersion.toInt), Option(fileName))
        case _ => (ItemTaskId.parse(p), None)
      }
    }

    def itemViewer(
        p: String,
        f: (SectionInfo, NewDefaultViewableItem) => NewDefaultViewableItem
    ): (String, MutableSectionInfo => MutableSectionInfo) = {
      val (itemId, contentFileName) = parseItemViewerPath(p)
      (
        s"/viewitem/viewitem.do",
        { info: MutableSectionInfo =>
          for {
            section <- Option(info.lookupSection[RootItemFileSection, RootItemFileSection](classOf))
            name    <- contentFileName
            _ = section.getModel(info).setFilename(name)
          } yield ()

          info.setAttribute(
            ItemServlet.VIEWABLE_ITEM,
            f(info, LegacyGuice.viewableItemFactory.createNewViewableItem(itemId))
          )
          info
        }
      )
    }

    path match {
      case ""                          => ("/home.do", identity)
      case p if p.startsWith("items/") => itemViewer(p.substring("items/".length), (_, vi) => vi)
      case p if p.startsWith("preview/") =>
        val itemId = ItemTaskId.parse(p.substring("preview/".length))
        (
          "/viewitem/viewitem.do",
          { info: MutableSectionInfo =>
            val previewableItem =
              LegacyGuice.userSessionService.getAttribute[PreviewableItem](itemId.getUuid)
            if (previewableItem != null) {
              val viewableItem = previewableItem.getViewableItem
              viewableItem.setFromRequest(true)
              info.setAttribute(ItemServlet.VIEWABLE_ITEM, viewableItem)
            }
            info
          }
        )

      case p if p.startsWith("integ/gen/") =>
        itemViewer(
          p.substring("integ/gen/".length),
          { (info, vi) =>
            vi.getState.setIntegrationType("gen")
            val decs = Decorations.getDecorations(info)
            decs.setMenuMode(MenuMode.HIDDEN)
            decs.setBanner(false)
            decs.setContent(true)
            vi
          }
        )
      case p => (s"/$p", identity)
    }
  }

  private val UserIdKey = "InitialUserId"

  def withTreePath(
      _path: String,
      uriInfo: UriInfo,
      req: HttpServletRequest,
      resp: HttpServletResponse,
      params: util.Map[String, Array[String]],
      f: MutableSectionInfo => ResponseBuilder
  ): Response = {
    val (treePath, setupInfo) = parsePath(_path)
    val path                  = s"/${_path}"
    (Option(LegacyGuice.treeRegistry.getTreeForPath(treePath)) match {
      case None => Response.status(404)
      case Some(_) if (CurrentUser.isGuest() && path.startsWith(WebConstants.ACCESS_PATH)) =>
        Response.status(401)
      case Some(tree) => {
        LegacyGuice.userSessionService.reenableSessionUse()
        req.setAttribute(UserIdKey, CurrentUser.getUserID)
        val info =
          setupInfo(LegacyContentController.createInfo(tree, path, req, resp, null, params, null))
        info.setAttribute(
          AjaxGenerator.AJAX_BASEURI,
          uriInfo.getBaseUriBuilder
            .path(classOf[LegacyContentApi])
            .path(classOf[LegacyContentApi], "ajaxCall")
            .build("")
        )
        f(info)
      }
    }).build()
  }

  @GET
  @Path("currentuser")
  @ApiOperation(
    value = "Current user details",
    notes = "Get details of the user for the current session",
    response = classOf[CurrentUserDetails]
  )
  def currentuser(
      @Context req: HttpServletRequest,
      @Context resp: HttpServletResponse
  ): Response = {
    val contributors = LegacyGuice.menuService.getContributors
    val noInst       = CurrentInstitution.get == null
    val (noParam, filterName) =
      if (noInst) (false, "serverAdmin")
      else if (CurrentUser.isGuest) (false, "guest")
      else (true, "loggedIn")

    LegacyGuice.userSessionService.reenableSessionUse()
    val context = LegacyGuice.sectionsController.createInfo("/home.do", req, resp, null, null, null)

    val cu = CurrentUser.getUserState

    val accessibilityMode = LegacyGuice.accessibilityModeService.isAccessibilityMode

    val canDownloadSearchResult: Boolean = hasAcl(SecurityConstants.EXPORT_SEARCH_RESULT)

    val scrapbookEnabled = LegacyGuice.myContentService.isMyContentContributionAllowed

    val prefsEditable = !(cu.isSystem || cu.isGuest) && !(cu.wasAutoLoggedIn &&
      LegacyGuice.configService.getProperties(new AutoLogin).isEditDetailsDisallowed)
    val menuGroups = {
      contributors
        .getExtensions(new PluginTracker.ParamFilter("enabledFor", noParam, filterName))
        .asScala
        .flatMap { ext =>
          contributors.getBeanByExtension(ext).getMenuContributions(context).asScala
        }
        .groupBy(_.getGroupPriority)
        .toSeq
        .sortBy(_._1)
        .map { case (_, links) =>
          links.sortBy(_.getLinkPriority).map { mc =>
            val menuLink = mc.getLink
            val href = Option(menuLink.getBookmark)
              .getOrElse(
                new BookmarkAndModify(
                  context,
                  menuLink.getHandlerMap.getHandler("click").getModifier
                )
              )
              .getHref
            val route   = Option(mc.getRoute).orElse(LegacyContentController.internalRoute(href))
            val iconUrl = if (mc.isCustomImage) Some(mc.getBackgroundImagePath) else None
            MenuItem(
              menuLink.getLabelText,
              if (route.isEmpty) Some(href) else None,
              Option(mc.getSystemIcon),
              route,
              iconUrl,
              "_blank" == menuLink.getTarget
            )
          }
        }
    }
    val counts = if (!cu.isGuest) Option {
      val notificationCount = LegacyGuice.freeTextService.countsFromFilters(
        Collections.singletonList(new NotificationSearch)
      )(0)
      val taskCount = LegacyGuice.freeTextService.countsFromFilters(
        Collections.singletonList(new TaskListSearch)
      )(0)
      ItemCounts(taskCount, notificationCount)
    }
    else None
    val ub           = cu.getUserBean
    val cacheControl = new CacheControl()
    cacheControl.setNoCache(true)
    cacheControl.setNoStore(true)
    cacheControl.setSMaxAge(-1)
    Response
      .ok(
        CurrentUserDetails(
          id = ub.getUniqueID,
          username = ub.getUsername,
          firstName = ub.getFirstName,
          lastName = ub.getLastName,
          emailAddress = ub.getEmailAddress,
          autoLoggedIn = cu.wasAutoLoggedIn(),
          guest = cu.isGuest,
          prefsEditable = prefsEditable,
          menuGroups = menuGroups,
          counts = counts,
          accessibilityMode = accessibilityMode,
          canDownloadSearchResult = canDownloadSearchResult,
          roles = cu.getUsersRoles.asScala,
          scrapbookEnabled = scrapbookEnabled,
          isSystem = cu.isSystem
        )
      )
      .cacheControl(cacheControl)
      .build()
  }

  @POST
  @GET
  @Path("/ajax/{path : .+}")
  def ajaxCall(
      @PathParam("path") _path: String,
      @Context uriInfo: UriInfo,
      @Context req: HttpServletRequest,
      @Context resp: HttpServletResponse
  ): Response = {

    withTreePath(
      _path,
      uriInfo,
      req,
      resp,
      req.getParameterMap,
      { info =>
        info.preventGET()
        LegacyContentController.execute(info)
        renderedResponse(info).getOrElse {
          ajaxResponse(info, info.getAttributeForClass(classOf[AjaxRenderContext]))
        }
      }
    )
  }

  private val LegacyContentKey = "LegacyContent"

  @POST
  @Path("/submit/{path : .+}")
  def submit(
      @PathParam("path") _path: String,
      @Context uriInfo: UriInfo,
      @Context req: HttpServletRequest,
      @Context resp: HttpServletResponse,
      params: mutable.Map[String, Array[String]]
  ): Response = {
    withTreePath(
      _path,
      uriInfo,
      req,
      resp,
      params.asJava,
      { info =>
        info.preventGET()
        info.getRootRenderContext.setRootResultListener(new LegacyResponseListener(info))
        LegacyContentController.execute(info)
        redirectResponse(info)
          .orElse(renderedResponse(info))
          .orElse(
            Option(info.getAttributeForClass(classOf[AjaxRenderContext]))
              .map(arc => ajaxResponse(info, arc))
          )
          .getOrElse {
            info.setRendered()
            Response.ok(req.getAttribute(LegacyContentKey))
          }
      }
    )
  }

  def userChanged(req: HttpServletRequest): Boolean = {
    val idNow  = CurrentUser.getUserID
    val idThen = req.getAttribute(UserIdKey).asInstanceOf[String]
    Option(req.getAttribute(MenuContributor.KEY_MENU_UPDATED)).contains(true) || idNow != idThen
  }

  def redirectResponse(info: MutableSectionInfo): Option[ResponseBuilder] = {
    val req = info.getRequest
    Option(req.getAttribute(LegacyContentController.RedirectedAttr).asInstanceOf[String]).map {
      url =>
        Response.ok {
          LegacyContentController.internalRoute(url) match {
            case Some(relative) => InternalRedirect(relative.substring(1), userChanged(req))
            case _              => ExternalRedirect(url)
          }
        }
    }
  }

  class LegacyResponseListener(info: MutableSectionInfo) extends RenderResultListener {
    import LegacyGuice.accessibilityModeService

    override def returnResult(result: SectionResult, fromId: String): Unit = {
      val context           = info.getRootRenderContext.asInstanceOf[StandardRenderContext]
      val decs              = Decorations.getDecorations(info)
      val accessibilityMode = accessibilityModeService.isAccessibilityMode
      val html = result match {
        case tr: TemplateResult =>
          val body = SectionUtils.renderToString(
            context,
            wrapBody(context, tr.getNamedResult(context, "body"))
          )
          val form = context.getForm
          val formString: Option[String] = Option(form.getAction) match {
            case Some(action) => Some(SectionUtils.renderToString(context, form))
            case None         => None
          }
          val upperbody =
            SectionUtils.renderToString(context, tr.getNamedResult(context, "upperbody"))
          val scrops = renderScreenOptions(context)
          val crumbs = renderCrumbs(context, decs).map(SectionUtils.renderToString(context, _))
          Iterable(
            Some("body" -> body),
            Option(upperbody).filter(_.nonEmpty).map("upperbody" -> _),
            formString.map("form" -> _),
            scrops.map("so" -> _),
            crumbs.map("crumbs" -> _)
          ).flatten.toMap
        case sr: SectionRenderable =>
          Map("body" -> SectionUtils.renderToString(context, wrapBody(context, sr)))
        case pr: PreRenderable =>
          Map("body" -> SectionUtils.renderToString(context, new PreRenderOnly(pr)))
      }

      context.addStatements(StatementBlock.get(context.dequeueFooterStatements))
      val ready = context.dequeueReadyStatements
      if (!ready.isEmpty)
        context.addStatements(
          new FunctionCallStatement(
            JQueryCore.JQUERY,
            new AnonymousFunction(new StatementBlock(ready).setSeperate(true))
          )
        )

      val scripts  = preRenderPageScripts(context, context).map(_.getStatements(context))
      val jsFiles  = context.getJsFiles.asScala
      val cssFiles = loadCss(context)
      val metaTags = context.getHeaderMarkup
      val title =
        Option(decs.getBannerTitle).orElse(Option(decs.getTitle)).map(_.getText).getOrElse("")
      val menuMode       = decs.getMenuMode.toString
      val fullscreenMode = decs.isFullscreen.toString
      val hideAppBar     = !(decs.isBanner || !decs.isMenuHidden || decs.isContent)
      val preventUnload  = context.getBody.getHandler(JSHandler.EVENT_BEFOREUNLOAD) != null
      info.getRequest.setAttribute(
        LegacyContentKey,
        LegacyContent(
          html,
          cssFiles,
          jsFiles,
          scripts.mkString("\n"),
          getBookmarkState(info, new BookmarkEvent(null, true, info)),
          title,
          metaTags,
          menuMode,
          fullscreenMode,
          hideAppBar,
          userChanged(info.getRequest),
          preventUnload,
          decs.isExcludeForm,
          accessibilityMode
        )
      )
    }
  }

  def loadCss(context: StandardRenderContext): Option[List[String]] = {
    def getCssFromContext: List[String] =
      context.getCssFiles.asScala.collect { case css: CssInclude =>
        css.getHref(context)
      }.toList

    val uri = context.getRequest.getRequestURI
    // Below three pages don't need 'legacy.css' so load CSS from server side
    val pagePattern = ".+/(apidocs|editoradmin|reports)\\.do".r

    val disableLegacyCss = uri match {
      case pagePattern(_) => true
      case _              => false
    }

    // If the context has a boolean attribute saying legacy CSS should be disabled, or the URL matches
    // above pattern, use CSS provided by the context.
    if (
      context.getBooleanAttribute(LegacyContentController.DISABLE_LEGACY_CSS) || disableLegacyCss
    ) {
      Option(getCssFromContext)
    } else {
      None
    }
  }

  private def preRenderPageScripts(
      context: RenderContext,
      helper: StandardRenderContext
  ): mutable.Buffer[JSStatements] = {
    val renderedStatements                      = mutable.Buffer[JSStatements]()
    var iterations: Int                         = 0
    var origStatements: util.List[JSStatements] = helper.dequeueStatements
    while ({
      !origStatements.isEmpty
    }) {
      val statements: util.List[JSStatements] = new util.ArrayList[JSStatements](origStatements)
      renderedStatements.insertAll(0, statements.asScala)
      context.preRender(statements)
      origStatements = helper.dequeueStatements
      if (
        {
          iterations += 1;
          iterations
        } > 10
      ) throw new SectionsRuntimeException("10 looks like infinity")
    }
    renderedStatements
  }

  def wrapBody(context: RenderContext, body: SectionRenderable): SectionRenderable = {
    val decs              = Decorations.getDecorations(context)
    val accessibilityMode = accessibilityModeService.isAccessibilityMode
    if (decs.isBanner || !decs.isMenuHidden || decs.isContent) {
      val cbTag = context.getBody
      cbTag.setId("content-body")
      cbTag.addClass(if (accessibilityMode) "accessibility" else "")
      val citag = new TagState("content-inner").addClass[TagState](decs.getPageLayoutDisplayClass)
      val cbtag = cbTag.addClasses[TagState](decs.getContentBodyClasses)

      new DivRenderer(citag, new DivRenderer(cbtag, body))
    } else new DivRenderer(context.getBody, body)
  }

  def renderCrumbs(context: RenderContext, d: Decorations): Option[SectionRenderable] = {
    val bc = Breadcrumbs.get(context)
    if (d.isForceBreadcrumbsOn || (d.isBreadcrumbs && !bc.getLinks.isEmpty)) Option {
      val ct = new TagState("breadcrumb-inner")
      val allCrumbs = bc.getLinks.asScala.map {
        case ls: HtmlLinkState => new LinkRenderer(ls)
        case o                 => new TagRenderer("span", o)
      } :+ Option(bc.getForcedLastCrumb).getOrElse(d.getTitle)
      new SpanRenderer(
        ct,
        new DelimitedRenderer(
          " " + CoreStrings.text("breadcrumb.separator") + " ",
          allCrumbs.toSeq: _*
        )
      )
    }
    else None
  }

  def renderedResponse(info: MutableSectionInfo) = {
    Option(info.getRootRenderContext.getRenderedResponse).map { sr =>
      info.setRendered()
      Response.ok(SectionUtils.renderToString(LegacyContentController.prepareJSContext(info), sr))
    }
  }

  def renderScreenOptions(context: RenderContext): Option[String] = {
    HelpAndScreenOptionsSection
      .getContent(context)
      .asScala
      .get("screenoptions")
      .map(bbr => SectionUtils.renderToString(context, bbr.getRenderable))
  }

  def ajaxResponse(info: MutableSectionInfo, arc: AjaxRenderContext): Response.ResponseBuilder = {
    var resp: ResponseBuilder = null
    val context               = LegacyContentController.prepareJSContext(info)

    def renderAjaxBody(sr: SectionRenderable): Unit = {
      val body    = context.getBody
      val formTag = context.getForm
      if (formTag.getAction == null) {
        val bookmarkEvent = new BookmarkEvent(null, true, null)
        formTag.setAction(new InfoFormAction(new InfoBookmark(context, bookmarkEvent)))
      }
      formTag.setNestedRenderable(sr)
      body.setNestedRenderable(formTag)
      renderScreenOptions(context)
      SectionUtils.renderToWriter(context, body, new DevNullWriter)
    }

    val renderedBody = Option(context.getRenderedBody).getOrElse {
      var bodySR: SectionResult = null
      context.processEvent(
        new RenderEvent(
          context,
          Option(context.getModalId).getOrElse(context.getRootId),
          new RenderResultListener {
            override def returnResult(result: SectionResult, fromId: String): Unit =
              bodySR = result
          }
        )
      )
      bodySR
    } match {
      case tr: TemplateResult    => tr.getNamedResult(context, "body")
      case sr: SectionRenderable => sr
      case pr: PreRenderable     => new PreRenderOnly(pr)
      // Due to many unknowns of what could cause renderedBody being null, return a 500 error at the moment.
      case _ =>
        LOGGER.debug("Unknown error at renderedBody - ajaxResponse");
        return Response.status(Status.NOT_IMPLEMENTED);
    }
    renderAjaxBody(renderedBody)
    val responseCallback = arc.getJSONResponseCallback
    info.setRendered()
    // removes old ui css that gets included when a sections ajax request is made
    arc.clearCss()
    Response.ok(responseCallback.getResponseObject(arc))
  }
}
