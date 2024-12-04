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

package com.tle.web.cloudproviders

import com.dytech.edge.wizard.beans.control.CustomControl
import sttp.model.Uri
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.cloudproviders.{
  CloudProviderHelper,
  CloudProviderInstance,
  CloudProviderService
}
import com.tle.core.wizard.controls.HTMLControl
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean
import com.tle.web.resources.ResourcesService
import com.tle.web.sections.events._
import com.tle.web.sections.js.generic.Js
import com.tle.web.sections.js.generic.expression.{
  ElementByIdExpression,
  ObjectExpression,
  ScriptVariable
}
import com.tle.web.sections.js.generic.function.{ExternallyDefinedFunction, IncludeFile}
import com.tle.web.sections.js.generic.statement.DeclarationStatement
import com.tle.web.sections.js.{ElementId, JSExpression}
import com.tle.web.sections.render.TagState
import com.tle.web.sections.standard.renderers.DivRenderer
import com.tle.web.sections.{SectionInfo, SectionResult}
import com.tle.web.wizard.controls.{AbstractWebControl, WebControl, WebControlModel}
import com.tle.web.wizard.render.WizardFreemarkerFactory
import com.tle.web.wizard.{BrokenWebControl, WizardStateInterface}
import java.util.UUID
import scala.jdk.CollectionConverters._

object CloudWizardControl {

  private val r     = ResourcesService.getResourceHelper(getClass)
  val ProviderRegex = """cp\.(.+)\.(.+)""".r

  val cloudJs =
    new IncludeFile(r.url("reactjs/scripts/cloudcontrol.js"), WizardFreemarkerFactory.CSS_INCLUDE)

  val initRender =
    new ExternallyDefinedFunction("CloudControl.createRender", cloudJs)

  class AttachmentHolder(val attachments: Iterable[EquellaAttachmentBean])

  val globalWizardData = new JSExpression {
    override def getExpression(info: RenderContext): String = {
      val wss     = info.getAttributeForClass(classOf[WizardStateInterface])
      val wizData = new ObjectExpression()
      wizData.put("wizId", wss.getWizid)
      wizData.put("stagingId", wss.getStagingId)
      wizData.put("userId", CurrentUser.getUserID)
      wizData.getExpression(info)
    }
    override def preRender(info: PreRenderContext): Unit = {}
  }

  val renderControlFunc = new ScriptVariable("RenderControl") {
    def getName   = name
    val declareIt = new DeclarationStatement(this, Js.call(initRender, globalWizardData))
    override def preRender(info: PreRenderContext): Unit = {
      info.preRender(declareIt)
      info.addStatements(declareIt)
    }
  }

  val reloadState =
    new ExternallyDefinedFunction("CloudControl.forceReload", cloudJs, renderControlFunc)

  def cloudControl(controlDef: HTMLControl): WebControl = {
    controlDef.getControlBean.getClassType match {
      case ProviderRegex(providerIds, controlId) =>
        (for {
          provider   <- CloudProviderHelper.getByUuid(UUID.fromString(providerIds))
          serviceUri <- provider.serviceUrls.get(s"control_$controlId")
          uri        <- CloudProviderService.serviceUri(provider, serviceUri, Map.empty).toOption
        } yield new CloudWizardControl(uri, controlDef, provider, controlId))
          .getOrElse(new BrokenWebControl(controlDef))
      case _ => null
    }
  }

  class CloudWizardControl(
      uri: Uri,
      controlDef: HTMLControl,
      provider: CloudProviderInstance,
      controlType: String
  ) extends AbstractWebControl[WebControlModel]
      with ParametersEventListener {
    setWrappedControl(controlDef)

    val config = controlDef.getControlBean.asInstanceOf[CustomControl]

    override def getModelClass: Class[WebControlModel] = classOf[WebControlModel]

    override protected def getIdForLabel: ElementId = null

    var required = false

    override def handleParameters(info: SectionInfo, event: ParametersEvent): Unit = {
      val requirement = event.getParameter(s"${getSectionId}_required", false)
      if (requirement != null) {
        required = requirement.toBoolean
      }
    }

    override def isEmpty: Boolean = required

    override def isMandatory: Boolean = required

    override def renderHtml(context: RenderEventContext): SectionResult = {

      val renderControl =
        new ExternallyDefinedFunction(
          renderControlFunc.getName,
          new IncludeFile(uri.toString(), renderControlFunc)
        )
      val ts = new TagState()

      val configValues = new ObjectExpression
      config.getAttributes.asScala.foreach { nv =>
        configValues.put(nv._1.toString, nv._2)
      }
      val func   = getReloadFunction(true)
      val params = new ObjectExpression()
      params.put("title", getTitle)
      params.put("vendorId", provider.vendorId)
      params.put("ctrlId", getSectionId)
      params.put("providerId", provider.id.toString)
      params.put("controlType", controlType)
      params.put("element", new ElementByIdExpression(this))
      params.put("reload", func)
      params.put("config", configValues)

      ts.addReadyStatements(renderControl, params)
      new DivRenderer(ts)
    }

    override def instantiateModel(info: SectionInfo): CloudWizardControlModel =
      new CloudWizardControlModel

    class CloudWizardControlModel extends WebControlModel {}
  }

}
