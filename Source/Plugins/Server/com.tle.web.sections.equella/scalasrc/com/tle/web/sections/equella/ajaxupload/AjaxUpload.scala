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

package com.tle.web.sections.equella.ajaxupload

import java.io.{FilterInputStream, InputStream}
import java.util.concurrent.atomic.AtomicReference

import com.dytech.edge.common.FileInfo
import com.tle.web.resources.ResourcesService
import com.tle.web.sections.jquery.JQuerySelector
import com.tle.web.sections.js.JSExpression
import com.tle.web.sections.js.generic.Js
import com.tle.web.sections.js.generic.function.{ExternallyDefinedFunction, IncludeFile, PartiallyApply}
import com.tle.web.sections.render.CssInclude
import com.tle.web.upload.StreamKilledException

import scala.util.Try

object AjaxUpload {
  val r = ResourcesService.getResourceHelper(getClass)
  private val CSS_INCLUDE = new CssInclude(r.url("css/render/ajaxupload.css"))
  private val INCLUDE = new IncludeFile(r.url("scripts/render/ajaxupload.js"), CSS_INCLUDE)
  private val FILE_UPLOAD_HANDLER_CLASS = new ExternallyDefinedFunction("AjaxUploads", INCLUDE)
  private val VALIDATE_FUNC = new ExternallyDefinedFunction(FILE_UPLOAD_HANDLER_CLASS, "validateFile", 5)
  private val ADD_UPLOAD_FUNC = new ExternallyDefinedFunction(FILE_UPLOAD_HANDLER_CLASS, "addUploadEntry", 4)

  def createValidate(maxSize: Long, mimeTypes: java.util.List[String], errorCallback: JSExpression,
                     startedUpload: JSExpression, doneCallback: JSExpression) = Js.call(AjaxUpload.VALIDATE_FUNC,
    maxSize.asInstanceOf[Object], mimeTypes, errorCallback, startedUpload, doneCallback)

  def createProgressFunc(jquery: JQuerySelector, cancelCallback: Object) =
    PartiallyApply.partial(ADD_UPLOAD_FUNC, 3, jquery, cancelCallback)

  def writeCancellableStream(writeFile: InputStream => FileInfo, stream: InputStream, cancelled: AtomicReference[Boolean]): Try[FileInfo] = {
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

}
