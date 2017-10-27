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

package com.tle.web.workflow.tasks.dialog

import java.util
import java.util.{Collections, UUID}
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

import com.dytech.edge.exceptions.BannedFileException
import com.tle.common.Check
import com.tle.common.filesystem.handle.StagingFile
import com.tle.core.filesystem.staging.service.StagingService
import com.tle.core.services.FileSystemService
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.freemarker.annotations.ViewFactory
import com.tle.web.resources.ResourcesService
import com.tle.web.sections.ajax.AjaxGenerator
import com.tle.web.sections.ajax.handler.{AjaxMethod, UpdateDomFunction}
import com.tle.web.sections.annotations.{Bookmarked, EventHandlerMethod}
import com.tle.web.sections.equella.ajaxupload.AjaxUpload
import com.tle.web.sections.equella.annotation.PlugKey
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog
import com.tle.web.sections.equella.render.{ButtonRenderer, UnselectLinkRenderer}
import com.tle.web.sections.events.js.BookmarkAndModify
import com.tle.web.sections.events.{ReadyToRespondListener, RenderContext}
import com.tle.web.sections.jquery.JQuerySelector
import com.tle.web.sections.js.generic.function.{PartiallyApply, PassThroughFunction, RuntimeFunction}
import com.tle.web.sections.js.generic.{Js, StatementHandler}
import com.tle.web.sections.js.{JSAssignable, JSCallable}
import com.tle.web.sections.render.{Label, SectionRenderable, TextLabel}
import com.tle.web.sections.result.util.KeyLabel
import com.tle.web.sections.standard.annotations.Component
import com.tle.web.sections.standard.dialog.model.DialogModel
import com.tle.web.sections.standard.model.{HtmlComponentState, HtmlLinkState, SimpleBookmark}
import com.tle.web.sections.standard.renderers.DivRenderer
import com.tle.web.sections.standard.{Button, FileDrop, TextField}
import com.tle.web.sections.{SectionInfo, SectionTree}
import com.tle.web.workflow.servlet.WorkflowMessageServlet
import com.tle.web.workflow.tasks.CurrentTaskSection

import scala.collection.JavaConverters._
import scala.util.{Failure, Success}

case class UploadedFile(getLink: HtmlLinkState, getCancel: UnselectLinkRenderer, getProgressDiv: SectionRenderable)

class TaskActionDialogModel extends DialogModel {
  @Bookmarked var stagingFolderUuid: String = null
  var getStagingFiles: util.List[UploadedFile] = null
  var getErrorMessage: Label = null
}

object AbstractTaskActionDialog
{
  val r = ResourcesService.getResourceHelper(getClass)
  def l(key: String) = new KeyLabel(r.key(key))
  def error(k: String) = l("command.taskaction.error."+k)
  val LABEL_ATTACHED_FILES = l("command.taskaction.attachedfiles")
  val LABEL_MUST_MESSAGE = l("command.taskaction.mustmessage")
  val MAX_FILESIZE = 1024 * 1024 * 1024
}

abstract class AbstractTaskActionDialog extends AbstractOkayableDialog[TaskActionDialogModel] with ReadyToRespondListener {
  import AbstractTaskActionDialog._

  setAjax(true)
  var successCallable : JSCallable = _
  var removeCallback : UpdateDomFunction = _
  var cancelFunc : UpdateDomFunction = _
  var validateFile : JSAssignable = _

  @Inject protected var fileSystemService: FileSystemService = _
  @Inject protected var stagingService: StagingService = _

  @Component(name="commentField") var _commentField : TextField = _
  @Component(name = "fd")
  @PlugKey("comments.upload") var _fileDrop : FileDrop = _
  @Component(name = "c")
  @PlugKey("command.taskaction.cancel") var cancelButton : Button = _

  @ViewFactory protected var viewFactory: FreemarkerFactory = _

  override def instantiateDialogModel(info: SectionInfo) = new TaskActionDialogModel

  def getButtonType: ButtonRenderer.ButtonType

  def getButtonLabel: Label

  def getActionType: CurrentTaskSection.CommentType

  def getPostCommentHeading: Label

