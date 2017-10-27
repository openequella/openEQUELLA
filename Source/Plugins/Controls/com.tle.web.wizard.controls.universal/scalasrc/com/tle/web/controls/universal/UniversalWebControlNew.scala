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

package com.tle.web.controls.universal

import java.util
import java.util.{Collections, UUID}
import javax.inject.Inject

import com.dytech.edge.wizard.beans.control.CustomControl
import com.tle.common.wizard.controls.universal.UniversalSettings
import com.tle.common.wizard.controls.universal.handlers.FileUploadSettings
import com.tle.core.filesystem.staging.service.StagingService
import com.tle.core.guice.Bind
import com.tle.core.mimetypes.MimeTypeService
import com.tle.core.services.FileSystemService
import com.tle.core.workflow.thumbnail.service.ThumbnailService
import com.tle.core.workflow.video.VideoService
import com.tle.web.controls.universal.UniversalWebControlNew._
import com.tle.web.controls.universal.handlers.FileUploadHandlerNew
import com.tle.web.controls.universal.handlers.fileupload.WebFileUploads.{attachmentCreatorForUpload, validateAllFinished}
import com.tle.web.controls.universal.handlers.fileupload._
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.freemarker.annotations.ViewFactory
import com.tle.web.sections.ajax.AjaxGenerator
import com.tle.web.sections.ajax.AjaxGenerator.EffectType
import com.tle.web.sections.ajax.handler.{AjaxFactory, AjaxMethod}
import com.tle.web.sections.annotations.{EventFactory, EventHandlerMethod}
import com.tle.web.sections.equella.annotation.PlugKey
import com.tle.web.sections.equella.component.SelectionsTable
import com.tle.web.sections.equella.component.model.{DynamicSelectionsTableModel, SelectionsTableSelection}
import com.tle.web.sections.equella.render.UnselectLinkRenderer
import com.tle.web.sections.events.RenderEventContext
import com.tle.web.sections.events.js.{BookmarkAndModify, EventGenerator}
import com.tle.web.sections.js.ElementId
import com.tle.web.sections.js.generic.function.PartiallyApply
import com.tle.web.sections.js.generic.{Js, OverrideHandler, ReloadHandler, StatementHandler}
import com.tle.web.sections.js.validators.Confirm
import com.tle.web.sections.render._
import com.tle.web.sections.result.util.KeyLabel
import com.tle.web.sections.standard.annotations.Component
import com.tle.web.sections.standard.model.HtmlLinkState
import com.tle.web.sections.standard.renderers.{DivRenderer, LinkRenderer, SpanRenderer}
import com.tle.web.sections.standard.{FileDrop, Link}
import com.tle.web.sections.{SectionInfo, SectionResult, SectionTree}
import com.tle.web.viewurl.attachments.{AttachmentNode, AttachmentResourceService, AttachmentTreeService}
import com.tle.web.wizard.controls.{AbstractWebControl, CCustomControl, WebControlModel}
import com.tle.web.wizard.impl.WebRepository
import com.tle.web.wizard.render.WizardFreemarkerFactory

import scala.collection.JavaConverters._

class UniversalWebControlModel extends WebControlModel

object UniversalWebControlNew {
  val LABEL_EMPTY_LIST = WebFileUploads.label("list.empty")
  val EDIT_LINK = WebFileUploads.label("list.edit")
  val REPLACE_LINK = WebFileUploads.label("list.replace")
  val DELETE_LINK = WebFileUploads.label("list.delete")
  val DELETE_CONFIRM = WebFileUploads.label("list.delete.confirm")
  val PREVIEW = WebFileUploads.label("list.preview")
  val KEY_HIDDEN_FROM_SUMMARY_NOTE = WebFileUploads.r.key("list.hidden.from.summary")

}

@Bind
class UniversalWebControlNew extends AbstractWebControl[UniversalWebControlModel] {
  def getModelClass = classOf[UniversalWebControlModel]

  @Component
  @PlugKey("list.add") var addLink: Link = _
  @Component(name = "a") var attachmentsTable : SelectionsTable = _
  @Component var fileUpload : FileDrop = _


  @ViewFactory(name = "wizardFreemarkerFactory") var wizardViewFactory : WizardFreemarkerFactory = _
  @EventFactory var events : EventGenerator = _
  @AjaxFactory var ajax : AjaxGenerator = _
  @Inject var dialog : UniversalResourcesDialog = _

  @Inject var mimeTypeService : MimeTypeService = _
  @Inject var fileSystemService : FileSystemService = _
  @Inject var attachmentTreeService : AttachmentTreeService = _
  @Inject var attachmentResourceService : AttachmentResourceService = _
  @Inject var thumbnailService : ThumbnailService = _
  @Inject var videoService : VideoService = _

  var ctx : AfterRegister = _

  def renderHtml(context: RenderEventContext): SectionResult = ctx.renderControl(context)

