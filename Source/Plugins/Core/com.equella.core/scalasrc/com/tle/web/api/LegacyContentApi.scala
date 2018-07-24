package com.tle.web.api

import java.net.URI
import java.util
import java.util.Collections

import com.dytech.common.io.DevNullWriter
import com.tle.beans.item.ItemTaskId
import com.tle.common.URLUtils
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
import com.tle.web.sections.events.js.BookmarkAndModify
import com.tle.web.sections.generic.InfoBookmark
import com.tle.web.sections.header.{InfoFormAction, MutableHeaderHelper}
import com.tle.web.sections.jquery.libraries.JQueryCore
import com.tle.web.sections.js.JSStatements
import com.tle.web.sections.js.generic.function.{AnonymousFunction, ExternallyDefinedFunction}
import com.tle.web.sections.js.generic.statement.{FunctionCallStatement, StatementBlock}
import com.tle.web.sections.registry.AbstractSectionsController
import com.tle.web.sections.render._
import com.tle.web.sections.standard.model.HtmlLinkState
import com.tle.web.sections.standard.renderers.{DivRenderer, LinkRenderer, SpanRenderer}
import com.tle.web.template.section.HelpAndScreenOptionsSection
import com.tle.web.template.{Breadcrumbs, Decorations, RenderTemplate}
import com.tle.web.viewable.servlet.ItemServlet
import io.swagger.annotations.Api
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.ws.rs._
import javax.ws.rs.core.Response.ResponseBuilder
import javax.ws.rs.core.{Context, Response, UriInfo}

import scala.collection.JavaConverters._
import scala.collection.mutable


case class RedirectContent(redirect: String, userUpdated: Boolean)

case class MenuItem(title: String, href: Option[String], systemIcon: Option[String], route: Option[String])

case class LegacyContent
(html: Map[String, String],
 css: Iterable[String],
 js: Iterable[String],
 script: String,
 state: Map[String, Array[String]],
 title: String,
 menuMode: String,
 fullscreenMode: String,
 hideAppBar: Boolean,
 userUpdated: Boolean)

case class ItemCounts(tasks: Int, notifications: Int)

case class CurrentUserDetails
(id: String,
 username: String, firstName: String, lastName: String, emailAddress: String,
 autoLoggedIn: Boolean, guest: Boolean, prefsEditable: Boolean,
 menuGroups: Iterable[Iterable[MenuItem]], counts: Option[ItemCounts])

object LegacyContentController extends AbstractSectionsController {

  def baseUri: URI = {
    Option(CurrentInstitution.get()).map(_.getUrlAsUri).
      getOrElse(LegacyGuice.urlService.getAdminUrl.toURI)
  }

  val RedirectedAttr = "REDIRECTED"

  override protected def getTreeForPath(path: String): SectionTree =
    LegacyGuice.treeRegistry.getTreeForPath(path)

  def getBookmarkState(info: SectionInfo, event: BookmarkEvent): Map[String, Array[String]] = {
    val q = new InfoBookmark(info, event).getBookmarkParams
    q.asScala.toMap
  }

