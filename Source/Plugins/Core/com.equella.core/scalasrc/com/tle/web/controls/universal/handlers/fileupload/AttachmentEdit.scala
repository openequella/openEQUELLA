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

package com.tle.web.controls.universal.handlers.fileupload

import com.tle.beans.item.attachments.{Attachment, FileAttachment}
import com.tle.core.plugins.{AbstractPluginService, PluginTracker}
import com.tle.web.controls.universal.{ControlContext, StagingContext}
import com.tle.web.controls.universal.handlers.fileupload.packages.PackageAttachmentExtension
import com.tle.web.sections.SectionInfo
import scala.jdk.CollectionConverters._

/** Trait for different commit type. Only for file type commits (etc. URL or WebPage are not
  * included).
  */
trait AttachmentCommit {

  /** Apply the commit. Some typical operations can be moving the attachment out of temp "_uploads"
    * folder and updating the state. Typically triggered if user uploads files directly through
    * universal attachment control (the drag and release area) or when they click the save button in
    * inline file upload dialog (a popup dialog for user creates or edits resources).
    *
    * @param a
    *   Attachment file
    * @param stg
    *   Staging context which includes current staging info and services, such as staging folder
    *   info and file service.
    */
  def apply(a: Attachment, stg: StagingContext): Attachment

  /** Cancel this commit. Some typical operations can be delete the uploaded file and update the
    * state. Typically triggered when user click `back` button or `close` button in inline file
    * upload dialog.
    *
    * @param a
    *   Attachment file.
    * @param stg
    *   Staging context which includes current staging info and services, such as staging folder
    *   info and move file operation.
    */
  def cancel(a: Attachment, stg: StagingContext): Unit
}

/** Empty commit type. Example: when you choose editing the exiting attachment the init commit type
  * would be this.
  */
object EmptyAttachmentCommit extends AttachmentCommit {
  def apply(a: Attachment, stg: StagingContext): Attachment = a
  def cancel(a: Attachment, stg: StagingContext): Unit      = {}
}

/** Case class encapsulating the functions required for the creation and management of new
  * attachment
  *
  * @param createStaged
  *   Function to create attachment and update the stage state.
  * @param commit
  *   Class encapsulating the functions of committing attachment and canceling commit action.
  */
case class AttachmentCreate(createStaged: StagingContext => Attachment, commit: AttachmentCommit)

/** Companion object for creating AttachmentCreate instances for uploaded file exclude package file
  * (etc. IMS, Mets, Qti).
  */
object AttachmentCreate {

  /** Create an AttachmentCreate instance for standard file exclude package file (etc. IMS, Mets,
    * Qti).
    *
    * @param uploaded
    *   Uploaded file.
    * @param suppressThumb
    *   Whether prevent generating thumbnails.
    */
  def apply(uploaded: SuccessfulUpload, suppressThumb: Boolean): AttachmentCreate = {
    def createStaged(stg: StagingContext) = {
      val fa = new FileAttachment
      fa.setFilename(uploaded.uploadPath)
      fa.setDescription(uploaded.description)
      fa.setMd5sum(uploaded.fileInfo.getMd5CheckSum)
      fa.setSize(uploaded.fileInfo.getLength)
      stg.gatherAdditionalMetadata(uploaded.uploadPath).foreach { a =>
        fa.setData(a._1, a._2)
      }
      fa
    }

    new AttachmentCreate(createStaged, StandardFileCommit(uploaded, suppressThumb, None))
  }

  /** Create an AttachmentCreate instance for package file (etc. IMS, Mets, Qti).
    *
    * @param info
    *   Instance of SectionInfo.
    * @param ctx
    *   Instance of ControlContext which encapsulates necessary object and functions such as
    *   FileUploadState, FileUploadState and stagingContext and so on.
    * @param uploaded
    *   Uploaded file.
    * @param d
    *   A sequence contains package types .
    */
  def apply(
      info: SectionInfo,
      ctx: ControlContext,
      uploaded: SuccessfulUpload,
      d: Seq[PackageType]
  ): AttachmentCreate = {
    packageTypes.find(_.handlesPackage(uploaded, d)).get.create(info, ctx, uploaded)
  }

  lazy val packageCreateById: Map[String, PackageAttachmentExtension] = {
    new PluginTracker[PackageAttachmentExtension](
      AbstractPluginService.get(),
      "com.tle.web.wizard.controls.universal",
      "packageAttachmentHandler",
      "type"
    ).setBeanKey("class").getBeanMap.asScala.toMap
  }

  lazy val packageTypes: Seq[PackageAttachmentExtension] =
    packageCreateById.values.toSeq.sortBy(_.order)

  /** Get corresponding child class of PackageAttachmentExtension for attachment. For example,
    * return IMSPackageExtension for an IMSAttachment type of file.
    *
    * @param a
    *   Attachment.
    */
  def extensionForPackageAttachment(a: Attachment): Option[PackageAttachmentExtension] =
    packageTypes.find(_.handles(a))
}

/** Case class encapsulating a list of attachments and a function of deleting those attachments.
  *
  * @param attachments
  *   List of attachment need to be delete.
  * @param deleteFiles
  *   Function of deleting operation.
  */
case class AttachmentDelete(attachments: Iterable[Attachment], deleteFiles: StagingContext => Unit)
