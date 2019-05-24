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

import java.net.URI
import java.util
import java.util.Collections

import com.dytech.common.io.DevNullWriter
import com.tle.beans.item.ItemTaskId
import com.tle.common.institution.CurrentInstitution
import com.tle.common.settings.standard.AutoLogin
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.i18n.CoreStrings
import com.tle.core.notification.standard.indexer.NotificationSearch
import com.tle.core.plugins.PluginTracker
import com.tle.core.workflow.freetext.TaskListSearch
import com.tle.legacy.LegacyGuice
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
import com.tle.web.sections.registry.{AbstractSectionsController, SectionsControllerImpl}
import com.tle.web.sections.render._
import com.tle.web.sections.standard.model.HtmlLinkState
import com.tle.web.sections.standard.renderers.{DivRenderer, LinkRenderer, SpanRenderer}
import com.tle.web.template.Decorations.MenuMode
import com.tle.web.template.section.HelpAndScreenOptionsSection
import com.tle.web.template.{Breadcrumbs, Decorations, RenderTemplate}
import com.tle.web.viewable.NewDefaultViewableItem
import com.tle.web.viewable.servlet.ItemServlet
import io.lemonlabs.uri.{Path => _, _}
import io.swagger.annotations.Api
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.ws.rs._
import javax.ws.rs.core.Response.ResponseBuilder
import javax.ws.rs.core.{CacheControl, Context, Response, UriInfo}

import scala.collection.JavaConverters._
import scala.collection.mutable

case class InternalRedirect(route: String, userUpdated: Boolean)

case class ExternalRedirect(href: String)

case class MenuItem(title: String,
                    href: Option[String],
                    systemIcon: Option[String],
                    route: Option[String],
                    iconUrl: Option[String],
                    newWindow: Boolean)

case class LegacyContent(html: Map[String, String],
                         css: Iterable[String],
                         js: Iterable[String],
                         script: String,
                         state: Map[String, Array[String]],
                         title: String,
                         menuMode: String,
                         fullscreenMode: String,
                         hideAppBar: Boolean,
                         userUpdated: Boolean,
                         preventUnload: Boolean,
                         noForm: Boolean)

case class ItemCounts(tasks: Int, notifications: Int)

case class CurrentUserDetails(id: String,
                              username: String,
                              firstName: String,
                              lastName: String,
                              emailAddress: String,
                              autoLoggedIn: Boolean,
                              guest: Boolean,
                              prefsEditable: Boolean,
                              menuGroups: Iterable[Iterable[MenuItem]],
                              counts: Option[ItemCounts])

object LegacyContentController extends AbstractSectionsController with SectionFilter {

  import LegacyGuice.urlService

  def relativeURI(uri: String): Option[RelativeUrl] = {
    val baseUrl   = AbsoluteUrl.parse(urlService.getBaseInstitutionURI.toString)
    val Host      = baseUrl.host
    val Port      = baseUrl.port
    val basePaths = baseUrl.path.parts.filter(_.length > 0)
    Url.parse(uri) match {
      case r: RelativeUrl => Some(r)
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

        override def addToBookmark(info: SectionInfo,
                                   bookmarkState: util.Map[String, Array[String]]): Unit =
          bookmarkState.put(RenderTemplate.XSRF_PARAM, Array[String](CurrentUser.getSessionID))
      }
    )
  }

  def baseUri(req: HttpServletRequest): URI = urlService.getBaseUriFromRequest(req)

  val RedirectedAttr = "REDIRECTED"

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

  override def handleException(info: SectionInfo,
                               exception: Throwable,
                               event: SectionEvent[_]): Unit = {
    throw exception
  }
}

@Api("Legacy content")
@Path("content")
class LegacyContentApi {

