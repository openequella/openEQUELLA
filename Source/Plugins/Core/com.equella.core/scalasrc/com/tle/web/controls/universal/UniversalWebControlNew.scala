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

import com.dytech.edge.wizard.beans.control.CustomControl
import com.google.common.io.ByteStreams
import com.tle.beans.item.attachments.{FileAttachment, IAttachment, LinkAttachment}
import com.tle.common.i18n.CurrentLocale
import com.tle.common.wizard.controls.universal.UniversalSettings
import com.tle.common.wizard.controls.universal.handlers.FileUploadSettings
import com.tle.core.guice.Bind
import com.tle.core.i18n.CoreStrings
import com.tle.core.item.dao.AttachmentDao
import com.tle.core.json.CirceUtils
import com.tle.core.mimetypes.MimeTypeService
import com.tle.core.services.FileSystemService
import com.tle.core.workflow.thumbnail.service.ThumbnailService
import com.tle.core.workflow.video.VideoService
import com.tle.web.cloudproviders.CloudWizardControl
import com.tle.web.controls.universal.UniversalWebControlNew._
import com.tle.web.controls.universal.handlers.FileUploadHandlerNew
import com.tle.web.controls.universal.handlers.fileupload.WebFileUploads.{
  attachmentCreatorForUpload,
  validateContent
}
import com.tle.web.controls.universal.handlers.fileupload._
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.freemarker.annotations.ViewFactory
import com.tle.web.sections.ajax.AjaxGenerator
import com.tle.web.sections.ajax.handler.{AjaxFactory, AjaxMethod}
import com.tle.web.sections.annotations.{EventFactory, EventHandlerMethod, TreeLookup}
import com.tle.web.sections.equella.ajaxupload._
import com.tle.web.sections.equella.annotation.PlugKey
import com.tle.web.sections.equella.render.ZebraTableRenderer
import com.tle.web.sections.events.RenderEventContext
import com.tle.web.sections.events.js.{BookmarkAndModify, EventGenerator}
import com.tle.web.sections.jquery.libraries.JQueryProgression
import com.tle.web.sections.js.ElementId
import com.tle.web.sections.js.generic.{Js, OverrideHandler, ReloadHandler}
import com.tle.web.sections.js.generic.expression.ObjectExpression
import com.tle.web.sections.js.generic.function.{
  ExternallyDefinedFunction,
  IncludeFile,
  PartiallyApply
}
import com.tle.web.sections.render._
import com.tle.web.sections.result.util.{KeyLabel, PluralKeyLabel}
import com.tle.web.sections.standard.Button
import com.tle.web.sections.standard.annotations.Component
import com.tle.web.sections.standard.renderers.{DivRenderer, FileDropRenderer}
import com.tle.web.sections.{SectionInfo, SectionResult, SectionTree, SimpleBookmarkModifier}
import com.tle.web.viewurl.attachments.{
  AttachmentNode,
  AttachmentResourceService,
  AttachmentTreeService
}
import com.tle.web.wizard.{WizardService, WizardState}
import com.tle.web.wizard.controls.{AbstractWebControl, CCustomControl, WebControlModel}
import com.tle.web.wizard.impl.WebRepository
import com.tle.web.wizard.render.WizardFreemarkerFactory
import com.tle.web.wizard.section.WizardBodySection
import io.circe.parser._
import io.circe.syntax._
import javax.inject.Inject
import scala.jdk.CollectionConverters._
import scala.io.Source

class UniversalWebControlModel extends WebControlModel

object UniversalWebControlNew {
  val LABEL_EMPTY_LIST             = WebFileUploads.label("list.empty")
  val EDIT_LINK                    = WebFileUploads.label("list.edit")
  val REPLACE_LINK                 = WebFileUploads.label("list.replace")
  val DELETE_LINK                  = WebFileUploads.label("list.delete")
  val ADD_RESOURCE                 = WebFileUploads.label("list.add")
  val DELETE_CONFIRM               = WebFileUploads.label("list.delete.confirm")
  val PREVIEW                      = WebFileUploads.label("list.preview")
  val KEY_HIDDEN_FROM_SUMMARY_NOTE = WebFileUploads.r.key("list.hidden.from.summary")
  val uploadListSrc = new IncludeFile(
    WebFileUploads.r.url("reactjs/scripts/uploadlist.js"),
    JQueryProgression.PRERENDER,
    FileDropRenderer.CSS,
    ZebraTableRenderer.CSS
  )
  val uploadListFunc =
    new ExternallyDefinedFunction("FileUploader", uploadListSrc)
}