  private val currentUploads = new ConcurrentHashMap[UUID, (String, AtomicReference[Boolean], Option[Label])]().asScala

  protected def getWorkflowStepTarget(info: SectionInfo): String = null

  override def getWidth = "696px"

  protected def setupModelForRender(context: RenderContext): Unit = {
    val model = getModel(context)
    val stagingFolderUuid = model.stagingFolderUuid
    val fileEntries = fileSystemService.enumerate(new StagingFile(stagingFolderUuid), null, null)
    val files = fileEntries.zipWithIndex.flatMap {
      case (fileEntry, index) =>
        val filename = fileEntry.getName
        val shouldShow = currentUploads.find(_._2._1 == filename) match {
          case None => Some((index.toString, true, new StatementHandler(removeCallback, filename)))
          case Some((uuid, (fn, ar, err))) if !ar.get() => Some((uuid, false, new StatementHandler(cancelFunc, uuid)))
          case _ => None
        }
        shouldShow.map { case (id, finished, removeHandler) =>
          val link = new HtmlLinkState(new TextLabel(filename), new SimpleBookmark(WorkflowMessageServlet.stagingUrl(stagingFolderUuid, filename)))
          link.setTarget("_blank")
          val remove = new HtmlLinkState
          remove.setClickHandler(removeHandler)
          val cancel = new UnselectLinkRenderer(remove, new TextLabel(""))
          val progressDivState = new HtmlComponentState
          val progressDiv = new DivRenderer(progressDivState)
          progressDivState.setId("u" + id)
          if (finished) {
            val inner = new DivRenderer("")
            inner.addClass("progress-bar-inner complete")
            progressDiv.setNestedRenderable(inner)
          }
          UploadedFile(link, cancel, progressDiv)
        }
    }
    currentUploads --= currentUploads.collect {
      case (u, (_,_, Some(m))) =>
        model.getErrorMessage = m
        u
    }
    model.getStagingFiles = files.toList.asJava
    _fileDrop.setAjaxUploadUrl(context, new BookmarkAndModify(context, ajaxEvents.getModifier("dndUpload")))
    _fileDrop.setValidateFile(context, validateFile)
  }

  override protected def getRenderableContents(context: RenderContext): SectionRenderable = {
    setupModelForRender(context)
    viewFactory.createResult("dialog/taskaction.ftl", this)
  }

  override protected def getContentBodyClass(context: RenderContext) = "taskactiondialog"

  override def readyToRespond(info: SectionInfo, redirect: Boolean): Unit = {
    super.readyToRespond(info, redirect)
    val model = getModel(info)
    if (model.isShowing) {
      var stagingFolderUuid = model.stagingFolderUuid
      if (stagingFolderUuid == null) {
        stagingFolderUuid = stagingService.createStagingArea.getUuid
        model.stagingFolderUuid = stagingFolderUuid
      }
    }
  }

  def validate(info: SectionInfo): Label

  def isMandatoryMessage : Boolean

  def validateHasMessage(info: SectionInfo): Label = {
    if (Check.isEmpty(_commentField.getValue(info).trim()))
    {
      AbstractTaskActionDialog.LABEL_MUST_MESSAGE
    } else null
  }

  def setSuccessCallable(successCallable : JSCallable) = this.successCallable = addParentCallable(successCallable)


  @EventHandlerMethod def ok(info: SectionInfo): Unit = {
    val model = getModel(info)
    val stagingUuid = model.stagingFolderUuid
    val errorMessage = validate(info)
    if (errorMessage == null) {
      val currentTaskSection : CurrentTaskSection = info.lookupSection(classOf[CurrentTaskSection])
      currentTaskSection.doComment(info, getActionType, getWorkflowStepTarget(info), _commentField.getValue(info), stagingUuid)
      closeDialog(info, successCallable, getActionType)
    }
    else {
      info.preventGET()
      model.getErrorMessage = errorMessage
    }
  }

  @EventHandlerMethod def cancelled(info: SectionInfo): Unit = {
    val temp = new StagingFile(getModel(info).stagingFolderUuid)
    stagingService.removeStagingArea(temp, true)
    closeDialog(info)
  }