  def prepareJSContext(info: MutableSectionInfo): StandardRenderContext = {
    val context = info.getRootRenderContext
    val helper = context.getHelper.asInstanceOf[MutableHeaderHelper]
    helper.setElementFunction(StandardExpressions.ELEMENT_FUNCTION)

    val bodyTag = context.getBody
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
        new ExternallyDefinedFunction("EQ.eventnv"))
    }
    helper.setTriggerEventFunction(StandardExpressions.TRIGGER_EVENT_FUNCTION)
    val standardContext = context.getAttributeForClass(classOf[StandardRenderContext])
    standardContext.setBindFunction(StandardExpressions.BIND_EVENT_FUNCTION)
    standardContext.setBindW3CFunction(StandardExpressions.BIND_W3C_FUNCTION)
    standardContext.preRender(RenderTemplate.STYLES_CSS)
    standardContext
  }

  override def renderFromRoot(info: SectionInfo): Unit = {
    prepareJSContext(info.getAttributeForClass(classOf[MutableSectionInfo]))
    super.renderFromRoot(info)
  }

  override def forwardToUrl(info: SectionInfo, link: String, code: Int): Unit = {
    info.setRendered()
    info.getRequest.setAttribute(RedirectedAttr, link)
  }

  lazy val selectionFilter = LegacyGuice.selectionService.get().asInstanceOf[SectionFilter]
  lazy val integrationFilter = LegacyGuice.integrationService.get().asInstanceOf[SectionFilter]

  override def createInfo(tree: SectionTree, path: String, request: HttpServletRequest,
                          response: HttpServletResponse, from: SectionInfo,
                          params: util.Map[String, Array[String]],
                          attrs: util.Map[AnyRef, AnyRef]): MutableSectionInfo = {
    val sectionInfo = createUnfilteredInfo(tree, request, response, attrs)
    sectionInfo.setAttribute(SectionInfo.KEY_PATH, path)
    sectionInfo.setAttribute(SectionInfo.KEY_FORWARDFROM, from)
    sectionInfo.setAttribute(SectionInfo.KEY_BASE_HREF, baseUri)
    LegacyGuice.moderationService.filter(sectionInfo)
    selectionFilter.filter(sectionInfo)
    integrationFilter.filter(sectionInfo)
    sectionInfo
  }

  override def handleException(info: SectionInfo, exception: Throwable,
                               event: SectionEvent[_]): Unit = {
    throw exception
  }
}

@Api("Legacy content")
@Path("content")
class LegacyContentApi {

  def parsePath(path: String): (String, mutable.Map[AnyRef, AnyRef]) = path match {
    case p if p.startsWith("items/") => {
      val itemId = ItemTaskId.parse(p.substring(6))
      ("/viewitem/viewitem.do", mutable.Map(ItemServlet.VIEWABLE_ITEM ->
        LegacyGuice.viewableItemFactory.createNewViewableItem(itemId)))
    }
    case p => (s"/$p", mutable.Map.empty)
  }

  private val UserIdKey = "InitialUserId"

  def withTreePath(_path: String, uriInfo: UriInfo, req: HttpServletRequest, resp: HttpServletResponse,
                   f: MutableSectionInfo => ResponseBuilder): Response = {
    val (treePath, attrs) = parsePath(_path)
    val path = s"/${_path}"
    (Option(LegacyGuice.treeRegistry.getTreeForPath(treePath)) match {
      case None => Response.status(404)
      case Some(tree) => {
        LegacyGuice.userSessionService.reenableSessionUse()
        req.setAttribute(UserIdKey, CurrentUser.getUserID)
        val info = LegacyContentController.createInfo(tree, path, req, resp, null, null, attrs.asJava)
        info.setAttribute(AjaxGenerator.AJAX_BASEURI, uriInfo.getBaseUriBuilder.
          path(classOf[LegacyContentApi]).path(classOf[LegacyContentApi], "ajaxCall").build(""))
        f(info)
      }
    }).build()
  }

  @GET
  @Path("currentuser")
  @Produces(value = Array("application/json"))
  def menuOptions(@Context req: HttpServletRequest, @Context resp: HttpServletResponse): CurrentUserDetails = {
    val contributors = LegacyGuice.menuService.getContributors
    val noInst = CurrentInstitution.get == null
    val (noParam, filterName) = if (noInst) (false, "serverAdmin")
    else if (CurrentUser.isGuest) (false, "guest")
    else (true, "loggedIn")

    LegacyGuice.userSessionService.reenableSessionUse()
    val context = LegacyGuice.sectionsController.createInfo("/home.do", req, resp, null, null, null)

    val cu = CurrentUser.getUserState

    val prefsEditable = !(cu.isSystem || cu.isGuest) && !(cu.wasAutoLoggedIn &&
      LegacyGuice.configService.getProperties(new AutoLogin).isEditDetailsDisallowed)
    val menuGroups = {
      contributors.getExtensions(new PluginTracker.ParamFilter("enabledFor", noParam, filterName)).asScala.flatMap { ext =>
        contributors.getBeanByExtension(ext).getMenuContributions(context).asScala
      }.groupBy(_.getGroupPriority).toSeq.sortBy(_._1).map {
        case (_, links) =>
          links.sortBy(_.getLinkPriority).map { mc =>
            val menuLink = mc.getLink
            val href = Option(menuLink.getBookmark).getOrElse(
              new BookmarkAndModify(context, menuLink.getHandlerMap.getHandler("click").getModifier)).getHref
            val relativized = LegacyContentController.baseUri.relativize(URI.create(href)).toString

            MenuItem(menuLink.getLabelText,
              Option(href).filter(_ != relativized),
              Option(mc.getSystemIcon),
              Option(mc.getRoute).orElse(Option(relativized)))
          }
      }
    }
    val counts = if (!cu.isGuest) Option {
      val notificationCount = LegacyGuice.freeTextService.countsFromFilters(Collections.singletonList(new NotificationSearch))(0)
      val taskCount = LegacyGuice.freeTextService.countsFromFilters(Collections.singletonList(new TaskListSearch))(0);
      ItemCounts(taskCount, notificationCount)
    } else None
    val ub = cu.getUserBean
    CurrentUserDetails(id = ub.getUniqueID, username = ub.getUsername, firstName = ub.getFirstName, lastName = ub.getLastName,
      emailAddress = ub.getEmailAddress, autoLoggedIn = cu.wasAutoLoggedIn(), guest = cu.isGuest, prefsEditable = prefsEditable,
      menuGroups = menuGroups, counts = counts)
  }