@Bind
class UniversalWebControlNew extends AbstractWebControl[UniversalWebControlModel] {
  def getModelClass = classOf[UniversalWebControlModel]

  @ViewFactory(name = "wizardFreemarkerFactory") var wizardViewFactory: WizardFreemarkerFactory = _
  @EventFactory var events: EventGenerator                                                      = _
  @AjaxFactory var ajax: AjaxGenerator                                                          = _
  @Inject var dialog: UniversalResourcesDialog                                                  = _

  @Inject var mimeTypeService: MimeTypeService                     = _
  @Inject var fileSystemService: FileSystemService                 = _
  @Inject var attachmentTreeService: AttachmentTreeService         = _
  @Inject var attachmentResourceService: AttachmentResourceService = _
  @Inject var thumbnailService: ThumbnailService                   = _
  @Inject var videoService: VideoService                           = _
  @Inject var wizardService: WizardService                         = _
  @Inject var attachmentDao: AttachmentDao                         = _

  @Component
  @PlugKey("duplicatewarning.linktext")
  var duplicateWarningMessage: Button = _

  var ctx: AfterRegister = _

  def renderHtml(context: RenderEventContext): SectionResult = ctx.renderControl(context)

  override def validate(): Unit = ctx.validate()

  protected def getIdForLabel: ElementId = this

  override def registered(id: String, tree: SectionTree): Unit = {
    super.registered(id, tree)
    ctx = new AfterRegister(id, tree, getWrappedControl.asInstanceOf[CCustomControl])
    duplicateWarningMessage.setClickHandler(events.getNamedHandler("openDuplicatePage"))
  }

  @EventHandlerMethod
  def openDuplicatePage(info: SectionInfo): Unit = {
    val wizardBodySection =
      info.lookupSection(classOf[WizardBodySection]).asInstanceOf[WizardBodySection]
    wizardBodySection.goToDuplicateDataTab(info)
  }

  @EventHandlerMethod def reloaded(info: SectionInfo): Unit = {
    WebFileUploads.removeFailedUploads(ctx, None)
    ctx.repository.getState.getWizardMetadataMapper.setMapNow(true)
  }

