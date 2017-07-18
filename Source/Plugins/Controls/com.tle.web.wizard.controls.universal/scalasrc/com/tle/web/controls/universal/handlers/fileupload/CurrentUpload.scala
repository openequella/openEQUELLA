package com.tle.web.controls.universal.handlers.fileupload

import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

import com.dytech.edge.common.FileInfo


object CurrentUpload
{
   def validatedUploads(u: Iterable[CurrentUpload]): Iterable[ValidatedUpload] = u collect {
     case v: ValidatedUpload => v
   }
}

sealed trait CurrentUpload
{
  def finished: Boolean
  def id: UUID
  def started: Instant
  def temporaryPath(path: String): String = s"${FileUploadState.UPLOADS_FOLDER}/${id}_files/$path"
}

case class UploadingFile(id: UUID, started: Instant, originalFilename: String, uploadPath: String, description: String, cancel: AtomicReference[Boolean]) extends CurrentUpload
{
  def success(fileInfo: FileInfo): CurrentUpload = SuccessfulUpload(id, started, originalFilename, uploadPath, description, fileInfo)
  def failed(reason: UploadResult): CurrentUpload = FailedUpload(id, started, originalFilename, reason)
  def finished = false
}

case class SuccessfulUpload(id: UUID, started: Instant, originalFilename: String, uploadPath: String, description: String, fileInfo: FileInfo) extends CurrentUpload
{
  def finished = true
  def successful = true
}

case class ValidatedUpload(s: SuccessfulUpload, detected: Seq[PackageType]) extends CurrentUpload {
  def finished = true
  def id = s.id
  def started = s.started
}

case class FailedUpload(id: UUID, started: Instant, originalFilename: String, reason: UploadResult) extends CurrentUpload {
  def finished = true
}

sealed trait IllegalFileReason

object IllegalFileReason
{
  def fromString(s: String): IllegalFileReason = s match {
    case "size" => FileTooBig
    case _ => WrongType
  }
}

case object BannedType extends IllegalFileReason
case object WrongType extends IllegalFileReason
case object FileTooBig extends IllegalFileReason
case object NotAPackage extends IllegalFileReason
case class WrongPackageType(allowed: Seq[PackageType], detected: Seq[PackageType]) extends IllegalFileReason

sealed trait UploadResult
case class Successful(fileInfo: FileInfo) extends UploadResult
case object Cancelled extends UploadResult
case class IllegalFile(reason: IllegalFileReason) extends UploadResult
case class Errored(t: Throwable) extends UploadResult
