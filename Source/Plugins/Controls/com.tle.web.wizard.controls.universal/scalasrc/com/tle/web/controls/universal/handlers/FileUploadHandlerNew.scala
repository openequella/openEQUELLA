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

package com.tle.web.controls.universal.handlers

import java.util
import java.util.{Collections, UUID}
import javax.inject.Inject

import com.tle.beans.item.ItemId
import com.tle.beans.item.attachments._
import com.tle.common.collection.AttachmentConfigConstants
import com.tle.common.filesystem.FileEntry
import com.tle.common.filesystem.handle.StagingFile
import com.tle.common.i18n.CurrentLocale
import com.tle.common.wizard.controls.universal.handlers.FileUploadSettings
import com.tle.core.filesystem.staging.service.StagingService
import com.tle.core.guice.Bind
import com.tle.core.item.service.{ItemFileService, ItemService}
import com.tle.core.mimetypes.MimeTypeService
import com.tle.core.security.TLEAclManager
import com.tle.core.services.{FileSystemService, ZipProgress}
import com.tle.core.workflow.thumbnail.service.ThumbnailService
import com.tle.core.workflow.video.VideoService
import com.tle.mycontent.service.MyContentService
import com.tle.mycontent.web.selection.{MyContentSelectable, MyContentSelectionSettings}
import com.tle.web.controls.universal._
import com.tle.web.controls.universal.handlers.fileupload._
import com.tle.web.controls.universal.handlers.fileupload.details._
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.inplaceeditor.service.InPlaceEditorWebService
import com.tle.web.myresource.MyResourceConstants
import com.tle.web.resources.ResourcesService
import com.tle.web.sections.ajax.AjaxGenerator
import com.tle.web.sections.ajax.handler.{AjaxFactory, AjaxMethod}
import com.tle.web.sections.annotations.{Bookmarked, EventHandlerMethod}
import com.tle.web.sections.equella.ajaxupload.AjaxUpload
import com.tle.web.sections.equella.annotation.PlugKey
import com.tle.web.sections.equella.render.UnselectLinkRenderer
import com.tle.web.sections.events.RenderContext
import com.tle.web.sections.events.js.BookmarkAndModify
import com.tle.web.sections.generic.InfoBookmark
import com.tle.web.sections.jquery.JQuerySelector
import com.tle.web.sections.js.generic.StatementHandler
import com.tle.web.sections.js.generic.function.{PartiallyApply, PassThroughFunction}
import com.tle.web.sections.render.{CombinedRenderer, Label, SectionRenderable, TextLabel}
import com.tle.web.sections.result.util.KeyLabel
import com.tle.web.sections.standard._
import com.tle.web.sections.standard.annotations.Component
import com.tle.web.sections.standard.model.{HtmlComponentState, HtmlLinkState, TableState}
import com.tle.web.sections.standard.renderers.{DivRenderer, LinkRenderer}
import com.tle.web.sections.{Bookmark, SectionInfo, SectionTree}
import com.tle.web.selection.filter.SelectionFilter
import com.tle.web.selection.{ParentFrameSelectionCallback, SelectedResourceDetails, SelectionService, SelectionSession}
import com.tle.web.viewurl.attachments.AttachmentResourceService
import com.tle.web.viewurl.{AttachmentDetail, ViewItemService, ViewableResource}

import scala.collection.JavaConverters._

class FileUploadHandlerModel extends AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel {
  @Bookmarked(name = "s") var selecting = false
  @Bookmarked(name = "r") var resolved = false
  var viewableResource: ViewableResource = _
}

object FileUploadHandlerNew {
  val r = ResourcesService.getResourceHelper(getClass)

  def l(s: String) = new KeyLabel(r.key(s))

  val LABEL_NAME = l("handlers.file.name")
  val LABEL_DESCRIPTION = l("handlers.file.description")
  val ADD_TITLE_LABEL = l("handlers.file.title")
  val EDIT_TITLE_LABEL = ADD_TITLE_LABEL
  val LABEL_REMOVE_UPLOAD = l("handlers.file.link.removeupload")
  val LABEL_CANCEL_UPLOAD = l("handlers.file.link.cancelupload")
  val VIEW_LINK_LABEL = l("handlers.file.viewlink")
}

