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

package com.tle.web.selection

import com.tle.web.integration.SingleSignonForm
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.js.generic.expression.ObjectExpression
import com.tle.web.sections.render.SimpleSectionResult
import com.tle.web.template.RenderNewTemplate
import com.tle.web.template.RenderNewTemplate.r
import io.circe.Json
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser._

object NewSelectionPage {

  val selectionJS = r.url("reactjs/selection.js")

  case class CourseSelection(courseId: Option[String], courseCode: Option[String], structure: Option[Json])

  case class ReturnData(returnurl: Option[String], returnprefix: Option[String], cancelurl: Option[String],
                        forcePost: Boolean, cancelDisabled: Boolean)

  case class SelectionData(returnData: ReturnData, courseData: CourseSelection, itemonly: Boolean, packageonly: Boolean,
                           attachmentonly: Boolean, selectMultiple: Boolean, useDownloadPrivilege: Boolean, attachmentUuidUrls: Boolean,
                           itemXml: Option[String], powerXml: Option[String])

  object SelectionData
  {
    def apply(formData: SingleSignonForm): SelectionData = {
      SelectionData(ReturnData(Option(formData.getReturnurl), Option(formData.getReturnprefix),
        Option(formData.getCancelurl), formData.isForcePost, formData.isCancelDisabled),
        CourseSelection(Option(formData.getCourseId), Option(formData.getCourseCode), Option(formData.getStructure)
          .flatMap(s => parse(s).toOption)),
        formData.isItemonly, formData.isPackageonly, formData.isAttachmentonly, formData.isSelectMultiple,
        formData.isUseDownloadPrivilege, formData.isAttachmentUuidUrls, Option(formData.getItemXml), Option(formData.getPowerXml))
    }
  }

  def renderNewSelection(info: SectionInfo, formData: SingleSignonForm): Unit = {

    def prepareJSData(data: ObjectExpression): ObjectExpression = {
      data.put("selection", SelectionData(formData).asJson.noSpaces)
      data
    }

    info.setAttribute(RenderNewTemplate.SetupJSKey, prepareJSData _)
    info.setAttribute(RenderNewTemplate.ReactJSKey, selectionJS)
    info.preventGET()
    info.getRootRenderContext.setRenderedBody(new SimpleSectionResult(""))
  }
}
