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

package com.tle.web.controls.universal.handlers.fileupload
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

import com.tle.web.sections.render.Label

import scala.collection.JavaConverters._

object FileUploadState
{
  val UPLOADS_FOLDER = "_uploads"
}

import com.tle.web.controls.universal.handlers.fileupload.FileUploadState._
class FileUploadState {

  private val currentUploads = new ConcurrentHashMap[UUID, CurrentUpload]().asScala

  def initialiseUpload(id: UUID, filename: String, description: String): CurrentUpload = {
    val ind = filename.indexOf('.')
    val ext = if (ind == -1) "" else filename.substring(ind)
    val newUpload = UploadingFile(id, Instant.now(), filename, s"$UPLOADS_FOLDER/$id$ext", description, new AtomicReference[Boolean](false))
    currentUploads.put(id, newUpload)
    newUpload
  }

  def finishedUpload(cu: CurrentUpload): Unit = {
    if (currentUploads.contains(cu.id)) {
      currentUploads.put(cu.id, cu)
    }
  }

  def allCurrentUploads : Iterable[CurrentUpload] = currentUploads.values.toBuffer.sortBy((_:CurrentUpload).started)

  def removeAll(uuids: Iterable[UUID]) : Unit = currentUploads --= uuids

  def remove(uuid: UUID) : Unit = currentUploads -= uuid

  def uploadForId(id: UUID): Option[CurrentUpload] = currentUploads.get(id)

  def newIllegalFile(filename: String, reason: IllegalFileReason): Unit = {
    val u = UUID.randomUUID()
    currentUploads.put(u, FailedUpload(u, Instant.now(), filename, IllegalFile(reason)))
  }

}