import com.tle.web.controls.universal.handlers.FileUploadHandlerNew._

@Bind
class FileUploadHandlerNew extends AbstractAttachmentHandler[FileUploadHandlerModel] {
  var state: UniversalControlState = _
  var ctx: AfterRegister = _

  @Inject var fileSystemService: FileSystemService = _
  @Inject var stagingService: StagingService = _
  @Inject var itemFileService: ItemFileService = _
  @Inject var mimeTypeService: MimeTypeService = _
  @Inject var myContentService: MyContentService = _
  @Inject var selectionService: SelectionService = _
  @Inject var myContentSelectable: MyContentSelectable = _
  @Inject var aclManager: TLEAclManager = _
  @Inject var itemService: ItemService = _
  @Inject var viewItemService: ViewItemService = _
  @Inject var attachmentResourceService: AttachmentResourceService = _
  @Inject var thumbnailService: ThumbnailService = _
  @Inject var videoService: VideoService = _
  @Inject var inplaceEditorService: InPlaceEditorWebService = _

  @Component var fileUpload: FileUpload = _
  @Component
  @PlugKey("handlers.file.link.filesfromscrapbook") var filesFromScrapbookLink: Link = _
  @Component var fileDrop: FileDrop = _
  @AjaxFactory var ajax: AjaxGenerator = _
  @Component
  @PlugKey("handlers.file.action.next") var optionsButton: Button = _
  @Component(name = "d") var detailTable: Table = _

  override def getDefaultPropertyName: String = "fuh"

  override def instantiateModel(info: SectionInfo) = new FileUploadHandlerModel

  override def onRegister(tree: SectionTree, parentId: String, state: UniversalControlState): Unit = {
    this.state = state
    super.onRegister(tree, parentId, state)
  }

  override def registered(id: String, tree: SectionTree): Unit = {
    super.registered(id, tree)
    ctx = new AfterRegister(id, tree, state)
    filesFromScrapbookLink.setClickHandler(events.getNamedHandler("startSelection"))
  }

  def getHandlerId: String = "fileHandler"

  def getLabel: AttachmentHandlerLabel = new AttachmentHandlerLabel(LABEL_NAME, LABEL_DESCRIPTION)

  def supports(attachment: IAttachment): Boolean = WebFileUploads.isFileOrZipAttachment(attachment) ||
    WebFileUploads.isPackageAttachment(attachment)


  def createNew(info: SectionInfo): Unit = {
    ctx.cancelled(info)
    val m = getModel(info)
    m.resolved = false
    m.selecting = false
    m.setEditDetails(false)
  }

  def loadForEdit(info: SectionInfo, attachment: Attachment): Unit = {
    val m = getModel(info)
    m.setEditDetails(true)
    m.resolved = true
    ctx.loadForEdit(info, attachment)
  }

  def render(context: RenderContext, renderOptions: DialogRenderOptions): SectionRenderable = {
    ctx.render(context, renderOptions)
  }

  def validate(info: SectionInfo): Boolean = ctx.validate(info)

  def saveEdited(info: SectionInfo, attachment: Attachment): Unit = ctx.editAttachment(info, attachment)

  def saveChanges(info: SectionInfo, replacementUuid: String): Unit =
    ctx.createNew(info, Option(replacementUuid).map(UUID.fromString))

  def cancelled(info: SectionInfo): Unit = ctx.cancelled(info)

  def remove(info: SectionInfo, attachment: Attachment, willBeReplaced: Boolean): Unit = {
    val da = WebFileUploads.deleteAttachment(ctx, attachment)
    da.attachments.foreach { a =>
      if (!willBeReplaced || a.getUuid != attachment.getUuid)
        ctx.controlState.removeMetadataUuid(info, a.getUuid)
    }
    ctx.controlState.removeAttachments(info, da.attachments.asJavaCollection)
    da.deleteFiles(ctx.stagingContext)
  }

