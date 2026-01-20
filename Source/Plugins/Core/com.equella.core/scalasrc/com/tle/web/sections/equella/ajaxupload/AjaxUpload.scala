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

package com.tle.web.sections.equella.ajaxupload

import java.io.{FilterInputStream, InputStream}
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference

import com.dytech.edge.common.FileInfo
import com.tle.web.resources.ResourcesService
import com.tle.web.sections.jquery.JQuerySelector
import com.tle.web.sections.jquery.libraries.JQueryProgression
import com.tle.web.sections.js.{JSAssignable, JSExpression}
import com.tle.web.sections.js.generic.Js
import com.tle.web.sections.js.generic.expression.FunctionCallExpression
import com.tle.web.sections.js.generic.function.{
  AnonymousFunction,
  ExternallyDefinedFunction,
  IncludeFile,
  PartiallyApply
}
import com.tle.web.sections.js.generic.statement.ReloadStatement
import com.tle.web.sections.render.CssInclude
import com.tle.web.upload.StreamKilledException
import io.circe.generic.extras._
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}

import scala.beans.BeanProperty
import scala.util.Try

case class AjaxFileEntry(
    id: String,
    name: String,
    link: String,
    editable: Boolean,
    preview: Boolean,
    children: Iterable[AjaxFileEntry]
)

object AjaxFileEntry {
  implicit val config                            = Configuration.default
  implicit val feEncoder: Encoder[AjaxFileEntry] = deriveEncoder
}

sealed trait AjaxUploadCommand
case class NewUpload(filename: String, size: Long) extends AjaxUploadCommand
case class Delete(id: String)                      extends AjaxUploadCommand

sealed trait AjaxUploadResponse
case class NewUploadResponse(uploadUrl: String, id: String, name: String) extends AjaxUploadResponse
case class UploadFailed(reason: String)                                   extends AjaxUploadResponse
case class AddEntries(entries: Iterable[AjaxFileEntry])                   extends AjaxUploadResponse
case class UpdateEntry(
    entry: AjaxFileEntry,
    attachmentDuplicateInfo: Option[AttachmentDuplicateInfo]
) extends AjaxUploadResponse
case class RemoveEntries(
    ids: Iterable[String],
    attachmentDuplicateInfo: Option[AttachmentDuplicateInfo]
) extends AjaxUploadResponse

// This class as part of AjaxResponse includes if duplicates are found and the attachment control's parent id
case class AttachmentDuplicateInfo(displayWarningMessage: Boolean, warningMessageWebId: String) {}

object AttachmentDuplicateInfo {
  implicit val config                                                       = Configuration.default
  implicit val attachmentDuplicateEncoder: Encoder[AttachmentDuplicateInfo] = deriveEncoder
}

object AjaxUploadCommand {
  implicit val config = Configuration.default
    .withDiscriminator("command")
    .copy(transformConstructorNames = _.toLowerCase)
  implicit val aucDecoder: Decoder[AjaxUploadCommand] = deriveDecoder
}

object AjaxUploadResponse {
  implicit val config = Configuration.default
    .withDiscriminator("response")
    .copy(transformConstructorNames = _.toLowerCase)
  implicit val aucrEncoder: Encoder[AjaxUploadResponse] = deriveEncoder
}

object AjaxUpload {
  val r           = ResourcesService.getResourceHelper(getClass)
  val CSS_INCLUDE = new CssInclude(r.url("css/render/ajaxupload.css"))
  private val INCLUDE =
    new IncludeFile(r.url("scripts/render/ajaxupload.js"), CSS_INCLUDE, JQueryProgression.PRERENDER)
  private val FILE_UPLOAD_HANDLER_CLASS = new ExternallyDefinedFunction("AjaxUploads", INCLUDE)
  private val VALIDATE_FUNC =
    new ExternallyDefinedFunction(FILE_UPLOAD_HANDLER_CLASS, "validateFile", 5)
  private val ADD_UPLOAD_FUNC =
    new ExternallyDefinedFunction(FILE_UPLOAD_HANDLER_CLASS, "addUploadEntry", 4)
  private val PROGESSONLY_UPLOAD_FUNC =
    new ExternallyDefinedFunction(FILE_UPLOAD_HANDLER_CLASS, "simpleUploadEntry", 4)

  def createValidate(
      maxSize: Long,
      mimeTypes: java.util.List[String],
      errorCallback: JSExpression,
      startedUpload: JSExpression,
      doneCallback: JSExpression
  ) =
    Js.call(
      AjaxUpload.VALIDATE_FUNC,
      maxSize.asInstanceOf[Object],
      mimeTypes,
      errorCallback,
      startedUpload,
      doneCallback
    )

  def createProgressFunc(jquery: JQuerySelector, cancelCallback: Object) =
    PartiallyApply.partial(ADD_UPLOAD_FUNC, 3, jquery, cancelCallback)

  def createSimpleProgressFunc(jquery: JQuerySelector, cancelCallback: Object) =
    PartiallyApply.partial(PROGESSONLY_UPLOAD_FUNC, 3, jquery, cancelCallback)

  def writeCancellableStream(
      writeFile: InputStream => FileInfo,
      stream: InputStream,
      cancelled: AtomicReference[Boolean]
  ): Try[FileInfo] = {
    val str = new FilterInputStream(stream) {
      def checkCancelled(): Unit = {
        if (cancelled.get) throw new StreamKilledException
      }

      override def read: Int = {
        checkCancelled()
        super.read
      }

      override def read(b: Array[Byte], off: Int, len: Int): Int = {
        checkCancelled()
        super.read(b, off, len)
      }
    }
    Try(writeFile(str))
  }

  def simpleUploadValidator(id: String, completed: JSExpression): JSAssignable = {
    val startedUpload =
      createSimpleProgressFunc(new JQuerySelector(JQuerySelector.Type.ID, id), null)

    Js.functionValue(
      AjaxUpload
        .createValidate(0, Collections.emptyList[String], completed, startedUpload, completed)
    )
  }

}
