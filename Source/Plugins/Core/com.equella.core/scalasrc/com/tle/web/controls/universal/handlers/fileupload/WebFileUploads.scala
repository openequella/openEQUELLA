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

import java.io.InputStream
import java.time.Instant
import java.util.{Collections, UUID}
import com.dytech.common.GeneralConstants
import com.dytech.edge.exceptions.BannedFileException
import com.tle.beans.item.attachments._
import com.tle.common.PathUtils
import com.tle.common.wizard.controls.universal.handlers.FileUploadSettings
import com.tle.web.controls.universal.{ControlContext, StagingContext}
import com.tle.web.resources.ResourcesService
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.equella.ajaxupload.AjaxUpload
import com.tle.web.sections.js.generic.Js
import com.tle.web.sections.js.generic.function.{ExternallyDefinedFunction, IncludeFile}
import com.tle.web.sections.js.{JSAssignable, JSExpression}
import com.tle.web.sections.render._
import com.tle.web.sections.result.util.KeyLabel
import com.tle.web.wizard.impl.WebRepository
import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

object WebFileUploads {

  val r                = ResourcesService.getResourceHelper(getClass)
  def label(s: String) = new KeyLabel(r.key(s))

  val KEY_INCORRECT_MIMETYPE      = r.key("handlers.file.error.incorrectmimetype")
  val KEY_ERROR_MAXFILESIZE       = r.key("handlers.file.maxfilesize")
  val KEY_ERROR_BANNED            = r.key("handlers.file.error.banned")
  val KEY_ERROR_NOTPACKAGE        = r.key("handlers.file.error.notpackage")
  val KEY_ERROR_NOTALLOWEDPACKAGE = r.key("handlers.file.error.notallowedpackage")

  private val INCLUDE = new IncludeFile(r.url("scripts/file/fileuploadhandler.js"))
  private val FILE_UPLOAD_HANDLER_CLASS =
    new ExternallyDefinedFunction("FileUploadHandler", INCLUDE)
  val ZIP_PROGRESS_FUNC =
    new ExternallyDefinedFunction(FILE_UPLOAD_HANDLER_CLASS, "setupZipProgress", 2)

  val SUPPRESS_THUMB_VALUE                         = "suppress"
  def isSuppressThumbnail(et: Attachment): Boolean = SUPPRESS_THUMB_VALUE == et.getThumbnail

  def validateBeforeUpload(
      mimeType: String,
      size: Long,
      fileSettings: FileUploadSettings
  ): Option[IllegalFileReason] = {
    if (
      fileSettings.isRestrictByMime && !fileSettings.getMimeTypes.asScala.toSet.contains(mimeType)
    )
      Some(WrongType)
    else if (
      fileSettings.isRestrictFileSize && size > (fileSettings.getMaxFileSize.toLong * GeneralConstants.BYTES_PER_MEGABYTE)
    )
      Some(FileTooBig)
    else None
  }

  def writeStream(uf: UploadingFile, ctx: ControlContext, stream: InputStream): UploadResult = {
    val tried = Try(ctx.repo.uploadStream(uf.uploadPath, stream, true))
    Try(stream.close)
    tried match {
      case Success(finfo) => Successful(finfo)
      case Failure(t) =>
        Try(ctx.stagingContext.delete(uf.uploadPath))
        t match {
          case b: BannedFileException => IllegalFile(BannedType)
          case t                      => Errored(t)
        }
    }
  }

  def uploadStream(
      uploadId: UUID,
      filename: String,
      description: String,
      fileSize: Long,
      mimeType: String,
      openStream: () => InputStream,
      ctx: ControlContext
  ): CurrentUpload = {
    val currentUpload = validateBeforeUpload(mimeType, fileSize, ctx.controlSettings) match {
      case Some(failReason) =>
        FailedUpload(uploadId, Instant.now(), filename, IllegalFile(failReason))
      case None =>
        ctx.state.initialiseUpload(uploadId, filename, description) match {
          case uf: UploadingFile =>
            writeStream(uf, ctx, openStream()) match {
              case Successful(finfo) => uf.success(finfo)
              case result            => uf.failed(result)
            }
          case o => o
        }
    }
    ctx.state.finishedUpload(currentUpload)
    currentUpload match {
      case fu: FailedUpload => ctx.stagingContext.deregisterFilename(uploadId)
      case _                => ()
    }
    currentUpload
  }