  def getTitleLabel(context: RenderContext, editing: Boolean): Label = if (editing) EDIT_TITLE_LABEL else ADD_TITLE_LABEL;

  override def isHiddenFromSummary(attachment: IAttachment): Boolean =
    super.isHiddenFromSummary(attachment) || WebFileUploads.zipAttachment(attachment).exists(!_.isAttachZip)

  @AjaxMethod def processUploadDND(info: SectionInfo): SectionRenderable = WebFileUploads.ajaxUpload(info, ctx, fileDrop)

  @AjaxMethod def processUploadOld(info: SectionInfo): SectionRenderable = WebFileUploads.ajaxUpload(info, ctx, fileUpload)

  @EventHandlerMethod def finishedUpload(info: SectionInfo, uploadId: UUID): Unit = {
    WebFileUploads.removeFailedUploads(ctx, Some(uploadId))
    WebFileUploads.validateAllFinished(info, ctx)
  }

  @EventHandlerMethod def illegalFile(info: SectionInfo, filename: String, reason: String): Unit =
    ctx.state.newIllegalFile(filename, IllegalFileReason.fromString(reason))

  @EventHandlerMethod def removeUpload(info: SectionInfo, uuid: UUID): Unit = {
    ctx.stopOrCancel(info, uuid)
  }

  @EventHandlerMethod def nextPage(info: SectionInfo): Unit = {
    getModel(info).setEditDetails(true)
    ctx.startEdit(info)
  }

  @EventHandlerMethod def startSelection(info: SectionInfo) = getModel(info).selecting = true

  @EventHandlerMethod def selectionsMade(info: SectionInfo, selectedResources: java.util.List[SelectedResourceDetails]) = {
    getModel(info).selecting = false
    ctx.selectionsMade(info, selectedResources.asScala)
  }


