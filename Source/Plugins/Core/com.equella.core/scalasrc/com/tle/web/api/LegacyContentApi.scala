package com.tle.web.api

import java.net.URI
import java.util
import java.util.Collections

import com.dytech.common.io.DevNullWriter
import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.plugins.PluginTracker
import com.tle.legacy.LegacyGuice
import com.tle.web.api.LegacyContentController.getBookmarkState
import com.tle.web.sections._
import com.tle.web.sections.ajax.AjaxRenderContext
import com.tle.web.sections.equella.js.StandardExpressions
import com.tle.web.sections.errors.SectionsExceptionHandler
import com.tle.web.sections.events._
import com.tle.web.sections.events.js.BookmarkAndModify
import com.tle.web.sections.generic.InfoBookmark
import com.tle.web.sections.header.{InfoFormAction, MutableHeaderHelper}
import com.tle.web.sections.jquery.libraries.JQueryCore
import com.tle.web.sections.js.JSStatements
import com.tle.web.sections.js.generic.function.{AnonymousFunction, ExternallyDefinedFunction}
import com.tle.web.sections.js.generic.statement.{FunctionCallStatement, StatementBlock}
import com.tle.web.sections.registry.AbstractSectionsController
import com.tle.web.sections.render.{CssInclude, SectionRenderable, TagState, TemplateResult}
import com.tle.web.sections.standard.model.SimpleBookmark
import com.tle.web.sections.standard.renderers.DivRenderer
import com.tle.web.template.{Decorations, RenderTemplate}
import io.swagger.annotations.Api
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.ws.rs._
import javax.ws.rs.core.Response.ResponseBuilder
import javax.ws.rs.core.{Context, Response, UriInfo}

import scala.collection.JavaConverters._
import scala.collection.mutable

case class NameValue(name: String, value: String)


case class RedirectContent(redirect: String, state: Iterable[NameValue])

case class MenuItem(title: String, href: Option[String], systemIcon: Option[String], route: Option[String])

case class MenuLinks(groups: Iterable[Iterable[MenuItem]])

case class LegacyContent(html: Map[String, String],
                         css: Iterable[String],
                         js: Iterable[String],
                         script: String,
                         state: Iterable[NameValue],
                         title: String,
                         menuMode: String,
                         fullscreenMode: String)

object LegacyContentController extends AbstractSectionsController {
  override protected def getTreeForPath(path: String): SectionTree = LegacyGuice.treeRegistry.getTreeForPath(path)

  override protected def getSectionFilters: util.List[SectionFilter] = Collections.emptyList()

  override protected def getExceptionHandlers: util.List[SectionsExceptionHandler] = Collections.emptyList()

  def getBookmarkState(info: SectionInfo, event: BookmarkEvent): Iterable[NameValue] = {
    val q = new InfoBookmark(info, event).getBookmarkParams
    q.asScala.flatMap {
      case (k, vals) => vals.map(v => NameValue(k, v))
    }
  }

  override def execute(info: SectionInfo): Unit = {
    val minfo = info.getAttributeForClass(classOf[MutableSectionInfo])
    minfo.fireBeforeEvents()
    minfo.processQueue()
    val redirect = info.isForceRedirect
    minfo.fireReadyToRespond(redirect)
    if (redirect) {
      info.getRequest.setAttribute("REDIRECTED",
        minfo.getAttribute[String](SectionInfo.KEY_PATH).substring(1))

      info.getRequest.setAttribute("STATE", getBookmarkState(info, new BookmarkEvent()))
    }
  }

  override def forwardToUrl(info: SectionInfo, link: String, code: Int): Unit = {
    info.getRequest.setAttribute("REDIRECTED", link)
  }
}

@Api("Legacy content")
@Path("content")
class LegacyContentApi {

  def withTreePath(_path: String, uriInfo: UriInfo, req: HttpServletRequest, resp: HttpServletResponse,
                   f: MutableSectionInfo => ResponseBuilder): Response = {
    val path = s"/${_path}"
    (Option(LegacyGuice.treeRegistry.getTreeForPath(path)) match {
      case None => Response.status(404)
      case Some(tree) => {
        LegacyGuice.userSessionService.reenableSessionUse()
        val info = LegacyContentController.createUnfilteredInfo(tree, req, resp, null)
        info.setAttribute(SectionInfo.KEY_PATH, path)
        info.setAttribute(SectionInfo.KEY_BASE_HREF,
          uriInfo.getBaseUriBuilder.path(classOf[LegacyContentApi])
            .path(classOf[LegacyContentApi], "ajaxCall").build(""))
        f(info)
      }
    }).build()
  }

