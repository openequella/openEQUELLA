package com.tle.web.api

import java.util
import java.util.{ArrayList, LinkedList, List}

import com.tle.legacy.LegacyGuice
import com.tle.web.sections._
import com.tle.web.sections.equella.js.StandardExpressions
import com.tle.web.sections.events._
import com.tle.web.sections.header.MutableHeaderHelper
import com.tle.web.sections.js.JSStatements
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction
import com.tle.web.sections.render.{CssInclude, SectionRenderable, TemplateResult}
import io.swagger.annotations.Api
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.ws.rs.core.Response.ResponseBuilder
import javax.ws.rs.core.{Context, Response}
import javax.ws.rs._

import scala.collection.JavaConverters._
import scala.collection.mutable

case class NameValue(name: String, value: String)

case class LegacyContent(html: Map[String, String],
                         css: Iterable[String],
                         js: Iterable[String],
                         script: Option[String],
                         state: Iterable[NameValue])

@Api("Legacy content")
@Path("content")
class LegacyContentApi {


  def withTreePath(_path: String, req: HttpServletRequest, resp: HttpServletResponse,
                   f: MutableSectionInfo => ResponseBuilder): Response = {
    val path = s"/${_path}"
    (Option(LegacyGuice.treeRegistry.getTreeForPath(path)) match {
      case None => Response.status(404)
      case Some(tree) => {
        LegacyGuice.userSessionService.reenableSessionUse()
        val info = LegacyGuice.sectionsController.createUnfilteredInfo(tree, req, resp, null)
        info.setAttribute(SectionInfo.KEY_PATH, path)
        f(info)
      }
    }).build()
  }

  @GET
  @Path("/render/{path}")
  @Produces(value = Array("application/json"))
  def renderHtml(@PathParam("path") _path: String, @Context req: HttpServletRequest, @Context resp: HttpServletResponse): Response = {

    withTreePath(_path, req, resp, renderTree)
  }

  @POST
  @Path("/submit/{path}")
  @Produces(value = Array("application/json"))
  def submit(@PathParam("path") _path: String, @Context req: HttpServletRequest, @Context resp: HttpServletResponse,
             params: Iterable[NameValue]): Response = {
    withTreePath(_path, req, resp,
      { info =>
        val paramEvent = new ParametersEvent(params.groupBy(_.name).mapValues(_.map(_.value).toArray).asJava, true)
        info.addParametersEvent(paramEvent)
        info.processEvent(paramEvent)
        renderTree(info)
      })
  }

  def renderTree(info: MutableSectionInfo): ResponseBuilder = {
    info.fireBeforeEvents()
    info.processQueue()
    info.fireReadyToRespond(false)

    val context = info.getRootRenderContext
    val helper = context.getHelper.asInstanceOf[MutableHeaderHelper]
    helper.setElementFunction(StandardExpressions.ELEMENT_FUNCTION)

    val bodyTag = context.getBody
    val formTag = context.getForm
    if (helper.getFormExpression == null) {
      formTag.setId(StandardExpressions.FORM_NAME)
      helper.setFormExpression(StandardExpressions.FORM_EXPRESSION)
    }

    if (helper.isSubmitFunctionsSet) sys.error("Will not work with special submitfuncs")


    helper.setSubmitFunctions(
      new ExternallyDefinedFunction("EQ.event"),
      new ExternallyDefinedFunction("EQ.eventnv"),
      new ExternallyDefinedFunction("EQ.event"),
      new ExternallyDefinedFunction("EQ.eventnv"))
    helper.setTriggerEventFunction(StandardExpressions.TRIGGER_EVENT_FUNCTION)
    val standardContext = context.getAttributeForClass(classOf[StandardRenderContext])
    standardContext.setBindFunction(StandardExpressions.BIND_EVENT_FUNCTION)
    standardContext.setBindW3CFunction(StandardExpressions.BIND_W3C_FUNCTION)
    val rootId = info.getRootId

    var html: Map[String, String] = Map.empty

    val renderEvent = new RenderEvent(context, rootId, new RenderResultListener {
      override def returnResult(result: SectionResult, fromId: String): Unit = html = result match {
        case tr: TemplateResult =>
          Map("body" -> SectionUtils.renderToString(context, tr.getNamedResult(context, "body")))
        case sr: SectionRenderable =>
          Map("body" -> SectionUtils.renderToString(context, sr))
      }
    })
    info.processEvent(renderEvent)
    println(preRenderPageScripts(context, standardContext))
    val jsFiles = standardContext.getJsFiles.asScala
    val cssFiles = standardContext.getCssFiles.asScala.collect {
      case css: CssInclude => css.getHref(standardContext)
    }
    Response.ok(LegacyContent(html, cssFiles, jsFiles, None, Iterable.empty))
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
        iterations += 1; iterations
      } > 10) throw new SectionsRuntimeException("10 looks like infinity")
    }
    renderedStatements
  }
}