  val packageTypes: Map[PackageType, FileUploadSettings => Boolean] = Map(
    (QTIPackage, _.isQtiPackagesOnly),
    (SCORMPackage, _.isScormPackagesOnly)
  )

  def allowedPackageTypes(fileSettings: FileUploadSettings): Seq[PackageType] = {
    if (fileSettings.isPackagesOnly) {
      packageTypes.collect { case (p, f) if f(fileSettings) => p }.toSeq
    } else Seq.empty
  }

  def validateContent(
      info: SectionInfo,
      ctx: ControlContext,
      uploadPath: String
  ): Either[IllegalFileReason, Seq[PackageType]] = {
    val settings = ctx.controlSettings
    val detected =
      ctx.repo.determinePackageTypes(info, uploadPath).asScala.map(PackageType.fromString).toSeq
    val allowed = allowedPackageTypes(settings)
    if (settings.isPackagesOnly && detected.isEmpty)
      Left(NotAPackage)
    else if (settings.isPackagesOnly && allowed.nonEmpty && !allowed.exists(detected.toSet))
      Left(WrongPackageType(allowed, detected))
    else
      Right(detected)
  }

  def failedValidation(
      ctx: ControlContext,
      s: SuccessfulUpload,
      ifr: IllegalFileReason
  ): CurrentUpload = {
    cleanupForUpload(ctx, s).apply(ctx.stagingContext)
    FailedUpload(s.id, s.started, s.originalFilename, IllegalFile(ifr))
  }

  def validateAllFinished(info: SectionInfo, ctx: ControlContext): Unit = {
    val allFinished = ctx.state.allCurrentUploads.collect { case s: SuccessfulUpload =>
      validateContent(info, ctx, s.uploadPath)
        .fold(ifr => failedValidation(ctx, s, ifr), dpt => ValidatedUpload(s, dpt))
    }
    allFinished.foreach(u => ctx.state.finishedUpload(u))
  }

  def removeFailedUploads(ctx: ControlContext, apartFrom: Option[UUID]): Unit = {
    val previousFails = ctx.state.allCurrentUploads.collect {
      case fu: FailedUpload if !apartFrom.contains(fu.id) => fu.id
    }
    ctx.state.removeAll(previousFails)
  }

  def attachmentCreatorForUpload(
      info: SectionInfo,
      ctx: ControlContext,
      v: ValidatedUpload
  ): AttachmentCreate = {
    if (v.detected.nonEmpty) AttachmentCreate(info, ctx, v.s, v.detected)
    else AttachmentCreate(v.s, ctx.controlSettings.isSuppressThumbnails)
  }

  def uniqueName(
      filename: String,
      replacing: Option[String],
      id: UUID,
      ctx: ControlContext
  ): String = {
    val rootNames = ctx.stagingContext.listRootFilenames() -- replacing.map(_.toLowerCase())
    val p         = PathUtils.fileParts(filename)

    @tailrec
    def registerName(fname: (String, String), count: Int): String = {
      if (count >= 100) sys.error("Failed to find unique filename after 100 tries")
      val mid = if (count == 1) "" else s"($count)"
      val n   = s"${fname._1}$mid${fname._2}"
      val ln  = n.toLowerCase()
      if (!rootNames.contains(ln) && ctx.repo.registerFilename(id, ln)) n
      else registerName(fname, count + 1)
    }

    registerName((p.getFirst, if (p.getSecond.isEmpty) "" else "." + p.getSecond), 1)
  }

  def errorMessage(allUploads: Iterable[CurrentUpload]): Option[Label] = {
    allUploads.toBuffer[CurrentUpload].reverse.collectFirst {
      case FailedUpload(_, _, fn, IllegalFile(ifr)) => labelForIllegalReason(ifr, fn)
    }
  }