  @GET
  @Path("menu")
  @Produces(value = Array("application/json"))
  def menuOptions(@Context req: HttpServletRequest, @Context resp: HttpServletResponse): MenuLinks = {
    val contributors = LegacyGuice.menuService.getContributors
    val noInst = CurrentInstitution.get == null
    val (noParam, filterName) = if (noInst) (false, "serverAdmin")
    else if (CurrentUser.isGuest) (false, "guest")
    else (true, "loggedIn")
    val baseUri = if (noInst) LegacyGuice.urlService.getAdminUrl.toURI else CurrentInstitution.get().getUrlAsUri
    LegacyGuice.userSessionService.reenableSessionUse()
    val context = LegacyGuice.sectionsController.createInfo("/home.do", req, resp, null, null, null)


    MenuLinks {
      contributors.getExtensions(new PluginTracker.ParamFilter("enabledFor", noParam, filterName)).asScala.flatMap { ext =>
        contributors.getBeanByExtension(ext).getMenuContributions(context).asScala
      }.groupBy(_.getGroupPriority).toSeq.sortBy(_._1).map {
        case (_, links) =>
          links.sortBy(_.getLinkPriority).map { mc =>
            val menuLink = mc.getLink
            val href = Option(menuLink.getBookmark).getOrElse(
              new BookmarkAndModify(context, menuLink.getHandlerMap.getHandler("click").getModifier)).getHref
            val relativized = baseUri.relativize(URI.create(href)).toString

            MenuItem(menuLink.getLabelText,
              Option(href).filter(_ != relativized),
              Option(mc.getSystemIcon),
              Option(mc.getRoute).orElse(Option(relativized)))
          }
      }
    }
  }

  @POST
  @Path("/ajax/{path : .+}")
  @Produces(value = Array("application/json"))
  def ajaxCall(@PathParam("path") _path: String, @Context uriInfo: UriInfo,
               @Context req: HttpServletRequest, @Context resp: HttpServletResponse): Response = {

    withTreePath(_path, uriInfo, req, resp, { i =>

      val paramEvent = new ParametersEvent(req.getParameterMap, true)
      i.addParametersEvent(paramEvent)
      i.processEvent(paramEvent)
      renderTree(i, true)
    })
  }

  @POST
  @Path("/submit/{path : .+}")
  @Produces(value = Array("application/json"))
  def submit(@PathParam("path") _path: String, @Context uriInfo: UriInfo,
             @Context req: HttpServletRequest, @Context resp: HttpServletResponse,
             params: Iterable[NameValue]): Response = {
    withTreePath(_path, uriInfo, req, resp,
      { info =>
        val paramEvent = new ParametersEvent(params.groupBy(_.name).mapValues(_.map(_.value).toArray).asJava, true)
        info.addParametersEvent(paramEvent)
        info.processEvent(paramEvent)
        renderTree(info, false)
      })
  }

  def redirectResponse(req: HttpServletRequest)(url: String): ResponseBuilder = {
    val state = Option(req.getAttribute("STATE").asInstanceOf[Iterable[NameValue]]).getOrElse(Iterable.empty)
    val fromBase = LegacyGuice.urlService.getBaseUriFromRequest(req).relativize(URI.create(url)).toString
    Response.ok(RedirectContent(fromBase, state))
  }

  def renderTree(info: MutableSectionInfo, ajax: Boolean): ResponseBuilder = {
    LegacyContentController.execute(info)

    val req = info.getRequest
    Option(req.getAttribute("REDIRECTED").asInstanceOf[String]).map(redirectResponse(req)).getOrElse {
      if (ajax) {
        Option(info.getRootRenderContext.getRenderedResponse).map {
          case sr: SectionRenderable => Response.ok(SectionUtils.renderToString(prepareJSContext(info), sr))
        }.getOrElse {
          var resp: ResponseBuilder = null
          val context = prepareJSContext(info)

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

          context.processEvent(new RenderEvent(context, Option(context.getModalId).getOrElse(context.getRootId),
            new RenderResultListener {
              override def returnResult(result: SectionResult, fromId: String): Unit = resp = {
                val arc = context.getAttributeForClass(classOf[AjaxRenderContext])
                val decs = Decorations.getDecorations(info)
                result match {
                  case tr: TemplateResult => renderAjaxBody(tr.getNamedResult(context, "body"))
                  case sr: SectionRenderable => renderAjaxBody(sr)
                }
                val responseCallback = arc.getJSONResponseCallback
                Response.ok(responseCallback.getResponseObject(arc))
              }
            }))
          resp
        }
      }
      else {
        val context = prepareJSContext(info)
        val rootId = info.getRootId
        var html: Map[String, String] = Map.empty
        val decs = Decorations.getDecorations(info)
        val renderEvent = new RenderEvent(context, rootId, new RenderResultListener {
          override def returnResult(result: SectionResult, fromId: String): Unit = html = result match {
            case tr: TemplateResult =>
              Map("body" -> SectionUtils.renderToString(context, wrapBody(decs, tr.getNamedResult(context, "body"))))
            case sr: SectionRenderable =>
              Map("body" -> SectionUtils.renderToString(context, wrapBody(decs, sr)))
          }
        })
        info.processEvent(renderEvent)

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
        Response.ok(LegacyContent(html, cssFiles, jsFiles, scripts.mkString("\n"),
          getBookmarkState(info, new BookmarkEvent(null, true, info)), title, menuMode, fullscreenMode))
      }
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
}
