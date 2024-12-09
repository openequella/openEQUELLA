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

package com.tle.web.controls.universal.handlers.fileupload.details

import java.util
import java.util.UUID
import com.tle.beans.item.attachments.{Attachment, FileAttachment, ZipAttachment}
import com.tle.common.NameValue
import com.tle.common.filesystem.FileEntry
import com.tle.web.controls.universal.handlers.fileupload.details.FileEditDetails._
import com.tle.web.controls.universal.handlers.fileupload.{AttachmentDelete, WebFileUploads}
import com.tle.web.controls.universal.{
  AbstractDetailsAttachmentHandler,
  ControlContext,
  DialogRenderOptions,
  RenderHelper
}
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.freemarker.annotations.ViewFactory
import com.tle.web.sections.ajax.AjaxGenerator
import com.tle.web.sections.ajax.handler.{AjaxFactory, AjaxMethod}
import com.tle.web.sections.annotations.{Bookmarked, EventFactory, EventHandlerMethod}
import com.tle.web.sections.equella.AbstractScalaSection
import com.tle.web.sections.equella.annotation.PlugKey
import com.tle.web.sections.events.RenderContext
import com.tle.web.sections.events.js.{EventGenerator, JSHandler}
import com.tle.web.sections.js.generic.expression.{ObjectExpression, ScriptVariable}
import com.tle.web.sections.js.generic.function.{ExternallyDefinedFunction, IncludeFile}
import com.tle.web.sections.js.generic.statement.AssignStatement
import com.tle.web.sections.js.generic.{Js, OverrideHandler}
import com.tle.web.sections.render.{Label, SectionRenderable, TextLabel}
import com.tle.web.sections.standard._
import com.tle.web.sections.standard.annotations.Component
import com.tle.web.sections.standard.model.HtmlBooleanState
import com.tle.web.sections.{SectionInfo, SectionTree}
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters._

object FileEditDetails {
  private val CSS_CLASS_FOLDER = "folder"
  private val CSS_CLASS_FILE   = "file"

  private val INCLUDE         = new IncludeFile(WebFileUploads.r.url("scripts/file/zip.js"))
  private val SELECT_ALL      = new ExternallyDefinedFunction("zipSelectAll", INCLUDE)
  private val SELECT_FUNCTION = new ExternallyDefinedFunction("zipSelect", INCLUDE)

  private val SELECTION_TREE = new ScriptVariable("zipTree")
}

