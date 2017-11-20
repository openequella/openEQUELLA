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

package com.tle.mets.packagehandler

import javax.inject.Inject

import com.tle.beans.item.Item
import com.tle.beans.item.attachments.{Attachment, CustomAttachment}
import com.tle.common.PathUtils
import com.tle.core.guice.Bind
import com.tle.core.qti.QtiConstants
import com.tle.mets.metsimport.METSTreeBuilder
import com.tle.web.controls.universal.ControlContext
import com.tle.web.controls.universal.handlers.fileupload._
import com.tle.web.controls.universal.handlers.fileupload.packages.{IMSPackageExtension, PackageAttachmentExtension}
import com.tle.web.resources.ResourcesService
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.result.util.KeyLabel

@Bind
class MetsPackageAttachmentHandlerNew extends PackageAttachmentExtension {

  @Inject var metsTreeBuilder: METSTreeBuilder = _

  val MetsType = OtherPackage("METS")

  def handlesPackage(upload: SuccessfulUpload, d: Seq[PackageType]) = d.contains(MetsType)

  def create(info: SectionInfo, ctx: ControlContext, upload: SuccessfulUpload) = {
    val (pkgInfo, extractedFolder) = IMSPackageExtension.unzipPackage(info, upload, ctx)
    AttachmentCreate({ stg =>
      val ma = new CustomAttachment
      ma.setType("mets")
      IMSPackageExtension.standardPackageDetails(ma, pkgInfo, upload)
      val destFile = PathUtils.filePath("_METS", upload.originalFilename)
      val pkgFolder = upload.originalFilename
      metsTreeBuilder.createTree(ctx.repo.getItem, stg.stgFile,
        extractedFolder, upload.uploadPath, pkgFolder, false)
      stg.moveFile(upload.uploadPath, destFile)
      stg.moveFile(extractedFolder, pkgFolder)
      stg.deregisterFilename(upload.id)
      stg.setPackageFolder(pkgFolder)
      ma.setUrl(destFile)
      ma
    }, (a, stg) => delete(ctx, a).deleteFiles(stg)
    )
  }

  val r = ResourcesService.getResourceHelper(getClass)
  val treatAsLabel = new KeyLabel(r.key("mets.packageoptions.aspackage"))

  override def delete(ctx: ControlContext, a: Attachment): AttachmentDelete = AttachmentDelete(
    Seq(a), { stg =>
      stg.delete(a.getUrl)
      if (a.getUrl.startsWith("_METS/")) {
        stg.delete(a.getUrl.substring(6))
      }
    }
  )

  def handles(a: Attachment): Boolean = a match {
    case ca: CustomAttachment if ca.getType == "mets" => true
    case _ => false
  }
}