  def parsePath(path: String): (String, MutableSectionInfo => MutableSectionInfo) = {

    def itemViewer(p: String, f: (SectionInfo, NewDefaultViewableItem) => NewDefaultViewableItem)
      : (String, MutableSectionInfo => MutableSectionInfo) = {
      val itemId = ItemTaskId.parse(p)
      (s"/viewitem/viewitem.do", { info: MutableSectionInfo =>
        info.setAttribute(ItemServlet.VIEWABLE_ITEM,
                          f(info, LegacyGuice.viewableItemFactory.createNewViewableItem(itemId)))
        info
      })
    }
    path match {
      case ""                          => ("/home.do", identity)
      case p if p.startsWith("items/") => itemViewer(p.substring("items/".length), (_, vi) => vi)
      case p if p.startsWith("integ/gen/") =>
        itemViewer(
          p.substring("integ/gen/".length), { (info, vi) =>
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

  def withTreePath(_path: String,
                   uriInfo: UriInfo,
                   req: HttpServletRequest,
                   resp: HttpServletResponse,
                   params: util.Map[String, Array[String]],
                   f: MutableSectionInfo => ResponseBuilder): Response = {
    val (treePath, setupInfo) = parsePath(_path)
    val path                  = s"/${_path}"
    (Option(LegacyGuice.treeRegistry.getTreeForPath(treePath)) match {
      case None => Response.status(404)
      case Some(tree) => {
        LegacyGuice.userSessionService.reenableSessionUse()
        req.setAttribute(UserIdKey, CurrentUser.getUserID)
        val info =
          setupInfo(LegacyContentController.createInfo(tree, path, req, resp, null, params, null))
        info.setAttribute(AjaxGenerator.AJAX_BASEURI,
                          uriInfo.getBaseUriBuilder
                            .path(classOf[LegacyContentApi])
                            .path(classOf[LegacyContentApi], "ajaxCall")
                            .build(""))
        f(info)
      }
    }).build()
  }

  @GET
  @Path("currentuser")
  @Produces(value = Array("application/json"))
  def menuOptions(@Context req: HttpServletRequest,
                  @Context resp: HttpServletResponse): Response = {
    val contributors = LegacyGuice.menuService.getContributors
    val noInst       = CurrentInstitution.get == null
    val (noParam, filterName) =
      if (noInst) (false, "serverAdmin")
      else if (CurrentUser.isGuest) (false, "guest")
      else (true, "loggedIn")

    LegacyGuice.userSessionService.reenableSessionUse()
    val context = LegacyGuice.sectionsController.createInfo("/home.do", req, resp, null, null, null)

    val cu = CurrentUser.getUserState

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
        .map {
          case (_, links) =>
            links.sortBy(_.getLinkPriority).map { mc =>
              val menuLink = mc.getLink
              val href = Option(menuLink.getBookmark)
                .getOrElse(
                  new BookmarkAndModify(context,
                                        menuLink.getHandlerMap.getHandler("click").getModifier))
                .getHref
              val relativized =
                LegacyContentController.relativeURI(href).filter(_.path.parts.last.endsWith(".do"))
              val route   = Option(mc.getRoute)
              val iconUrl = if (mc.isCustomImage) Some(mc.getBackgroundImagePath) else None
              MenuItem(
                menuLink.getLabelText,
                if (relativized.isEmpty && route.isEmpty) Some(href) else None,
                Option(mc.getSystemIcon),
                route.orElse(relativized.map(r => "/" + r.toString)),
                iconUrl,
                "_blank" == menuLink.getTarget
              )
            }
        }
    }
    val counts = if (!cu.isGuest) Option {
      val notificationCount = LegacyGuice.freeTextService.countsFromFilters(
        Collections.singletonList(new NotificationSearch))(0)
      val taskCount = LegacyGuice.freeTextService.countsFromFilters(
        Collections.singletonList(new TaskListSearch))(0);
      ItemCounts(taskCount, notificationCount)
    } else None
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
          counts = counts
        )
      )
      .cacheControl(cacheControl)
      .build()
  }

  @POST
  @GET
  @Path("/ajax/{path : .+}")
  @Produces(value = Array("application/json"))
  def ajaxCall(@PathParam("path") _path: String,
               @Context uriInfo: UriInfo,
               @Context req: HttpServletRequest,
               @Context resp: HttpServletResponse): Response = {

    withTreePath(
      _path,
      uriInfo,
      req,
      resp,
      req.getParameterMap, { info =>
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
  @Produces(value = Array("application/json"))
  def submit(@PathParam("path") _path: String,
             @Context uriInfo: UriInfo,
             @Context req: HttpServletRequest,
             @Context resp: HttpServletResponse,
             params: mutable.Map[String, Array[String]]): Response = {
    withTreePath(
      _path,
      uriInfo,
      req,
      resp,
      params.asJava, { info =>
        info.preventGET()
        info.getRootRenderContext.setRootResultListener(new LegacyResponseListener(info))
        LegacyContentController.execute(info)
        redirectResponse(info)
          .orElse(renderedResponse(info))
          .orElse(Option(info.getAttributeForClass(classOf[AjaxRenderContext])).map(arc =>
            ajaxResponse(info, arc)))
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
    idNow != idThen
  }

  def redirectResponse(info: MutableSectionInfo): Option[ResponseBuilder] = {
    val req = info.getRequest
    Option(req.getAttribute(LegacyContentController.RedirectedAttr).asInstanceOf[String]).map {
      url =>
        Response.ok {
          LegacyContentController.relativeURI(url) match {
            case None           => ExternalRedirect(url)
            case Some(relative) => InternalRedirect(relative.toString, userChanged(req))
          }
        }
    }
  }

  class LegacyResponseListener(info: MutableSectionInfo) extends RenderResultListener {

    override def returnResult(result: SectionResult, fromId: String): Unit = {
      val context = info.getRootRenderContext.asInstanceOf[StandardRenderContext]
      val decs    = Decorations.getDecorations(info)
      val html = result match {
        case tr: TemplateResult =>
          val body = SectionUtils.renderToString(
            context,
            wrapBody(context, tr.getNamedResult(context, "body")))
          val upperbody =
            SectionUtils.renderToString(context, tr.getNamedResult(context, "upperbody"))
          val hasoMap = HelpAndScreenOptionsSection.getContent(context).asScala
          val scrops = hasoMap
            .get("screenoptions")
            .map(bbr => SectionUtils.renderToString(context, bbr.getRenderable))
          val crumbs = renderCrumbs(context, decs).map(SectionUtils.renderToString(context, _))
          Iterable(
            Some("body"                                          -> body),
            Option(upperbody).filter(_.nonEmpty).map("upperbody" -> _),
            scrops.map("so"                                      -> _),
            crumbs.map("crumbs"                                  -> _)
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
            new AnonymousFunction(new StatementBlock(ready).setSeperate(true))))

      val scripts = preRenderPageScripts(context, context).map(_.getStatements(context))
      val jsFiles = context.getJsFiles.asScala
      val cssFiles = context.getCssFiles.asScala.collect {
        case css: CssInclude => css.getHref(context)
      }
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
          menuMode,
          fullscreenMode,
          hideAppBar,
          userChanged(info.getRequest),
          preventUnload,
          decs.isExcludeForm
        )
      )
    }
  }

  private def preRenderPageScripts(context: RenderContext,
                                   helper: StandardRenderContext): mutable.Buffer[JSStatements] = {
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
      if ({
        iterations += 1;
        iterations
      } > 10) throw new SectionsRuntimeException("10 looks like infinity")
    }
    renderedStatements
  }

  def wrapBody(context: RenderContext, body: SectionRenderable): SectionRenderable = {
    val decs = Decorations.getDecorations(context)

    if (decs.isBanner || !decs.isMenuHidden || decs.isContent) {
      val cbTag = context.getBody
      cbTag.setId("content-body")
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
        new DelimitedRenderer(" " + CoreStrings.text("breadcrumb.separator") + " ", allCrumbs: _*))
    } else None
  }

  def renderedResponse(info: MutableSectionInfo) = {
    Option(info.getRootRenderContext.getRenderedResponse).map { sr =>
      info.setRendered()
      Response.ok(SectionUtils.renderToString(LegacyContentController.prepareJSContext(info), sr))
    }
  }

  def ajaxResponse(info: MutableSectionInfo, arc: AjaxRenderContext) = {
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
        ))
      bodySR
    } match {
      case tr: TemplateResult    => tr.getNamedResult(context, "body")
      case sr: SectionRenderable => sr
      case pr: PreRenderable     => new PreRenderOnly(pr)
    }
    renderAjaxBody(renderedBody)
    val responseCallback = arc.getJSONResponseCallback
    info.setRendered()
    Response.ok(responseCallback.getResponseObject(arc))
  }
}