class FileEditDetails(
    parentId: String,
    tree: SectionTree,
    ctx: ControlContext,
    zipHandler: ZipHandler,
    editingHandler: EditingHandler,
    viewerHandler: ViewerHandler,
    showRestrict: Boolean,
    val editingAttachment: SectionInfo => Attachment
) extends AbstractScalaSection
    with RenderHelper
    with DetailsPage {

  type M = FileEditDetailsModel
  tree.registerSubInnerSection(this, parentId)
  val settings = ctx.controlSettings

  @Component var displayName: TextField           = _
  @ViewFactory var viewFactory: FreemarkerFactory = _
  @EventFactory var events: EventGenerator        = _
  @AjaxFactory var ajax: AjaxGenerator            = _

  @Component(name = "e") var editFileDiv: Div = _

  @Component
  @PlugKey("handlers.file.details.unzipfile") var executeUnzip: Button = _
  @Component
  @PlugKey("handlers.file.details.removeunzipped") var removeUnzip: Button = _
  @Component var selections: MappedBooleans                                = _
  @Component var zipProgressDiv: Div                                       = _
  @Component var fileListDiv: Div                                          = _
  @Component
  @PlugKey("handlers.file.zipdetails.link.selectall") var selectAll: Link = _
  @Component
  @PlugKey("handlers.file.zipdetails.link.selectnone") var selectNone: Link = _
  @Component var attachZip: Checkbox                                        = _
  @Component var viewers: SingleSelectionList[NameValue]                    = _
  @Component var previewCheckBox: Checkbox                                  = _
  @Component var restrictCheckbox: Checkbox                                 = _
  @Component(name = "st") var suppressThumbnails: Checkbox                  = _

  var saveClickHandler: JSHandler = _

  @EventHandlerMethod def unzipFile(info: SectionInfo): Unit = {
    if (!zipHandler.unzipped) {
      attachZip.setChecked(info, true)
      zipHandler.unzip
    }
  }

  @EventHandlerMethod def removeZip(info: SectionInfo): Unit = {
    val m = getModel(info)
    if (zipHandler.unzipped) {
      zipHandler.removeUnzipped()
    }
  }

  @EventHandlerMethod def editFile(info: SectionInfo, openWith: Boolean): Unit = {
    val model = getModel(info)
    model.appletMode = if (openWith) "openwith" else "open"
  }

  @EventHandlerMethod def save(info: SectionInfo): Unit = {
    ctx.controlState.save(info)
  }

  class ZipJson(
      @BeanProperty val total: Int,
      @BeanProperty val upto: Int,
      @BeanProperty val finished: Boolean
  )

  @AjaxMethod def zipProgress(info: SectionInfo): ZipJson = {
    zipHandler.zipProgress
      .map(zp => new ZipJson(zp.getTotalFiles, zp.getCurrentFile, zp.isFinished))
      .getOrElse(new ZipJson(0, 0, true))
  }

  override def getDefaultPropertyName = "fd"

  override def registered(id: String, tree: SectionTree): Unit = {
    super.registered(id, tree)

    val editFileAjaxFunction = ajax.getAjaxUpdateDomFunction(
      tree,
      this,
      events.getEventHandler("editFile"),
      "editFileAjaxDiv"
    )

    saveClickHandler = new OverrideHandler(events.getSubmitValuesFunction("save"))

    executeUnzip.setClickHandler(events.getNamedHandler("unzipFile"))
    removeUnzip.setClickHandler(events.getNamedHandler("removeZip"))
    selectAll.setClickHandler(new OverrideHandler(Js.call_s(SELECT_ALL, true.asInstanceOf[Object])))
    selectNone.setClickHandler(
      new OverrideHandler(Js.call_s(SELECT_ALL, false.asInstanceOf[Object]))
    )
    viewers.setListModel(viewerHandler.viewerListModel)
    zipProgressDiv.addReadyStatements(
      WebFileUploads.ZIP_PROGRESS_FUNC,
      ajax.getAjaxFunction("zipProgress"),
      ajax.getAjaxUpdateDomFunction(
        tree,
        this,
        null,
        ajax.getEffectFunction(AjaxGenerator.EffectType.REPLACE_IN_PLACE),
        "zipArea"
      )
    )
  }

  def newModel = FileEditDetailsModel.apply

  case class FileEditDetailsModel(info: SectionInfo) {
    @Bookmarked(name = "am")
    var appletMode: String = _

    lazy val a = editingAttachment(info)

    def getCommonIncludePath = AbstractDetailsAttachmentHandler.COMMON_INCLUDE_PATH

    def getCommonPrefix = AbstractDetailsAttachmentHandler.COMMON_PREFIX

    var validate = false

    def getEditTitle = new TextLabel(displayName.getValue(info))

    def getDisplayName = displayName

    lazy val getErrors =
      (if (validate) validateDisplayName(info).toMap else Map.empty[String, Label]).asJava

    def isShowViewers = viewers.getListModel.getOptions(info).size() != 2

    def isShowPreview = ctx.controlSettings.isAllowPreviews

    def isShowRestrict = showRestrict

    def getPreviewCheckBox = previewCheckBox

    def getRestrictCheckbox = restrictCheckbox

    def isShowFileConversion = false

    def isShowThumbnailOption = settings.isShowThumbOption

    def getSuppressThumbnails = suppressThumbnails

    def getViewers = viewers

    def getEditFileDiv = editFileDiv

    def getRemoveUnzip = removeUnzip

    def isZipFile = isUnzipped || (!settings.isNoUnzip && WebFileUploads.isZipFile(a))

    def getExecuteUnzip = executeUnzip

    lazy val getFileListDiv = {
      val pa = _files.groupBy(_.parentPath)
      def mkNodeChildren(path: String): ObjectExpression =
        new ObjectExpression(
          "folder",
          false.asInstanceOf[Object],
          "children",
          pa.getOrElse(path, Seq.empty).filter(!_.isFolder).map(_.id).asJavaCollection
        )

      val map = new ObjectExpression("ROOT", mkNodeChildren(""))
      _files.filter(_.isFolder).foreach { e =>
        map.put(e.id, mkNodeChildren(e.getPath))
      }
      fileListDiv.addReadyStatements(info, new AssignStatement(SELECTION_TREE, map))
      fileListDiv
    }

    def getSelections = selections

    def getSelectAll = selectAll

    def getSelectNone = selectNone

    def getAttachZip = attachZip

    def isUnzipped = zipHandler.unzipped

    def isUnzipping = _isUnzipping

    def getZipProgressDiv = zipProgressDiv

    lazy val (_isUnzipping, _files) = {
      val _unzipping = zipHandler.zipProgress.exists(!_.isFinished)
      if (!_unzipping) {
        val allEntries       = zipHandler.unzippedEntries
        val (files, folders) = convertFileList("", "", 1, allEntries)
        (
          false,
          (files ++ folders).zipWithIndex.map { case (efp, i) =>
            val id = "s" + i
            val check =
              if (efp.file) selections.getBooleanState(info, efp.fullpath)
              else {
                val s = new HtmlBooleanState
                s.setClickHandler(Js.handler(Js.call_s(SELECT_FUNCTION, id)))
                s
              }
            check.setId(id)
            new EntryDisplay(efp, check, id)
          }
        )
      } else (true, Seq.empty)
    }

    def getFiles: util.Collection[EntryDisplay] = _files.asJavaCollection
  }

  def renderDetails(context: RenderContext): (SectionRenderable, DialogRenderOptions => Unit) = {
    val m = getModel(context)
    (renderModel("file/file-edit.ftl", m), _.setSaveClickHandler(saveClickHandler))
  }

  def prepareUI(info: SectionInfo): Unit = {
    val et = editingAttachment(info)
    et match {
      case za: ZipAttachment =>
        selections.setCheckedSet(info, zipHandler.selectedAttachments.keySet.asJava)
        attachZip.setChecked(info, za.isAttachZip)
      case _ => ()
    }
    displayName.setValue(info, et.getDescription)
    previewCheckBox.setChecked(info, et.isPreview)
    restrictCheckbox.setChecked(info, et.isRestricted)
    viewers.setSelectedStringValue(info, et.getViewer)
    suppressThumbnails.setChecked(info, WebFileUploads.isSuppressThumbnail(et))
  }

  def editAttachment(
      info: SectionInfo,
      _a: Attachment,
      ctx: ControlContext
  ): (Attachment, Option[AttachmentDelete]) = {

    def copyExtra(src: Attachment, dest: Attachment): Unit = {
      dest.setUuid(src.getUuid)
      dest.setRestricted(src.isRestricted)
      dest.setPreview(src.isPreview)
      dest.setMd5sum(src.getMd5sum)
      dest.setThumbnail(src.getThumbnail)
    }
    val unzipped = zipHandler.unzipped
    val (a, delete) = (_a, unzipped) match {
      case (fa: FileAttachment, true) =>
        val za = new ZipAttachment
        copyExtra(fa, za)
        za.setUrl(WebFileUploads.zipPath(fa.getFilename))
        (za, None)
      case (za: ZipAttachment, false) =>
        val fa = new FileAttachment
        fa.setFilename(WebFileUploads.removeZipPath(za.getUrl))
        copyExtra(za, fa)

        (
          fa,
          Some(
            AttachmentDelete(
              ctx.controlState.getAttachments.asScala.filter(WebFileUploads.isSelectedInZip(za)),
              _ => ()
            )
          )
        )
      case _ => (_a, None)
    }
    if (unzipped) {
      val old           = zipHandler.selectedAttachments
      val oldNames      = old.keySet
      val selectedFiles = selections.getCheckedSet(info).asScala
      val delete        = oldNames -- selectedFiles
      val add           = selectedFiles -- oldNames
      val cs            = ctx.controlState
      add.foreach { fn =>
        val fa       = new FileAttachment
        val fullpath = WebFileUploads.removeZipPath(a.getUrl) + "/" + fn
        fa.setUuid(UUID.randomUUID().toString)
        fa.setData(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID, a.getUuid)
        fa.setFilename(fullpath)
        fa.setDescription(WebFileUploads.filenameOnly(fn))
        fa.setSize(ctx.stagingContext.fileSize(fullpath))
        cs.addAttachment(info, fa)
        cs.addMetadataUuid(info, fa.getUuid)
      }
      old.filterKeys(delete).values.foreach { a =>
        cs.removeAttachment(info, a)
        cs.removeMetadataUuid(info, a.getUuid)
      }
      val za = a.asInstanceOf[ZipAttachment]
      za.setAttachZip(attachZip.isChecked(info))
    } else if (WebFileUploads.isWebPage(a.getUrl)) {
      ctx.repo.getState.getWizardMetadataMapper.addHtmlMappedFile(a.getUrl)
    }
    a.setViewer(viewers.getSelectedValueAsString(info))
    a.setDescription(displayName.getValue(info))
    if (showRestrict) {
      a.setRestricted(restrictCheckbox.isChecked(info))
    }
    if (ctx.controlSettings.isAllowPreviews) {
      a.setPreview(previewCheckBox.isChecked(info))
    }
    if (ctx.controlSettings.isShowThumbOption) {
      val wasSuppressed = WebFileUploads.isSuppressThumbnail(_a)
      val suppressed    = suppressThumbnails.isChecked(info)
      if (suppressed != wasSuppressed) {
        a.setThumbnail(
          if (suppressed) WebFileUploads.SUPPRESS_THUMB_VALUE
          else ctx.stagingContext.thumbRequest(a.getUrl)
        )
      }
    }
    editingHandler.syncEdits(a.getUrl)
    (a, delete)
  }

  def prependParent(parent: String, filename: String, sep: String = "/"): String =
    if (parent.isEmpty) filename else parent + sep + filename

  case class EntryFullPath(parentPath: String, level: Int, entry: FileEntry, displayPath: String) {
    def file     = !entry.isFolder
    def name     = entry.getName
    val fullpath = prependParent(parentPath, name)
  }
  class EntryDisplay(fp: EntryFullPath, check: HtmlBooleanState, val id: String) {
    def parentPath     = fp.parentPath
    def isFolder       = fp.entry.isFolder
    def getLevel       = fp.level
    def getCheck       = check
    def getDisplayPath = fp.displayPath
    def getName        = fp.name
    def getPath        = fp.fullpath
    def getFileClass   = if (isFolder) CSS_CLASS_FOLDER else CSS_CLASS_FILE
  }

  def convertFileList(
      parentPath: String,
      displayParent: String,
      level: Int,
      entries: Seq[FileEntry]
  ): (Seq[EntryFullPath], Seq[EntryFullPath]) = {
    val (files, _folders) = entries
      .map(e => EntryFullPath(parentPath, level, e, prependParent(displayParent, e.getName, " / ")))
      .partition(_.file)
    val folders = _folders.flatMap { fed =>
      val (childFiles, childFolders) =
        convertFileList(
          prependParent(parentPath, fed.name),
          prependParent(displayParent, fed.name, " / "),
          level + 1,
          fed.entry.getFiles.asScala.toSeq
        )
      if (childFiles.isEmpty) childFolders else fed +: (childFiles ++ childFolders)
    }
    (files, folders)
  }

  override def previewable = true

  override def validate(info: SectionInfo): Boolean = {
    val m = getModel(info)
    m.validate = true
    m.getErrors.isEmpty
  }

}
