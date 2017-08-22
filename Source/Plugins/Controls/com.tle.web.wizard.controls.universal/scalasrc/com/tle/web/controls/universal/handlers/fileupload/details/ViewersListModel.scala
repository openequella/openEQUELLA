package com.tle.web.controls.universal.handlers.fileupload.details

import com.tle.common.NameValue
import com.tle.web.controls.universal.handlers.fileupload.WebFileUploads
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.equella.utils.KeyOption
import com.tle.web.sections.standard.model.{DynamicHtmlListModel, Option}
import com.tle.web.viewurl.{ViewItemService, ViewableResource}

object ViewersListModel {
  val KEY_DEFAULT = WebFileUploads.r.key("handlers.file.viewer.default")
}

class ViewersListModel(viewItemService: ViewItemService, viewableResource: SectionInfo => ViewableResource) extends DynamicHtmlListModel[NameValue] {
  override protected def populateModel(info: SectionInfo): java.lang.Iterable[NameValue] = viewItemService.getEnabledViewers(info, viewableResource(info))

  override protected def getTopOption = new KeyOption[NameValue](ViewersListModel.KEY_DEFAULT, "", null)
}