  override def getOkLabel = getButtonLabel
  def getAttachedFilesLabel: Label = LABEL_ATTACHED_FILES

  def getCommentField : TextField = _commentField
  def getFileDrop : FileDrop = _fileDrop

  @AjaxMethod
  def dndUpload(info: SectionInfo): Boolean = {
    val model = getModel(info)
    val uploadId = UUID.fromString(info.getRequest.getHeader("X_UUID"))
    val stagingFolderUuid = model.stagingFolderUuid
    val filename = _fileDrop.getFilename(info)
    val fn = if (!Check.isEmpty(filename)) filename.toLowerCase else ""
    val stream = _fileDrop.getInputStream(info)
    val staging = new StagingFile(stagingFolderUuid)
    val uploadRef = new AtomicReference[Boolean](false)
    currentUploads.update(uploadId, (fn, uploadRef, None))
    // overwrite existing file
    if (fileSystemService.fileExists(staging, fn))
      fileSystemService.removeFile(staging, fn)
    AjaxUpload.writeCancellableStream(s => fileSystemService.write(staging, fn, s, false), stream, uploadRef) match {
      case Success(_) => currentUploads.remove(uploadId)
      case Failure(f) => fileSystemService.removeFile(staging, fn)
        f match {
          case bfe: BannedFileException => currentUploads.put(uploadId, (fn, uploadRef, Some(error("banned"))))
          case _ => currentUploads.remove(uploadId)
        }
    }
    true
  }

  @EventHandlerMethod
  def removeUploadedFile(info: SectionInfo, filename: String): Unit = {
    val staging = new StagingFile(getModel(info).stagingFolderUuid)
    fileSystemService.removeFile(staging, filename)
  }

  @EventHandlerMethod
  def illegalFile(info: SectionInfo, filename: String, reason: String): Unit = {
    getModel(info).getErrorMessage = error(reason)
  }

  @EventHandlerMethod
  def cancelFile(info: SectionInfo, uuid: UUID): Unit = {
    currentUploads.get(uuid).foreach {
      case (fn, ar, _) => ar.set(true)
    }

  }



  override def treeFinished(id: String, tree: SectionTree): Unit = {
    setOkHandler(events.getSubmitValuesHandler("ok"))
    setCancelHandler(new StatementHandler(Js.call_s(new RuntimeFunction() {
      override protected def createFunction(info: RenderContext) = new PassThroughFunction("cl", events.getSubmitValuesFunction("cancelled"))
    })))
    cancelButton.setClickHandler(events.getSubmitValuesHandler("cancelled"))
    val divs = Seq("uploads", "comment-error")
    val updateProgressArea = ajaxEvents.getAjaxUpdateDomFunction(tree, this, null,
      ajaxEvents.getEffectFunction(AjaxGenerator.EffectType.REPLACE_IN_PLACE), divs: _*)

    removeCallback = ajaxEvents.getAjaxUpdateDomFunction(tree, this,
      events.getEventHandler("removeUploadedFile"), ajaxEvents.getEffectFunction(AjaxGenerator.EffectType.REPLACE_IN_PLACE), divs: _*)

    cancelFunc = ajaxEvents.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("cancelFile"),
      ajaxEvents.getEffectFunction(AjaxGenerator.EffectType.REPLACE_IN_PLACE), divs: _*)

    val errorCallback = PartiallyApply.partial(events.getSubmitValuesFunction("illegalFile"), 2)
    val startedUpload = AjaxUpload.createProgressFunc(new JQuerySelector(JQuerySelector.Type.RAW, "#uploads .uploadsprogress"), cancelFunc)
    validateFile = Js.functionValue(AjaxUpload.createValidate(MAX_FILESIZE, Collections.emptyList[String],
      errorCallback, startedUpload, PartiallyApply.partial(updateProgressArea, 0)))
    super.treeFinished(id, tree)
  }

  override protected def collectFooterActions(context: RenderContext): util.Collection[Button] = {
    val buttons = new util.ArrayList[Button]
    buttons.add(getOk)
    buttons.add(cancelButton)
    buttons
  }
}
