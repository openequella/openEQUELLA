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

package com.tle.web.workflow.servlet

import javax.inject.Inject

import com.tle.common.PathUtils
import com.tle.core.institution.InstitutionService
import com.tle.web.workflow.guice.StaticServices

object WorkflowMessageServlet {

  import StaticServices._

  val basePath = "workflow/message/"

  def messageUrl(msgUuid: String, filename: String): String =
    institutionService.institutionalise(PathUtils.urlEncodedPath(basePath, msgUuid, filename))

  def stagingUrl(stagingUuid: String, filename: String): String =
    institutionService.institutionalise(PathUtils.urlEncodedPath(basePath, "$", stagingUuid, filename))
}
