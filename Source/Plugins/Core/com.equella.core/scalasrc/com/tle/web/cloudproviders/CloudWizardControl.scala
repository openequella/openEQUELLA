package com.tle.web.cloudproviders

import java.util.UUID

import com.dytech.edge.wizard.beans.control.CustomControl
import com.softwaremill.sttp.Uri
import com.tle.core.cloudproviders.{CloudProviderDB, CloudProviderInstance, CloudProviderService}
import com.tle.core.db.RunWithDB
import com.tle.core.wizard.controls.HTMLControl
import com.tle.web.resources.ResourcesService
import com.tle.web.sections.events.RenderEventContext
import com.tle.web.sections.js.ElementId
import com.tle.web.sections.js.generic.expression.ObjectExpression
import com.tle.web.sections.js.generic.function.IncludeFile
import com.tle.web.sections.render.TagState
import com.tle.web.sections.standard.renderers.DivRenderer
import com.tle.web.sections.{SectionInfo, SectionResult}
import com.tle.web.wizard.BrokenWebControl
import com.tle.web.wizard.controls.{AbstractWebControl, WebControl, WebControlModel}

import scala.collection.JavaConverters._

object CloudWizardControl {

  private val r     = ResourcesService.getResourceHelper(getClass)
  val ProviderRegex = """cp\.(.+)\.(.+)""".r

  val cloudJs = new IncludeFile(r.url("scripts/cloudcontrol.js"))

  def cloudControl(controlDef: HTMLControl): WebControl = {
    controlDef.getControlBean.getClassType match {
      case ProviderRegex(providerIds, controlId) =>
        val providerId = UUID.fromString(providerIds)
        RunWithDB.execute {
          CloudProviderDB
            .get(providerId)
            .subflatMap { provider =>
              CloudProviderService
                .serviceUri(provider, s"control_$controlId", Map.empty)
                .map { uri =>
                  new CloudWizardControl(uri, controlDef, provider, controlId)
                }
            }
            .getOrElse(new BrokenWebControl(controlDef))
        }
      case _ => null
    }
  }

  class CloudWizardControl(uri: Uri,
                           controlDef: HTMLControl,
                           provider: CloudProviderInstance,
                           controlType: String)
      extends AbstractWebControl[WebControlModel] {
    setWrappedControl(controlDef)

    val config = controlDef.getControlBean.asInstanceOf[CustomControl]

    override def getModelClass: Class[WebControlModel] = classOf[WebControlModel]

    override protected def getIdForLabel: ElementId = null

    override def renderHtml(context: RenderEventContext): SectionResult = {
      import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction
      val renderControl =
        new ExternallyDefinedFunction("CloudControl.render",
                                      new IncludeFile(uri.toString(), cloudJs))
      val ts = new TagState(this)

      val configValues = new ObjectExpression
      config.getAttributes.asScala.foreach { nv =>
        configValues.put(nv._1.toString, nv._2)
      }
      val params = new ObjectExpression()
      params.put("title", getTitle)
      params.put("vendorId", provider.vendorId)
      params.put("providerId", provider.id.toString)
      params.put("controlType", controlType)
      params.put("element", this)
      params.put("config", configValues)
      ts.addReadyStatements(renderControl, params)
      new DivRenderer(ts)
    }

    override def instantiateModel(info: SectionInfo): CloudWizardControlModel =
      new CloudWizardControlModel

    class CloudWizardControlModel extends WebControlModel {}
  }

}