  @POST
  @GET
  @Path("/ajax/{path : .+}")
  @Produces(value = Array("application/json"))
  def ajaxCall(@PathParam("path") _path: String, @Context uriInfo: UriInfo,
               @Context req: HttpServletRequest, @Context resp: HttpServletResponse): Response = {

    withTreePath(_path, uriInfo, req, resp, { info =>

      val paramEvent = new ParametersEvent(req.getParameterMap, true)
      info.addParametersEvent(paramEvent)
      info.preventGET()
      info.processEvent(paramEvent)
      LegacyContentController.execute(info)
      renderedResponse(info).getOrElse {
        ajaxResponse(info, info.getAttributeForClass(classOf[AjaxRenderContext]))
      }
    })
  }

  private val LegacyContentKey = "LegacyContent"

  @POST
  @Path("/submit/{path : .+}")
  @Produces(value = Array("application/json"))
  def submit(@PathParam("path") _path: String, @Context uriInfo: UriInfo,
             @Context req: HttpServletRequest, @Context resp: HttpServletResponse,
             params: mutable.Map[String, Array[String]]): Response = {
    withTreePath(_path, uriInfo, req, resp,
      { info =>
        val javaParams = params.asJava
        val paramEvent = new ParametersEvent(javaParams, true)
        info.preventGET()
        info.addParametersEvent(paramEvent)
        info.processEvent(paramEvent)
        info.getRootRenderContext.setRootResultListener(new LegacyResponseListener(info))
        LegacyContentController.execute(info)
        redirectResponse(info)
          .orElse(renderedResponse(info))
          .orElse(Option(info.getAttributeForClass(classOf[AjaxRenderContext])).map(arc => ajaxResponse(info, arc)))
          .getOrElse {
            Response.ok(req.getAttribute(LegacyContentKey))
          }
      })
  }

  def userChanged(req: HttpServletRequest): Boolean = {
    val idNow = CurrentUser.getUserID
    val idThen = req.getAttribute(UserIdKey).asInstanceOf[String]
    idNow != idThen
  }

  def redirectResponse(info: MutableSectionInfo): Option[ResponseBuilder] = {
    val req = info.getRequest
    Option(req.getAttribute(LegacyContentController.RedirectedAttr).asInstanceOf[String]).map { url =>
      val fromBase = LegacyGuice.urlService.getBaseUriFromRequest(req).relativize(URI.create(url)).toString
      Response.ok(RedirectContent(fromBase, userChanged(req)))
    }
  }

  class LegacyResponseListener(info: MutableSectionInfo) extends RenderResultListener {
    val context = info.getRootRenderContext.asInstanceOf[StandardRenderContext]
    val decs = Decorations.getDecorations(info)