  override def validate(): Unit = ctx.validate()

  protected def getIdForLabel: ElementId = addLink

  override def registered(id: String, tree: SectionTree): Unit = {
    super.registered(id, tree)
    ctx = new AfterRegister(id, tree, getWrappedControl.asInstanceOf[CCustomControl])
  }

  @EventHandlerMethod def reloaded(info: SectionInfo): Unit = {
    WebFileUploads.removeFailedUploads(ctx, None)
    ctx.repository.getState.getWizardMetadataMapper.setMapNow(true)
  }

  @EventHandlerMethod def finishedUpload(info: SectionInfo, uploadId: UUID): Unit =
    ctx.validateAndAddFinished(info, uploadId)

  @EventHandlerMethod def cancelUpload(info: SectionInfo, uploadId: UUID): Unit =
    ctx.state.uploadForId(uploadId).foreach { cu =>
      WebFileUploads.cleanupForUpload(ctx, cu).apply(ctx.stagingContext)
      ctx.state.remove(uploadId)
    }

  @EventHandlerMethod def illegalFile(info: SectionInfo, filename: String, reason: String): Unit =
    ctx.state.newIllegalFile(filename, IllegalFileReason.fromString(reason))

  @AjaxMethod def uploadFile(info: SectionInfo): SectionRenderable = WebFileUploads.ajaxUpload(info, ctx, fileUpload)

  @EventHandlerMethod def delete(info: SectionInfo, attachmentUuid: String): Unit =
    dialog.deleteAttachment(info, attachmentUuid)

  class AfterRegister(id: String, tree: SectionTree, storageControl: CCustomControl) extends ControlContext with RenderHelper {
    val state = new FileUploadState
    val definition = new UniversalSettings(control.getControlBean.asInstanceOf[CustomControl])
    val repository = control.getRepository.asInstanceOf[WebRepository]
    val fileSettingsO = if (definition.getAttachmentTypes.contains("fileHandler")) Some(new FileUploadSettings(definition)) else None

    dialog.setRepository(repository)
    dialog.setDefinition(definition)
    dialog.setStorageControl(storageControl)
    dialog.setPreferredId(tree.getSubId(id, "dialog"))
    tree.registerInnerSection(dialog, id)
    dialog.setOkHandler(new ReloadHandler)
    dialog.setReloadFunction(getReloadFunction(true, events.getEventHandler("reloaded")))

    addLink.setClickHandler(new OverrideHandler(dialog.getOpenFunction, "", ""))
    addLink.setDisablable(true)

    attachmentsTable.setNothingSelectedText(LABEL_EMPTY_LIST)
    attachmentsTable.setAddAction(addLink)
    attachmentsTable.setSelectionsModel(new AttachmentsModel)
    attachmentsTable.setFilterable(false)

