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

package com.tle.web.qti.packagehandler

import java.util.UUID
import javax.inject.Inject

import com.tle.beans.item.attachments.{Attachment, CustomAttachment}
import com.tle.common.PathUtils
import com.tle.common.filesystem.FileSystemConstants
import com.tle.core.guice.Bind
import com.tle.core.qti.QtiConstants
import com.tle.core.qti.service.QtiService
import com.tle.ims.service.IMSService
import com.tle.web.controls.universal.handlers.fileupload._
import com.tle.web.controls.universal.handlers.fileupload.packages.IMSPackageExtension.{commitFiles, standardPackageDetails, unzipPackage}
import com.tle.web.controls.universal.handlers.fileupload.packages.PackageAttachmentExtension
import com.tle.web.controls.universal.{ControlContext, StagingContext}
import com.tle.web.resources.ResourcesService
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.result.util.KeyLabel

import scala.collection.JavaConverters._

@Bind
class QtiPackageAttachmentHandlerNew extends PackageAttachmentExtension {

  @Inject var qtiService : QtiService = _
  @Inject var imsService: IMSService = _

  def handlesPackage(upload: SuccessfulUpload, d: Seq[PackageType]) = d.contains(QTIPackage)

  def create(info: SectionInfo, ctx: ControlContext, upload: SuccessfulUpload): AttachmentCreate = {
    val (pkgInfo, pkgUnzip) = unzipPackage(info, upload, ctx)
    AttachmentCreate({ stg =>
      val qti = new CustomAttachment
      qti.setType(QtiConstants.TEST_CUSTOM_ATTACHMENT_TYPE)
      qti.setData(QtiConstants.KEY_FILE_SIZE, upload.fileInfo.getLength)
      standardPackageDetails(qti, pkgInfo, upload)
      populateDetails(stg, qti, pkgUnzip)
      val pkgFolder = QtiConstants.QTI_FOLDER_PATH
      qti.setUrl(PathUtils.filePath(QtiConstants.QTI_FOLDER_PATH, "imsmanifest.xml"))
      stg.moveFile(pkgUnzip, pkgFolder)
      stg.delete(upload.uploadPath)
      stg.delete(upload.temporaryPath(""))
      stg.setPackageFolder(pkgFolder)
      stg.deregisterFilename(upload.id)
      qti
    }, (a,stg) => delete(ctx, a).deleteFiles(stg))
  }

  def handles(a: Attachment) : Boolean = a match {
    case ca: CustomAttachment if ca.getType == QtiConstants.TEST_CUSTOM_ATTACHMENT_TYPE => true
    case _ => false
  }


  private def populateDetails(stg: StagingContext, attachment: CustomAttachment, baseExtractedPath: String): Unit = {
    val manifest = Option(imsService.getImsManifest(stg.stgFile, baseExtractedPath, false))
    manifest.flatMap(m => m.getAllResources.asScala.find(_.getType.startsWith("imsqti_test_xml"))).foreach { res =>
      val xmlRelLoc = res.getHref
      val quiz = qtiService.loadV2Test(stg.stgFile, baseExtractedPath, xmlRelLoc)
      val details = qtiService.getTestDetails(quiz)
      attachment.setDescription(details.getTitle)
      val testUuid = UUID.randomUUID.toString
      attachment.setData(QtiConstants.KEY_TEST_UUID, testUuid)
      attachment.setData(QtiConstants.KEY_XML_PATH, PathUtils.filePath(QtiConstants.QTI_FOLDER_PATH, xmlRelLoc))
      val toolName = details.getToolName
      if (toolName != null) {
        attachment.setData(QtiConstants.KEY_TOOL_NAME, toolName)
        attachment.setData(QtiConstants.KEY_TOOL_VERSION, details.getToolVersion)
      }
      attachment.setData(QtiConstants.KEY_MAX_TIME, details.getMaxTime)
      attachment.setData(QtiConstants.KEY_QUESTION_COUNT, details.getQuestionCount)
      attachment.setData(QtiConstants.KEY_SECTION_COUNT, details.getSectionCount)
      attachment.setData(QtiConstants.KEY_NAVIGATION_MODE, details.getNavigationMode.toQtiString)
    }
  }

  val r = ResourcesService.getResourceHelper(getClass)
  val treatAsLabel = new KeyLabel(r.key("packageoptions.asqti"))

  def delete(ctx: ControlContext, a: Attachment) : AttachmentDelete = AttachmentDelete(Seq(a), stg =>
    stg.delete(QtiConstants.QTI_FOLDER_PATH)
  )
}
