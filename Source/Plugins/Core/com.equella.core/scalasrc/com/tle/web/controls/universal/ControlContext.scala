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

package com.tle.web.controls.universal

import java.util.UUID

import com.tle.beans.item.ItemId
import com.tle.common.filesystem.handle.StagingFile
import com.tle.common.wizard.controls.universal.handlers.FileUploadSettings
import com.tle.core.mimetypes.MimeTypeService
import com.tle.core.services.{FileSystemService, ZipProgress}
import com.tle.core.workflow.thumbnail.service.ThumbnailService
import com.tle.core.workflow.video.VideoService
import com.tle.web.controls.universal.FileStagingContext._
import com.tle.web.controls.universal.handlers.fileupload.FileUploadState
import com.tle.web.wizard.impl.WebRepository
import org.apache.tika.config.TikaConfig
import org.apache.tika.metadata.{HttpHeaders, _}
import org.apache.tika.parser.{AutoDetectParser, ParseContext}
import org.apache.tika.sax.BodyContentHandler

import scala.util.Try

trait StagingContext {
  def deregisterFilename(id: UUID): Unit

  def fileSize(fullpath: String): Long

  def setPackageFolder(pkgFolder: String): Unit

  def delete(path: String): Unit

  def listRootFilenames(): Set[String]

  def moveFile(from: String, to: String): Unit

  def thumbRequest(filePath: String): String

  def gatherAdditionalMetadata(filePath: String): Iterable[(String, AnyRef)]

  def unzip(srcZip: String, target: String): ZipProgress

  def stgFile: StagingFile
}

trait ControlContext {
  def repo: WebRepository = controlState.getRepository
  def state: FileUploadState
  def mimeTypeForFilename(name: String): String
  def controlSettings: FileUploadSettings
  def controlState: UniversalControlState
  def stagingContext: StagingContext
}

object FileStagingContext {
  // Can't find property 'LAST_MODIFIED' from Tika v2 so create a val with
  // how 'LAST_MODIFIED' is created in Tika v1.27
  val lastModified = Property.internalDate("Last-Modified")

  val tikaMapping = Iterable(
    Office.AUTHOR        -> "author",
    DublinCore.PUBLISHER -> "publisher",
    lastModified         -> "lastmodified",
    Office.PAGE_COUNT    -> "pagecount",
    Office.WORD_COUNT    -> "wordcount"
  )

  val tikaMimes = Set(
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
  )
}

class FileStagingContext(
    stgId: Option[String],
    itemId: ItemId,
    fileSystemService: FileSystemService,
    thumbnailService: ThumbnailService,
    videoService: VideoService,
    mimeTypeService: MimeTypeService,
    repo: WebRepository
) extends StagingContext {
  def unzip(srcZip: String, target: String): ZipProgress = {
    fileSystemService.unzipWithProgress(stgFile, srcZip, target)
  }

  lazy val stgFile = stgId.map(new StagingFile(_)).get

  def moveFile(from: String, to: String): Unit = fileSystemService.move(stgFile, from, to)

  def listRootFilenames(): Set[String] =
    fileSystemService.enumerate(stgFile, "", null).map(_.getName.toLowerCase()).toSet

  def delete(path: String) = fileSystemService.removeFile(stgFile, path)

  def setPackageFolder(pkgFolder: String): Unit =
    repo.getState.getWizardMetadataMapper.setPackageExtractedFolder(pkgFolder)

  def fileSize(fullPath: String): Long = fileSystemService.fileLength(stgFile, fullPath)

  def deregisterFilename(id: UUID): Unit = repo.unregisterFilename(id)

  def thumbRequest(filePath: String): String = {
    val thumbnail = thumbnailService.submitThumbnailRequest(itemId, stgFile, filePath, true, false)
    if (videoService.canConvertVideo(filePath))
      videoService.makeGalleryVideoPreviews(stgFile, filePath)
    thumbnail
  }

  def gatherAdditionalMetadata(filepath: String): Iterable[(String, AnyRef)] = {
    (if (
       tikaMimes(mimeTypeService.getMimeTypeForFilename(filepath)) && fileSystemService
         .fileExists(stgFile, filepath)
     ) {
       Try(fileSystemService.read(stgFile, filepath))
         .flatMap { inp =>
           val tried = Try {
             val meta = new Metadata
             meta.set(TikaCoreProperties.RESOURCE_NAME_KEY, filepath)
             val bcHandler = new BodyContentHandler
             val parser    = new AutoDetectParser(new TikaConfig(getClass.getClassLoader))
             parser.parse(inp, bcHandler, meta, new ParseContext)
             meta
           }
           inp.close()
           tried
         }
         .toOption
         .map { m =>
           tikaMapping.flatMap {
             case (FileStagingContext.lastModified, "lastmodified") =>
               Option(m.getDate(FileStagingContext.lastModified)).map(("lastmodified", _))
             case (f: Property, ef) => Option(m.get(f)).map((ef, _))
           }
         }
     } else None).getOrElse(Iterable.empty)
  }
}
