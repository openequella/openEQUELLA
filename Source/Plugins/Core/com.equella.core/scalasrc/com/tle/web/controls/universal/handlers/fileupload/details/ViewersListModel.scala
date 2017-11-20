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