    override def returnResult(result: SectionResult, fromId: String): Unit = {
      val html = result match {
        case tr: TemplateResult =>
          val body = SectionUtils.renderToString(context, wrapBody(decs, tr.getNamedResult(context, "body")))
          val hasoMap = HelpAndScreenOptionsSection.getContent(context).asScala
          val scrops = hasoMap.get("screenoptions").map(bbr => SectionUtils.renderToString(context, bbr.getRenderable))
          val crumbs = renderCrumbs(context, decs).map(SectionUtils.renderToString(context, _))
          Iterable(
            Some("body" -> body),
            scrops.map("so" -> _),
            crumbs.map("crumbs" -> _)
          ).flatten.toMap
        case sr: SectionRenderable =>
          Map("body" -> SectionUtils.renderToString(context, wrapBody(decs, sr)))
      }

      context.addStatements(StatementBlock.get(context.dequeueFooterStatements))
      val ready = context.dequeueReadyStatements
      if (!ready.isEmpty)
        context.addStatements(new FunctionCallStatement(JQueryCore.JQUERY,
          new AnonymousFunction(new StatementBlock(ready).setSeperate(true))))

      val scripts = preRenderPageScripts(context, context).map(_.getStatements(context))
      val jsFiles = context.getJsFiles.asScala
      val cssFiles = context.getCssFiles.asScala.collect {
        case css: CssInclude => css.getHref(context)
      }
      val title = Option(decs.getTitle).map(_.getText).getOrElse("")
      val menuMode = decs.getMenuMode.toString
      val fullscreenMode = decs.isFullscreen.toString
      val hideAppBar = !(decs.isBanner || !decs.isMenuHidden || decs.isContent)
      info.getRequest.setAttribute(LegacyContentKey,
        LegacyContent(html, cssFiles, jsFiles, scripts.mkString("\n"),
          getBookmarkState(info, new BookmarkEvent(null, true, info)), title,
          menuMode, fullscreenMode, hideAppBar, userChanged(info.getRequest)
        )
      )
    }
  }

  private def preRenderPageScripts(context: RenderContext, helper: StandardRenderContext): mutable.Buffer[JSStatements] = {
    val renderedStatements = mutable.Buffer[JSStatements]()
    var iterations: Int = 0
    var origStatements: util.List[JSStatements] = helper.dequeueStatements
    while ( {
      !origStatements.isEmpty
    }) {
      val statements: util.List[JSStatements] = new util.ArrayList[JSStatements](origStatements)
      renderedStatements.insertAll(0, statements.asScala)
      context.preRender(statements)
      origStatements = helper.dequeueStatements
      if ( {
        iterations += 1;
        iterations
      } > 10) throw new SectionsRuntimeException("10 looks like infinity")
    }
    renderedStatements
  }

  def wrapBody(decs: Decorations, body: SectionRenderable): SectionRenderable = {
    val citag = new TagState("content-inner").addClass[TagState](decs.getPageLayoutDisplayClass)
    val cbtag = new TagState("content-body").addClasses[TagState](decs.getContentBodyClasses)

    new DivRenderer(citag, new DivRenderer(cbtag, body))
  }


  def renderCrumbs(context: RenderContext, d: Decorations): Option[SectionRenderable] = {
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

  def renderedResponse(info: MutableSectionInfo) = {
    Option(info.getRootRenderContext.getRenderedResponse).map { sr =>
      Response.ok(SectionUtils.renderToString(LegacyContentController.prepareJSContext(info), sr))
    }
  }

  def ajaxResponse(info: MutableSectionInfo, arc: AjaxRenderContext) = {
    var resp: ResponseBuilder = null
    val context = LegacyContentController.prepareJSContext(info)

    def renderAjaxBody(sr: SectionRenderable): Unit = {
      val body = context.getBody
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
      context.processEvent(new RenderEvent(context, Option(context.getModalId).getOrElse(context.getRootId),
        new RenderResultListener {
          override def returnResult(result: SectionResult, fromId: String): Unit =
            bodySR = result
        }))
      bodySR
    } match {
      case tr: TemplateResult => tr.getNamedResult(context, "body")
      case sr: SectionRenderable => sr
      case pr: PreRenderable => new PreRenderOnly(pr)
    }
    renderAjaxBody(renderedBody)
    val responseCallback = arc.getJSONResponseCallback
    Response.ok(responseCallback.getResponseObject(arc))
  }
}