  class AfterRegister(id: String, tree: SectionTree, storageControl: CCustomControl)
      extends ControlContext
      with RenderHelper {
    val state      = new FileUploadState
    val definition = new UniversalSettings(control.getControlBean.asInstanceOf[CustomControl])
    val repository = control.getRepository.asInstanceOf[WebRepository]
    val fileSettingsO =
      if (definition.getAttachmentTypes.contains("fileHandler"))
        Some(new FileUploadSettings(definition))
      else None
//    val commandModifier = ajax.getModifier("uploadCommand")

    dialog.setRepository(repository)
    dialog.setDefinition(definition)
    dialog.setStorageControl(storageControl)
    dialog.setPreferredId(tree.getSubId(id, "dialog"))
    tree.registerInnerSection(dialog, id)
    dialog.setOkHandler(new ReloadHandler)
    dialog.setReloadFunction(getReloadFunction(true, events.getEventHandler("reloaded")))

    def controlSettings: FileUploadSettings = fileSettingsO.get

    def controlState = dialog

    def stateAction[A](info: SectionInfo)(f: => A): A = {
      var ret: Any = null
      dialog.updateWizardState(
        info,
        { _ =>
          ret = f
          controlState.getStorageControl.getWizardPage
            .saveToDocument(List(controlState.getStorageControl).asJava, null)
          true
        }
      )
      ret.asInstanceOf[A]
    }

    class UniversalRenderModel(info: SectionInfo) extends WebControlModel {
//      sys.error("metadatamapping")

      def getId = id

      // These two methods are called in universalattachmentlist.ftl
      def isDisplayDuplicateWarning: Boolean = isDuplicateWarning
      def getDuplicateWarningLink            = duplicateWarningMessage
      def getDuplicateWarningMessage         = CoreStrings.text("duplicatewarning.message")

      def getDivTag = {
        def entries(
            attachments: Iterable[AttachmentNode],
            editable: Boolean
        ): Iterable[AjaxFileEntry] = {
          attachments.map { an =>
            val attachment = an.getAttachment
            val children   = entries(an.getChildren.asScala, false)
            entryForAttachment(info, attachment, editable, children)
          }
        }

        val ts = new TagState()
        ts.setId(id + "_r")
        val topLevelAttachments =
          attachmentTreeService.getTreeStructure(dialog.getAttachments, false).asScala
        val uploadArgs = new ObjectExpression(
          "elem",
          ts,
          "ctrlId",
          id,
          "editable",
          java.lang.Boolean.valueOf(repo.isEditable),
          "canUpload",
          java.lang.Boolean.valueOf(fileSettingsO.isDefined),
          "maxAttachments",
          (if (definition.isMaxFilesEnabled) Some(definition.getMaxFiles)
           else Some(1).filterNot(_ => definition.isMultipleSelection)).map(Integer.valueOf).orNull,
          "entries",
          CirceUtils.circeToExpression(entries(topLevelAttachments, true)),
          "strings",
          new ObjectExpression(
            "add",
            ADD_RESOURCE.getText,
            "edit",
            EDIT_LINK.getText,
            "replace",
            REPLACE_LINK.getText,
            "delete",
            DELETE_LINK.getText,
            "deleteConfirm",
            DELETE_CONFIRM.getText,
            "cancel",
            FileUploadHandlerNew.LABEL_CANCEL_UPLOAD.getText,
            "drop",
            CurrentLocale.get(FileDropRenderer.KEY_DND),
            "none",
            LABEL_EMPTY_LIST.getText,
            "preview",
            PREVIEW.getText,
            "toomany",
            CurrentLocale.getFormatForKey("wizard.controls.file.toomanyattachments"),
            "toomany_1",
            CurrentLocale.getFormatForKey("wizard.controls.file.toomanyattachments.1")
          ),
          "reloadState",
          CloudWizardControl.reloadState,
          "dialog",
          PartiallyApply.partial(dialog.getOpenFunction, 2),
          "commandUrl",
          ajax.getAjaxUrl(info, "uploadCommand").getHref
        )
        ts.addReadyStatements(uploadListFunc, uploadArgs)
        new DivRenderer(ts)
      }
    }

    def validate(): Unit = {
      val attachments         = dialog.getAttachments.asScala
      val state               = repository.getState
      val uploadedAttachments = dialog.getAttachments.size
      val fileDuplicateCheckEnabled =
        dialog.getControlConfiguration.getBooleanAttribute("FILE_DUPLICATION_CHECK")
      val linkDuplicateCheckEnabled =
        dialog.getControlConfiguration.getBooleanAttribute("LINK_DUPLICATION_CHECK")

      def fileDuplicateCheck(fileAttachment: FileAttachment): Unit = {
        wizardService.checkFileAttachmentDuplicate(
          state,
          fileAttachment.getFilename,
          fileAttachment.getUuid
        )
      }

      def linkDuplicateCheck(linkAttachment: LinkAttachment): Unit = {
        wizardService.checkLinkAttachmentDuplicate(
          state,
          linkAttachment.getUrl,
          linkAttachment.getUuid
        )
      }

      def updateDuplicateWarningMessage(): Unit = {
        if (duplicatesFound) {
          setDuplicateWarning(true)
        } else {
          setDuplicateWarning(false)
        }
      }

      val maxFiles = if (definition.isMaxFilesEnabled) definition.getMaxFiles else 1

      if (
        (!definition.isMultipleSelection && uploadedAttachments > 1) ||
        (definition.isMaxFilesEnabled && uploadedAttachments > maxFiles)
      ) {
        setInvalid(
          true,
          new PluralKeyLabel(
            "wizard.controls.file.toomanyattachments",
            maxFiles,
            (uploadedAttachments - maxFiles).asInstanceOf[Object]
          )
        )
      }

      if (attachments.nonEmpty) {
        attachments.collect {
          case fileAttachment: FileAttachment if fileDuplicateCheckEnabled =>
            fileDuplicateCheck(fileAttachment)
          case linkAttachment: LinkAttachment if linkDuplicateCheckEnabled =>
            linkDuplicateCheck(linkAttachment)
        }
      }
      updateDuplicateWarningMessage
    }

    def renderControl(context: RenderEventContext): SectionResult = {
      val m = new UniversalRenderModel(context)
      wizardViewFactory.createWizardResult(
        renderModel("universalattachmentlist.ftl", m),
        UniversalWebControlNew.this
      )
    }

    def mimeTypeForFilename(name: String): String = mimeTypeService.getMimeTypeForFilename(name)

    val stagingContext = new FileStagingContext(
      Option(repo.getStagingid),
      repo.getItem.getItemId,
      fileSystemService,
      thumbnailService,
      videoService,
      mimeTypeService,
      repository
    )

    def viewFactory: FreemarkerFactory = wizardViewFactory

    def entryForAttachment(
        info: SectionInfo,
        a: IAttachment,
        editable: Boolean,
        children: Iterable[AjaxFileEntry]
    ): AjaxFileEntry = {
      val viewableResource =
        attachmentResourceService.getViewableResource(info, repository.getViewableItem, a)
      val desc = if (Option(dialog.findHandlerForAttachment(a)).exists(_.isHiddenFromSummary(a))) {
        new KeyLabel(KEY_HIDDEN_FROM_SUMMARY_NOTE, a.getDescription).getText
      } else a.getDescription
      AjaxFileEntry(
        a.getUuid,
        desc,
        viewableResource.createDefaultViewerUrl().getHref,
        editable,
        a.isPreview,
        children
      )
    }

    // Returns true if any attachment in this control has duplicate information found
    def duplicatesFound: Boolean = {
      val duplicateData = repo.getState.getDuplicateData
      val attachments   = dialog.getAttachments.asScala
      attachments.exists(attachment => duplicateData.containsKey(attachment.getUuid))
    }

    def processUploadCommand(info: SectionInfo): SectionResult = {
      val request = info.getRequest
      def uploadStream(uploadId: UUID): AjaxUploadResponse = {
        state.uploadForId(uploadId) match {
          case Some(uf: UploadingFile) =>
            state.remove(uploadId)
            def illegal(reason: IllegalFileReason) =
              UploadFailed(
                WebFileUploads.labelForIllegalReason(reason, uf.originalFilename).getText
              )
            val mimeType = mimeTypeForFilename(uf.originalFilename)
            // Retrieve file from form data. If not found then try to get it from request body.
            val r = WebFileUploads
              .validateBeforeUpload(mimeType, request.getContentLengthLong, controlSettings)
              .map(illegal)
              .getOrElse {
                WebFileUploads.writeStream(uf, this, request.getInputStream) match {
                  case Successful(fileInfo) =>
                    validateContent(info, this, uf.uploadPath) match {
                      case Left(ifr) => illegal(ifr)
                      case Right(detected) =>
                        stateAction(info) {
                          val v      = ValidatedUpload(uf.success(fileInfo), detected)
                          val create = attachmentCreatorForUpload(info, this, v)
                          val a      = create.createStaged(stagingContext)
                          val uuid   = UUID.randomUUID().toString
                          a.setUuid(uuid)
                          controlState.addAttachment(info, a)
                          controlState.addMetadataUuid(info, uuid)
                          create.commit(a, stagingContext)
                          repo.unregisterFilename(uploadId)
                          // Validate attachments after uploading through 'Drag and Drop'
                          validate
                          UpdateEntry(
                            entryForAttachment(info, a, true, Iterable.empty),
                            Option(new AttachmentDuplicateInfo(duplicatesFound, getElementId(info)))
                          )
                        }
                    }
                  case IllegalFile(reason) => illegal(reason)
                  case Errored(t) => UploadFailed(Option(t.getMessage).getOrElse("Unknown error"))
                }
              }
            r
          case _ => UploadFailed("NO SUCH UPLOAD")
        }
      }

      val response =
        Option(request.getParameter("uploadId")).map(UUID.fromString).map(uploadStream).getOrElse {
          val maxStream  = ByteStreams.limit(request.getInputStream, 32 * 1024)
          val jsonSource = Source.fromInputStream(maxStream, "UTF-8").mkString

          decode[AjaxUploadCommand](jsonSource).fold(throw _, identity) match {
            case Delete(attachmentUuid) =>
              stateAction(info) {
                dialog.deleteAttachment(info, attachmentUuid)
                // Validate attachments after removing through 'Drag and Drop'
                validate
                RemoveEntries(
                  Iterable(attachmentUuid),
                  Option(new AttachmentDuplicateInfo(duplicatesFound, getElementId(info)))
                )
              }
            case NewUpload(filename, size) =>
              stateAction(info) {
                val uploadId   = UUID.randomUUID()
                val uniqueName = WebFileUploads.uniqueName(filename, None, uploadId, this)
                WebFileUploads
                  .validateBeforeUpload(mimeTypeForFilename(filename), size, controlSettings)
                  .map { reason =>
                    UploadFailed(WebFileUploads.labelForIllegalReason(reason, filename).getText)
                  }
                  .getOrElse {
                    state.initialiseUpload(uploadId, uniqueName, uniqueName)
                    val uploadUrl = ajax
                      .getModifiedAjaxUrl(
                        info,
                        new SimpleBookmarkModifier("uploadId", uploadId.toString),
                        "uploadCommand"
                      )
                      .getHref
                    NewUploadResponse(uploadUrl, uploadId.toString, uniqueName)
                  }
              }
          }
        }

      new SimpleSectionResult(response.asJson.noSpaces, "application/json")
    }
  }

  @AjaxMethod def uploadCommand(info: SectionInfo): SectionResult = ctx.processUploadCommand(info)

}