  def labelForIllegalReason(reason: IllegalFileReason, filename: String): Label =
    new KeyLabel(
      reason match {
        case FileTooBig             => KEY_ERROR_MAXFILESIZE
        case WrongType              => KEY_INCORRECT_MIMETYPE
        case NotAPackage            => KEY_ERROR_NOTPACKAGE
        case WrongPackageType(_, _) => KEY_ERROR_NOTALLOWEDPACKAGE
        case BannedType             => KEY_ERROR_BANNED
      },
      filename
    )

  def deleteAttachment(ctx: ControlContext, a: Attachment): AttachmentDelete = a match {
    case fa: FileAttachment =>
      AttachmentDelete(Iterable(fa), stg => if (!isSelectedInAZip(fa)) stg.delete(fa.getFilename))
    case za: ZipAttachment =>
      AttachmentDelete(
        a +: findAttachments(ctx.repo, isSelectedInZip(za)),
        { stg =>
          stg.delete(removeZipPath(za.getUrl))
          stg.delete(za.getUrl)
        }
      )
    case a: Attachment => AttachmentCreate.extensionForPackageAttachment(a).get.delete(ctx, a)
  }

  def removeFilesForUpload(su: SuccessfulUpload)(stg: StagingContext): Unit = {
    stg.delete(su.uploadPath)
    stg.delete(su.temporaryPath(""))
  }

  def cleanupForUpload(ctx: ControlContext, cu: CurrentUpload): StagingContext => Unit = {
    ctx.repo.unregisterFilename(cu.id)
    cu match {
      case uf: UploadingFile =>
        _ => uf.cancel.set(true)
      case su: SuccessfulUpload  => removeFilesForUpload(su)
      case ValidatedUpload(s, _) => removeFilesForUpload(s)
      case fu: FailedUpload =>
        _ => ()
    }
  }

  def isFileOrZipAttachment(a: IAttachment): Boolean = a match {
    case ha: HtmlAttachment => false
    case fa: FileAttachment => true
    case za: ZipAttachment  => true
    case _                  => false
  }

  def isPackageAttachment(a: IAttachment): Boolean = a match {
    case a: Attachment => AttachmentCreate.extensionForPackageAttachment(a).isDefined
    case _             => false
  }

  def imsResources(repo: WebRepository): List[Attachment] =
    findAttachments(repo, _.getAttachmentType == AttachmentType.IMSRES)

  def findAttachments(repo: WebRepository, f: IAttachment => Boolean): List[Attachment] = {
    val attachments = repo.getState.getAttachments
    attachments.iterator().asScala.filter(f).map(_.asInstanceOf[Attachment]).toList
  }

  def isZipFile(a: Attachment): Boolean = a match {
    case fa: FileAttachment => fa.getFilename.endsWith(".zip")
    case za: ZipAttachment  => true
    case _                  => false
  }

  def zipPath(fn: String): String = s"_zips/$fn"

  def removeZipPath(fn: String): String = fn.substring(6)

  def zipAttachment(attachment: IAttachment): Option[ZipAttachment] = attachment match {
    case za: ZipAttachment => Some(za)
    case _                 => None
  }

  def isSelectedInAZip(a: IAttachment): Boolean = a match {
    case fa: FileAttachment => fa.getData(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID) != null
    case _                  => false
  }

  def isSelectedInZip(za: ZipAttachment)(a: IAttachment): Boolean = a match {
    case fa: FileAttachment => fa.getData(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID) == za.getUuid
    case _                  => false
  }

  def filenameOnly(fn: String): String = {
    val ind = fn.lastIndexOf('/')
    if (ind == -1) fn else fn.substring(ind + 1)
  }

  def isWebPage(filename: String): Boolean = {
    val lower = filename.toLowerCase
    lower.endsWith(".html") || lower.endsWith(".htm")
  }

  def validateFunc(
      controlSettings: FileUploadSettings,
      errorCallback: JSExpression,
      startedUpload: JSExpression,
      doneCallback: JSExpression
  ): JSAssignable = {
    Js.functionValue(
      AjaxUpload.createValidate(
        if (controlSettings.isRestrictFileSize)
          controlSettings.getMaxFileSize.toLong * GeneralConstants.BYTES_PER_MEGABYTE
        else 0,
        if (controlSettings.isRestrictByMime) controlSettings.getMimeTypes
        else Collections.emptyList(),
        errorCallback,
        startedUpload,
        doneCallback
      )
    )
  }
}