  class AfterRegister(id: String, tree: SectionTree, val controlState: UniversalControlState)
    extends ControlContext with RenderHelper with ZipHandler with ViewerHandler with EditingHandler {

    val canScrapbook = myContentService.isMyContentContributionAllowed
    val state: FileUploadState = new FileUploadState
    val stagingContext = new FileStagingContext(Option(repo.getStagingid), repo.getItem.getItemId, fileSystemService, thumbnailService,
      videoService, mimeTypeService, controlState.getRepository)
    val controlSettings = new FileUploadSettings(controlState.getControlConfiguration)
    val cancelFunc = ajax.getAjaxUpdateDomFunction(tree, FileUploadHandlerNew.this, events.getEventHandler("removeUpload"),
      ajax.getEffectFunction(AjaxGenerator.EffectType.REPLACE_IN_PLACE), "uploads")

    val validateFile = {
      val errorCallback = PartiallyApply.partial(events.getSubmitValuesFunction("illegalFile"), 2)
      val doneCallback = PartiallyApply.partial(events.getSubmitValuesFunction("finishedUpload"), 1)
      val startedUpload = AjaxUpload.createProgressFunc(new JQuerySelector(JQuerySelector.Type.RAW, "#uploads .uploadsprogress"), cancelFunc)
      WebFileUploads.validateFunc(controlSettings, errorCallback, startedUpload, doneCallback)
    }
    val _resultsCallback = new PassThroughFunction("r" + id, events.getSubmitValuesFunction("selectionsMade"))

    val showRestrict = hasInstitutionPrivilege(AttachmentConfigConstants.RESTRICT_ATTACHMENTS)
    private def getEditingAttachment(info: SectionInfo) = getEditState.a
    val fileEditDetails = new FileEditDetails(id, tree, this, this, this, this, showRestrict, getEditingAttachment, inplaceEditorService)
    val packageEditDetails = new PackageEditDetails(id, tree, this, this, showRestrict, getEditingAttachment)
    val fileOptions = new FileOptions(id, tree, controlSettings, this, _ => singleUpload, resolvedType)

    def detailsEditorForAttachment(a: Attachment): DetailsPage =
      if (WebFileUploads.isPackageAttachment(a)) packageEditDetails else fileEditDetails

    case class DetailsEditState(a: Attachment, page: DetailsPage,
                                cleanupFiles: (Attachment, StagingContext) => Unit,
                                zipAttachment: Boolean, editingArea: Option[StagingFile] = None,
                                commitUnzipPaths: Option[(String, String)] = None, zipProgress: Option[ZipProgress] = None) {


      def removeEditingArea() : Unit = editingArea.foreach(sf => stagingService.removeStagingArea(sf, true))
      def commit(): Unit = (a, zipAttachment) match {
        case (fa: FileAttachment, true) =>
          stagingContext.moveFile(fa.getFilename, WebFileUploads.zipPath(fa.getFilename))
          commitUnzipPaths.foreach(p => stagingContext.moveFile(p._1, fa.getFilename))
        case (za: ZipAttachment, false) =>
          stagingContext.delete(za.getUrl)
          stagingContext.moveFile(WebFileUploads.zipPath(za.getUrl), za.getUrl)
        case _ => commitUnzipPaths.foreach(p => stagingContext.delete(p._1))
      }


      def afterCommit(): Unit = removeEditingArea()

      def cancel() : Unit = {
        cleanupFiles(a, stagingContext)
        commitUnzipPaths.foreach(f => stagingContext.delete(f._1))
        removeEditingArea()
      }
    }

    var editStateO: Option[DetailsEditState] = None

    def getEditState = editStateO.getOrElse(sys.error("Trying to use edit state when none exists"))

    def setEditState(d: DetailsEditState) = editStateO = Some(d)

    def allValidatedUploads = CurrentUpload.validatedUploads(state.allCurrentUploads)


    class UploadDisplay(id: UUID, filename: String, finished: Boolean) {
      def getFilename = filename

      val getProgressDiv = {
        val progressDivState = new HtmlComponentState
        val progressDiv = new DivRenderer(progressDivState)
        progressDivState.setId("u" + id)
        if (finished) {
          val inner = new DivRenderer("")
          inner.addClass("progress-bar-inner complete")
          progressDiv.setNestedRenderable(inner)
        }
        progressDiv
      }

      val getRemove = {
        val remove = new HtmlLinkState
        remove.setClickHandler(new StatementHandler(cancelFunc, id))
        new UnselectLinkRenderer(remove, if (finished) LABEL_REMOVE_UPLOAD else LABEL_CANCEL_UPLOAD)
      }
    }

    case class FileAddModel(_uploads: Iterable[CurrentUpload]) {
      def isCanScrapbook = canScrapbook

      val getUploads = _uploads.collect {
        case UploadingFile(id, _, fn, _, _, _) => new UploadDisplay(id, fn, false)
        case SuccessfulUpload(id, _, fn, _, _, _) => new UploadDisplay(id, fn, true)
        case v: ValidatedUpload => new UploadDisplay(v.id, v.s.originalFilename, true)
      }.asJava

      def getProblemLabel = WebFileUploads.errorMessage(_uploads).orNull

      def getFileUpload = fileUpload

      def getFileDrop = fileDrop

      def getFilesFromScrapbookLink = filesFromScrapbookLink
    }

    class DetailsModel(info: SectionInfo) {
      val resource = viewableResource(info)
      val tableState = detailTable.getState(info)
      for (detail <- Option(resource.getCommonAttachmentDetails).getOrElse(Collections.emptyList[AttachmentDetail]).asScala) {
        val labelCell = new TableState.TableCell(detail.getName)
        labelCell.addClass("label")
        tableState.addRow(labelCell, detail.getDescription)
      }

      def getDetailTable = detailTable

      val getThumbnail = resource.createStandardThumbnailRenderer(new TextLabel(resource.getDescription))

      val getViewlink = {
        if (getEditState.page.previewable) {
          val resourceBookmark: Bookmark = resource.createCanonicalUrl
          val linkState = new HtmlLinkState(VIEW_LINK_LABEL, resourceBookmark)
          linkState.setTarget(HtmlLinkState.TARGET_BLANK)
          new LinkRenderer(linkState)
        } else null
      }

    }

    def render(context: RenderContext, renderOptions: DialogRenderOptions) = {
      val m = getModel(context)
      val (r, f) = if (m.selecting) renderSelection(context)
      else if (m.isEditDetails) {
        if (m.resolved) {
          val (_r, _f) = renderEditing(context)
          (CombinedRenderer.combineResults(renderModel("file/file-editheader.ftl", new DetailsModel(context)), _r), _f)
        }
        else fileOptions.render(context, singleUpload)
      } else renderFileUpload(context)
      f(renderOptions)
      r
    }

    def renderFileUpload(context: RenderContext): (SectionRenderable, DialogRenderOptions => Unit) = {
      fileUpload.setAjaxUploadUrl(context, new BookmarkAndModify(context, ajax.getModifier("processUploadOld")))
      fileDrop.setAjaxUploadUrl(context, new BookmarkAndModify(context, ajax.getModifier("processUploadDND")))
      fileUpload.setValidateFile(context, validateFile)
      fileDrop.setValidateFile(context, validateFile)

      val allUploads = state.allCurrentUploads
      val m = FileAddModel(allUploads)
      (renderModel("file/file-add.ftl", m), prepareOptions(context, allUploads))
    }

    def singleUpload: ValidatedUpload = {
      val allValidated = allValidatedUploads
      allValidated.headOption.filter(_ => allValidated.size == 1).getOrElse(sys.error("Illegal state - need single upload"))
    }

    def resolvedType(info: SectionInfo, vu: ValidatedUpload): Unit = {
      getModel(info).resolved = true
      val creator = WebFileUploads.attachmentCreatorForUpload(info, this, vu)
      val a = creator.create(stagingContext)
      val ed = detailsEditorForAttachment(a)
      state.remove(vu.id)
      setEditState(DetailsEditState(a, ed, creator.cancel, false))
      ed.prepareUI(info)
    }

    def startEdit(info: SectionInfo): Unit = {
      val su = singleUpload
      fileOptions.defaultResolve(singleUpload) match {
        case Some(vu) => resolvedType(info, vu)
        case None => fileOptions.prepareUI(info, su)
      }
    }

    def loadForEdit(info: SectionInfo, attachment: Attachment): Unit = {
      val page = detailsEditorForAttachment(attachment)
      setEditState(DetailsEditState(attachment, page, (_, _) => (), WebFileUploads.zipAttachment(attachment).isDefined))
      page.prepareUI(info)
    }

    def renderEditing(context: RenderContext): (SectionRenderable, DialogRenderOptions => Unit) = {
      val s = getEditState
      val (r, o) = s.page.renderDetails(context)
      (r, { (rdo:DialogRenderOptions) => rdo.setShowSave(true); o(rdo) })
    }

    def editAttachment(info: SectionInfo, attachment: Attachment): Unit = {
      val editState = getEditState
      editState.commit()
      val (newAttach, delattach) = detailsEditorForAttachment(attachment).editAttachment(info, attachment, this)
      if (newAttach ne attachment) {
        val a = controlState.getRepository.getAttachments
        a.removeAttachment(attachment)
        a.addAttachment(newAttach)
      }
      delattach.foreach { ad =>
        controlState.removeAttachments(info, ad.attachments.asJavaCollection)
        ad.attachments.foreach(a => controlState.removeMetadataUuid(info, a.getUuid))
        ad.deleteFiles(stagingContext)
      }
      editState.afterCommit()
    }

    def createNew(info: SectionInfo, uuid: Option[UUID]): Unit = {
      if (getModel(info).isEditDetails) {
        val editState = getEditState
        val u = uuid.getOrElse(UUID.randomUUID()).toString
        val _a = editState.a
        _a.setUuid(u)
        editState.commit()
        val (a,_) = editState.page.editAttachment(info, _a, this)
        controlState.addAttachment(info, a)
        if (uuid.isEmpty) {
          controlState.addMetadataUuid(info, u)
        }
        editState.afterCommit()
      }
      else {
        allValidatedUploads.zipWithIndex.foreach { case (vu,i) =>
          val c = WebFileUploads.attachmentCreatorForUpload(info, this, vu)
          val a = c.create(stagingContext)
          val repUuid = uuid.filter(_ => i == 0)
          val u = repUuid.getOrElse(UUID.randomUUID()).toString
          a.setUuid(u)
          controlState.addAttachment(info, a)
          if (repUuid.isEmpty) {
            controlState.addMetadataUuid(info, u)
          }
        }
      }
    }

    def validate(info: SectionInfo): Boolean = if (getModel(info).isEditDetails) {
      getEditState.page.validate(info)
    } else true

    def prepareOptions(context: RenderContext, uploads: Iterable[CurrentUpload])(renderOptions: DialogRenderOptions): Unit = {
      // Can move on if there are no uploading files and at least one successful upload
      if (!uploads.exists(!_.finished)) {
        val allValidated = allValidatedUploads
        if (allValidated.size == 1) {
          optionsButton.setClickHandler(context, events.getNamedHandler("nextPage"))
          renderOptions.addAction(optionsButton)
        }
        else if (allValidated.size > 1) {
          renderOptions.setShowSave(true)
          renderOptions.setShowAddReplace(true)
        }
      }
    }

    def cancelled(info: SectionInfo): Unit = {
      state.allCurrentUploads.foreach { cu =>
        WebFileUploads.cleanupForUpload(this, cu).apply(stagingContext)
        state.remove(cu.id)
      }
      editStateO.foreach(_.cancel)
      editStateO = None
    }

    def mimeTypeForFilename(name: String): String = mimeTypeService.getMimeTypeForFilename(name)

    def stopOrCancel(info: SectionInfo, id: UUID): Unit = {
      state.uploadForId(id).foreach { cu =>
        WebFileUploads.cleanupForUpload(ctx, cu).apply(stagingContext)
        state.remove(cu.id)
      }
    }

    def viewFactory: FreemarkerFactory = FileUploadHandlerNew.this.viewFactory

    case class SelectionModel(_selectionUrl: String) {
      def getSelectionUrl = _selectionUrl

      def getResultsCallback = _resultsCallback
    }

    def renderSelection(context: RenderContext): (SectionRenderable, DialogRenderOptions => Unit) = {
      val forward: SectionInfo = selectionService.getSelectionSessionForward(context, initSession, myContentSelectable)
      (renderModel("file/file-selection.ftl", SelectionModel(new InfoBookmark(forward).getHref)), _.setFullscreen(true))
    }

    private def initSession: SelectionSession = {
      val session: SelectionSession = new SelectionSession(new ParentFrameSelectionCallback(_resultsCallback, false))
      val mimeFilter: SelectionFilter = new SelectionFilter
      val settings: MyContentSelectionSettings = new MyContentSelectionSettings
      settings.setRestrictToHandlerTypes(util.Arrays.asList(MyResourceConstants.MYRESOURCE_CONTENT_TYPE))
      session.setAttribute(classOf[MyContentSelectionSettings], settings)
      session.setSelectScrapbook(true)
      session.setSelectItem(true)
      session.setSelectAttachments(false)
      session.setSelectPackage(false)
      session.setSelectMultiple(isMultiple)
      session.setAddToRecentSelections(false)
      if (!isMultiple) session.setSkipCheckoutPage(true)
      if (controlSettings.isRestrictByMime) {
        mimeFilter.setAllowedMimeTypes(controlSettings.getMimeTypes)
        session.setAttribute(classOf[SelectionFilter], mimeFilter)
      }
      session
    }

    def selectionsMade(info: SectionInfo, selectedResources: Iterable[SelectedResourceDetails]) = {
      selectedResources.foreach { resource =>
        val itemId = new ItemId(resource.getUuid, resource.getVersion)
        val item = itemService.get(itemId)
        val files: java.util.List[FileAttachment] = new UnmodifiableAttachments(item).getList(AttachmentType.FILE)
        files.asScala.foreach { fa =>
          val u = UUID.randomUUID()
          val fn = WebFileUploads.uniqueName(fa.getFilename, u, this)
          WebFileUploads.uploadStream(u, fn, resource.getTitle, fa.getSize, mimeTypeForFilename(fn), () =>
            fileSystemService.read(itemFileService.getItemFile(item), fa.getFilename), this)
        }
      }
      WebFileUploads.validateAllFinished(info, ctx)
    }

    def hasInstitutionPrivilege(priv: String): Boolean =
      !aclManager.filterNonGrantedPrivileges(List(priv).asJava).isEmpty

    override def unzippedEntries: Seq[FileEntry] = {
      val eds = getEditState
      val zipFolder = eds.a match {
        case fa: FileAttachment => eds.commitUnzipPaths.get._1
        case za: ZipAttachment => WebFileUploads.removeZipPath(za.getUrl)
      }
      fileSystemService.enumerateTree(stagingContext.stgFile, zipFolder, null).getFiles.asScala
    }

    object EmptyZipProgress extends ZipProgress {
      def getTotalFiles: Int = 0

      def getCurrentFile: Int = 0

      def isFinished: Boolean = true
    }

    def unzip: ZipProgress = {
      val eds = getEditState
      if (!eds.zipAttachment) {
        val (commitUnzipPaths, zp) = eds.a match {
          case fa: FileAttachment =>
            if (eds.commitUnzipPaths.isEmpty) {
              val srcZip = fa.getFilename
              val target = "_uploads/" + UUID.randomUUID().toString
              (Some((target, fa.getFilename)), stagingContext.unzip(srcZip, target))
            } else (eds.commitUnzipPaths, eds.zipProgress.getOrElse(EmptyZipProgress))
          case _ => (None, EmptyZipProgress)
        }
        setEditState(eds.copy(zipAttachment = true, commitUnzipPaths = commitUnzipPaths, zipProgress = Some(zp)))
        zp
      } else EmptyZipProgress
    }

    def unzipped: Boolean = getEditState.zipAttachment


    def selectedAttachments: Map[String, Attachment] = getEditState.a match {
      case za: ZipAttachment =>
        val fnPrefixLen = WebFileUploads.removeZipPath(za.getUrl).length + 1
        WebFileUploads.findAttachments(repo, WebFileUploads.isSelectedInZip(za)).map {
          a => (a.getUrl.substring(fnPrefixLen), a)
        }.toMap
      case _ => Map.empty
    }

    def removeUnzipped: Unit = setEditState(getEditState.copy(zipAttachment = false))

    def viewableResource(info: SectionInfo): ViewableResource = {
      val m = getModel(info)
      if (m.viewableResource == null) {
        m.viewableResource = attachmentResourceService.getViewableResource(info, controlState.getViewableItem(info), getEditState.a)
      }
      m.viewableResource
    }

    lazy val viewerListModel: ViewersListModel = new ViewersListModel(viewItemService, viewableResource)

    def zipProgress: Option[ZipProgress] = getEditState.zipProgress.filter(!_.isFinished)

    def editingArea: String = {
      val eds = getEditState
      eds.editingArea.getOrElse {
        val sf = stagingService.createStagingArea()
        setEditState(eds.copy(editingArea = Some(sf)))
        sf
      }.getUuid
    }

    def syncEdits(filename: String): Unit = {
      getEditState.editingArea.foreach {
        sf => if (fileSystemService.fileExists(sf, filename)) {
          fileSystemService.move(sf, filename, stagingContext.stgFile, filename)
        }
      }
    }

  }

}