    val deleteFunc = ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("delete"), ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), id)
    val cancelFunc = ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("cancelUpload"), ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), id)

    lazy val validateFile = {
      val errorCallback = PartiallyApply.partial(getReloadFunction(true, events.getEventHandler("illegalFile")), 2)
      val doneCallback = PartiallyApply.partial(getReloadFunction(true, events.getEventHandler("finishedUpload")), 1)
      val startedUpload = PartiallyApply.partial(WebFileUploads.ADD_ATTACHMENT_FUNC, 3, id, cancelFunc)
      WebFileUploads.validateFunc(controlSettings, errorCallback, startedUpload, doneCallback)
    }

    def controlSettings: FileUploadSettings = fileSettingsO.get

    def controlState = dialog

    class UniversalRenderModel(info: SectionInfo) extends WebControlModel {
      def getId = id
      def isShowFileUpload = fileSettingsO.isDefined && repo.isEditable
      val isCanAdd = {
        val uploadedAttachments = getAttachments.size
        (definition.isMultipleSelection || attachmentsTable.getSelectionsModel.getSelections(info).isEmpty) &&
          (!definition.isMaxFilesEnabled || uploadedAttachments < definition.getMaxFiles)
      }
      def getAddLink = addLink
      def getAttachmentsTable = attachmentsTable
      def getFileUpload = fileUpload
      val getUploadProblem = WebFileUploads.errorMessage(state.allCurrentUploads).orNull
    }

    def validate() : Unit = {
      val uploadedAttachments = getAttachments.size
      if (definition.isMaxFilesEnabled && uploadedAttachments > definition.getMaxFiles) {
        setInvalid(true, new KeyLabel("wizard.controls.file.toomanyattachments", definition.getMaxFiles.asInstanceOf[Object], (uploadedAttachments - definition.getMaxFiles).asInstanceOf[Object]))
      }
    }

    def getAttachments =
      dialog.getAttachments.asScala

    def renderControl(context: RenderEventContext): SectionResult = {
      val m = new UniversalRenderModel(context)
      addLink.setDisplayed(context, m.isCanAdd)
      if (m.isShowFileUpload) {
        fileUpload.setAjaxUploadUrl(context, new BookmarkAndModify(context, ajax.getModifier("uploadFile")))
        fileUpload.setValidateFile(context, validateFile)
      }
      if (m.isCanAdd) addDisabler(context, addLink)
      wizardViewFactory.createWizardResult(renderModel("universalattachmentlist.ftl", m), UniversalWebControlNew.this)
    }

    def mimeTypeForFilename(name: String): String = mimeTypeService.getMimeTypeForFilename(name)

    val stagingContext = new FileStagingContext(Option(repo.getStagingid), repo.getItem.getItemId, fileSystemService,
      thumbnailService, videoService, mimeTypeService, repository)

    def viewFactory: FreemarkerFactory = wizardViewFactory

    def validateAndAddFinished(info: SectionInfo, apartFrom: UUID): Unit = {
      WebFileUploads.removeFailedUploads(this, Some(apartFrom))
      validateAllFinished(info, this)
      val uploads = state.allCurrentUploads
      val processed = uploads.collect {
        case v: ValidatedUpload =>
          val create = attachmentCreatorForUpload(info, this, v)
          val a = create.create(stagingContext)
          val uuid = UUID.randomUUID().toString
          a.setUuid(uuid)
          controlState.addAttachment(info, a)
          controlState.addMetadataUuid(info, uuid)
          v.id
      }
      state.removeAll(processed)
    }

    class AttachmentsModel extends DynamicSelectionsTableModel[AttachmentNode] {


      override def getSelections(info: SectionInfo): java.util.List[SelectionsTableSelection] = {

        def convertNode(indent: Int)(attachmentNode: AttachmentNode): List[SelectionsTableSelection] = {
          val attachment = attachmentNode.getAttachment
          val selection = new SelectionsTableSelection
          val viewableResource = attachmentResourceService.getViewableResource(info, repository.getViewableItem, attachment)

          val attachmentHandler = dialog.findHandlerForAttachment(attachment)
          val hiddenFromSummary = attachmentHandler != null && attachmentHandler.isHiddenFromSummary(attachment)

          val vurl = viewableResource.createDefaultViewerUrl
          val view = new HtmlLinkState(vurl)
          val viewLink = new LinkRenderer(view)
          viewLink.setTarget("_blank")

          selection.setViewAction(if (attachment.isPreview)
            CombinedRenderer.combineResults(viewLink, new SpanRenderer(PREVIEW).addClass("preview-tag")) else viewLink)

          val _descriptionLabel = new TextLabel(attachment.getDescription)
          val uuid = attachment.getUuid

          val actions = if (storageControl.isEnabled) {
            // This is bollocks in order to not refactor
            // the whole universal control
            val firstActions = if (indent == 0) {
              List(makeAction(EDIT_LINK, new OverrideHandler(dialog.getOpenFunction, "", uuid)),
                makeAction(REPLACE_LINK, new OverrideHandler(dialog.getOpenFunction, uuid, "")))
            } else {
              viewLink.addClass("indent")
              List.empty[SectionRenderable]
            }
            firstActions :+ makeAction(DELETE_LINK, new OverrideHandler(deleteFunc, uuid).addValidator(new Confirm(DELETE_CONFIRM)))
          } else Nil
          selection.setActions(actions.asJava)
          viewLink.setLabel(if (hiddenFromSummary) new KeyLabel(KEY_HIDDEN_FROM_SUMMARY_NOTE, _descriptionLabel) else _descriptionLabel)
          selection :: attachmentNode.getChildren.asScala.toList.flatMap(convertNode(indent+1))
        }

        val source: java.util.List[AttachmentNode] = attachmentTreeService.getTreeStructure(getAttachments.asJava, false)
        (source.asScala.flatMap(convertNode(0)) ++ uploadEntries(info).toBuffer).asJava
      }


      def uploadEntries(info: SectionInfo) = {
        state.allCurrentUploads.collect {
          case uf: UploadingFile =>
            val ts = new SelectionsTableSelection
            val dts = new TagState()
            dts.setId("u"+uf.id)
            dts.addClass("progress-bar")
            val nameProgress = CombinedRenderer.combineMultipleResults(new LabelRenderer(new TextLabel(uf.originalFilename)), new DivRenderer(dts))
            ts.setViewAction(nameProgress)
            val rem = new HtmlLinkState(new StatementHandler(cancelFunc, uf.id.toString))
            ts.setActions(Collections.singletonList(new UnselectLinkRenderer(rem, FileUploadHandlerNew.LABEL_CANCEL_UPLOAD)))
            ts
        }
      }

      protected def getSourceList(info: SectionInfo): util.Collection[AttachmentNode] = sys.error("Don't call")

      protected def transform(info: SectionInfo, selection: SelectionsTableSelection, thing: AttachmentNode, actions: util.List[SectionRenderable], index: Int): Unit = sys.error("Don't call")
    }


  }

}