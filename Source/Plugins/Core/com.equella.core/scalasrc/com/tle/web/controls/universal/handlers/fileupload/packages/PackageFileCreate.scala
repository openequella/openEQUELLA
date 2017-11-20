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

package com.tle.web.controls.universal.handlers.fileupload.packages

import java.util.UUID

import com.dytech.edge.common.FileInfo
import com.tle.beans.item.attachments.{Attachment, CustomAttachment, ImsAttachment}
import com.tle.common.filesystem.FileSystemConstants
import com.tle.core.plugins.{AbstractPluginService, PluginTracker}
import com.tle.web.controls.universal.{ControlContext, StagingContext}
import com.tle.web.controls.universal.handlers.fileupload._
import com.tle.web.controls.universal.handlers.fileupload.packages.IMSPackageExtension.{commitFiles, standardPackageDetails, unzipPackage}
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.render.Label
import com.tle.web.wizard.PackageInfo

import scala.collection.JavaConverters._

trait PackageAttachmentExtension {
  def treatAsLabel : Label

  def handlesPackage(upload: SuccessfulUpload, d: Seq[PackageType]): Boolean

  def create(info: SectionInfo, ctx: ControlContext, upload: SuccessfulUpload): AttachmentCreate

  def delete(ctx: ControlContext, a: Attachment): AttachmentDelete

  def handles(a: Attachment): Boolean

  def order : Int = 0
}

object PackageFileCreate {

  lazy val tracker = new PluginTracker[PackageAttachmentExtension](AbstractPluginService.get(),
    "com.tle.web.wizard.controls.universal", "packageAttachmentHandler", "type").setBeanKey("class")

  lazy val packageCreateById: Map[String, PackageAttachmentExtension] = tracker.getBeanMap.asScala.toMap
  lazy val packageTypes: Seq[PackageAttachmentExtension] = packageCreateById.values.toSeq.sortBy(_.order)

  def extensionForAttachment(a: Attachment) : Option[PackageAttachmentExtension] = packageTypes.find(_.handles(a))

  def createForUpload(info: SectionInfo, ctx: ControlContext, upload: SuccessfulUpload, d: Seq[PackageType]): AttachmentCreate = {
    packageTypes.find(_.handlesPackage(upload, d)).get.create(info, ctx, upload)
  }
}

object IMSPackageExtension extends PackageAttachmentExtension {
  override def order = 1000

  def handlesPackage(upload: SuccessfulUpload, d: Seq[PackageType]): Boolean = d.contains(IMSPackage)

  def unzipPackage(info: SectionInfo, upload: SuccessfulUpload, ctx: ControlContext): (PackageInfo, String) = {
    val pkgUnzip = upload.temporaryPath("pkg")
    ctx.repo.unzipFile(upload.uploadPath, pkgUnzip, true)
    (ctx.repo.readPackageInfo(info, pkgUnzip), pkgUnzip)
  }

  def standardPackageDetails(a: Attachment, pkgInfo: PackageInfo, upload: SuccessfulUpload): Unit = {
    val pkgFolder = upload.originalFilename
    a.setMd5sum(upload.fileInfo.getMd5CheckSum)
    a.setUrl(pkgFolder)
    a.setDescription(Option(pkgInfo.getTitle).getOrElse(pkgFolder))
  }

  def handles(a: Attachment) : Boolean = a match {
    case ims: ImsAttachment => true
    case _ => false
  }

  def commitFiles(stg: StagingContext, pkgUnzip: String, upload: SuccessfulUpload): Unit = {
    val pkgFolder = upload.originalFilename
    stg.moveFile(pkgUnzip, pkgFolder)
    stg.moveFile(upload.uploadPath, s"${FileSystemConstants.IMS_FOLDER}/${upload.originalFilename}")
    stg.delete(upload.temporaryPath(""))
    stg.setPackageFolder(pkgFolder)
    stg.deregisterFilename(upload.id)
  }

  def deleteIMSFiles(stg: StagingContext, a: Attachment): Unit = {
    stg.delete(a.getUrl)
    stg.delete(s"${FileSystemConstants.IMS_FOLDER}/${a.getUrl}")
  }

  override def delete(ctx: ControlContext, a: Attachment): AttachmentDelete = AttachmentDelete(
    a +: WebFileUploads.imsResources(ctx.repo), stg => deleteIMSFiles(stg, a)
  )

  def create(info: SectionInfo, ctx: ControlContext, upload: SuccessfulUpload): AttachmentCreate = {
    val (pkgInfo, pkgUnzip) = unzipPackage(info, upload, ctx)
    AttachmentCreate({ stg =>
      val imsa = new ImsAttachment
      imsa.setSize(upload.fileInfo.getLength)
      imsa.setExpand(false)
      standardPackageDetails(imsa, pkgInfo, upload)
      commitFiles(stg, pkgUnzip, upload)
      imsa
    }, (a,stg) => deleteIMSFiles(stg, a))
  }

  val treatAsLabel = WebFileUploads.label("handlers.file.packageoptions.aspackage")
}

object ScormPackageExtension extends PackageAttachmentExtension {
  private val KEY_SCORM_VERSION = "SCORM_VERSION"

  val treatAsLabel = WebFileUploads.label("handlers.file.packageoptions.asscorm")

  def handlesPackage(upload: SuccessfulUpload, d: Seq[PackageType]): Boolean = d.contains(SCORMPackage)

  def create(info: SectionInfo, ctx: ControlContext, upload: SuccessfulUpload): AttachmentCreate = {
    val (pkgInfo, pkgUnzip) = unzipPackage(info, upload, ctx)
    AttachmentCreate({ stg =>
      val attachment = new CustomAttachment
      attachment.setType("scorm")
      attachment.setData("fileSize", upload.fileInfo.getLength)
      standardPackageDetails(attachment, pkgInfo, upload)
      commitFiles(stg, pkgUnzip, upload)
      attachment
    }, (a, stg) => IMSPackageExtension.deleteIMSFiles(stg, a))
  }

  override def delete(ctx: ControlContext, a: Attachment): AttachmentDelete = AttachmentDelete(Seq(a),
    stg => IMSPackageExtension.deleteIMSFiles(stg, a))

  def handles(a: Attachment) : Boolean = a match {
    case ca: CustomAttachment if ca.getType == "scorm" => true
    case _ => false
  }

}