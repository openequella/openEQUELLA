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

package com.tle.mets.packagehandler

import com.tle.beans.item.attachments.{Attachment, CustomAttachment}
import com.tle.common.PathUtils
import com.tle.core.guice.Bind
import com.tle.mets.MetsConstants.{METS_FOLDER_PREFIX, METS_TYPE}
import com.tle.mets.metsimport.METSTreeBuilder
import com.tle.web.controls.universal.{ControlContext, StagingContext}
import com.tle.web.controls.universal.handlers.fileupload._
import com.tle.web.controls.universal.handlers.fileupload.packages.{
  IMSPackageExtension,
  PackageAttachmentExtension
}
import com.tle.web.resources.ResourcesService
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.result.util.KeyLabel

import javax.inject.Inject

@Bind
class MetsPackageAttachmentHandlerNew extends PackageAttachmentExtension {

  @Inject var metsTreeBuilder: METSTreeBuilder = _

  val MetsType = OtherPackage(METS_TYPE.toUpperCase())

  def handlesPackage(upload: SuccessfulUpload, d: Seq[PackageType]) = d.contains(MetsType)

  def create(info: SectionInfo, ctx: ControlContext, upload: SuccessfulUpload) = {
    val (pkgInfo, extractedFolder) = IMSPackageExtension.unzipPackage(info, upload, ctx)
    AttachmentCreate(
      { stg =>
        val ma = new CustomAttachment
        ma.setType(METS_TYPE)
        IMSPackageExtension.standardPackageDetails(ma, pkgInfo, upload)
        val destFile  = PathUtils.filePath(METS_FOLDER_PREFIX, upload.originalFilename)
        val pkgFolder = upload.originalFilename
        metsTreeBuilder.createTree(
          ctx.repo.getItem,
          stg.stgFile,
          extractedFolder,
          upload.uploadPath,
          pkgFolder,
          false
        )
        stg.moveFile(upload.uploadPath, destFile)
        stg.moveFile(extractedFolder, pkgFolder)
        stg.deregisterFilename(upload.id)
        stg.setPackageFolder(pkgFolder)
        ma.setUrl(destFile)
        ma
      },
      MetsPackageCommit
    )
  }

  val r            = ResourcesService.getResourceHelper(getClass)
  val treatAsLabel = new KeyLabel(r.key("mets.packageoptions.aspackage"))

  override def delete(ctx: ControlContext, a: Attachment): AttachmentDelete = AttachmentDelete(
    Seq(a),
    { stg =>
      MetsPackageCommit.cancel(a, stg)
    }
  )

  def handles(a: Attachment): Boolean = a match {
    case ca: CustomAttachment if ca.getType == METS_TYPE => true
    case _                                               => false
  }

  /** Mets package commit type. Simply returns the attachment on apply, but on cancel ensures all
    * content is deleted from the dedicated METS package folder and unpack folder in the staging
    * area.
    */
  object MetsPackageCommit extends AttachmentCommit {
    override def apply(a: Attachment, stg: StagingContext): Attachment = a

    override def cancel(a: Attachment, stg: StagingContext): Unit = {
      // delete Mets package file
      stg.delete(a.getUrl)
      // delete Mets unpack folder
      if (a.getUrl.startsWith(s"${METS_FOLDER_PREFIX}/")) {
        stg.delete(a.getUrl.substring(s"${METS_FOLDER_PREFIX}/".length))
      }
    }
  }
}
