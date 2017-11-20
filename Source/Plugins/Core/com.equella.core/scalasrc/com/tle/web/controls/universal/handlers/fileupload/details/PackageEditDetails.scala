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

package com.tle.web.controls.universal.handlers.fileupload.details

import com.tle.beans.item.attachments.{Attachment, ImsAttachment}
import com.tle.common.NameValue
import com.tle.common.collection.AttachmentConfigConstants
import com.tle.common.filesystem.FileSystemConstants
import com.tle.core.plugins.PluginTracker
import com.tle.web.controls.universal.handlers.fileupload.{AttachmentDelete, WebFileUploads}
import com.tle.web.controls.universal.{AbstractDetailsAttachmentHandler, ControlContext, DialogRenderOptions, RenderHelper}
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.freemarker.annotations.ViewFactory
import com.tle.web.sections.equella.AbstractScalaSection
import com.tle.web.sections.events.RenderContext
import com.tle.web.sections.generic.AbstractPrototypeSection
import com.tle.web.sections.render.{SectionRenderable, TextLabel}
import com.tle.web.sections.result.util.KeyLabel
import com.tle.web.sections.standard.annotations.Component
import com.tle.web.sections.standard.dialog.Dialog
import com.tle.web.sections.standard.model.{LabelOption, SimpleHtmlListModel}
import com.tle.web.sections.standard.{Checkbox, SingleSelectionList, TextField, model}
import com.tle.web.sections.{SectionInfo, SectionTree}

import scala.collection.JavaConverters._

object PackageEditDetails {
  val KEY_PFXBUTTON = WebFileUploads.r.key("handlers.file.packagedetails.")
}

import PackageEditDetails._

class PackageEditDetails(parentId: String, tree: SectionTree, ctx: ControlContext, viewerHandler: ViewerHandler,
                         showRestrict: Boolean, val editingAttachment: SectionInfo => Attachment)
  extends AbstractScalaSection with RenderHelper with DetailsPage {

  type M = PackageEditDetailsModel

  tree.registerSubInnerSection(this, parentId)

  val settings = ctx.controlSettings

  @Component var displayName: TextField = _
  @ViewFactory var viewFactory: FreemarkerFactory = _

  private[details] object ExpandType extends Enumeration {
    type ExpandType = Value
    val SINGLE, EXPAND = Value
  }

  @Component var expandButtons: SingleSelectionList[ExpandType.ExpandType] = _
  @Component var viewers: SingleSelectionList[NameValue] = _
  @Component var previewCheckBox: Checkbox = _
  @Component var restrictCheckbox: Checkbox = _

  override def getDefaultPropertyName = "pd"

  override def registered(id: String, tree: SectionTree): Unit = {
    super.registered(id, tree)
    expandButtons.setListModel(new SimpleHtmlListModel[ExpandType.ExpandType](ExpandType.SINGLE, ExpandType.EXPAND) {
      override def convertToOption(obj: ExpandType.ExpandType): model.Option[ExpandType.ExpandType] = new LabelOption[ExpandType.ExpandType](
        new KeyLabel(KEY_PFXBUTTON + obj.toString.toLowerCase), obj.toString, obj)
    })
    expandButtons.setAlwaysSelect(true)
    viewers.setListModel(viewerHandler.viewerListModel)
  }

  class PackageEditDetailsModel(info: SectionInfo) {
    lazy val a = editingAttachment(info)
    var validate = false

    def getCommonIncludePath = AbstractDetailsAttachmentHandler.COMMON_INCLUDE_PATH

    def getCommonPrefix = AbstractDetailsAttachmentHandler.COMMON_PREFIX

    def getEditTitle = new TextLabel(displayName.getValue(info))

    def getDisplayName = displayName

    def getExpandButtons = expandButtons

    def getPreviewCheckBox = previewCheckBox

    def getRestrictCheckbox = restrictCheckbox

    def getErrors = (if (validate) validateDisplayName(info).toMap else Map.empty).asJava

    def isShowViewers = viewers.getListModel.getOptions(info).size() != 2

    def getViewers = viewers

    def isShowPreview = ctx.controlSettings.isAllowPreviews

    def isShowRestrict = showRestrict

    def isShowFileConversion = false

    def isShowThumbnailOption = false

    def isShowExpandButtons = isIMSPackage(a)

  }

  def isIMSPackage(a: Attachment): Boolean = a match {
    case i: ImsAttachment => true
    case _ => false
  }

  def renderDetails(context: RenderContext): (SectionRenderable, DialogRenderOptions => Unit) = {
    (renderModel("file/file-packageedit.ftl", getModel(context)), _ => ())
  }

  def prepareUI(info: SectionInfo): Unit = {
    val et = editingAttachment(info)
    et match {
      case ims: ImsAttachment => expandButtons.setSelectedValue(info, if (ims.isExpand) ExpandType.EXPAND else ExpandType.SINGLE)
      case _ => ()
    }
    displayName.setValue(info, et.getDescription)
    restrictCheckbox.setChecked(info, et.isRestricted)
    viewers.setSelectedStringValue(info, et.getViewer)
  }

  def editAttachment(info: SectionInfo, a: Attachment, ctx: ControlContext): (Attachment, Option[AttachmentDelete]) = {
    a.setDescription(displayName.getValue(info))
    if (showRestrict) {
      a.setRestricted(restrictCheckbox.isChecked(info))
    }
    a.setViewer(viewers.getSelectedValueAsString(info))
    val newExpand = expandButtons.getSelectedValue(info) == ExpandType.EXPAND
    val ad: Option[AttachmentDelete] = a match {
      case ims: ImsAttachment if newExpand != ims.isExpand =>
        ims.setExpand(newExpand)
        if (newExpand) {
          val packageFilename = ims.getUrl
          ctx.repo.createPackageNavigation(info, packageFilename,
            FileSystemConstants.IMS_FOLDER + '/' + packageFilename, packageFilename, true)
          None
        } else {
          Some(AttachmentDelete(WebFileUploads.imsResources(ctx.repo), _ => ()))
        }
      case _ => None
    }
    (a, ad)
  }


  override def previewable = false

  override def validate(info: SectionInfo): Boolean = {
    val m = getModel(info)
    m.validate = true
    m.getErrors.isEmpty
  }

  def newModel = new PackageEditDetailsModel(_)
}
