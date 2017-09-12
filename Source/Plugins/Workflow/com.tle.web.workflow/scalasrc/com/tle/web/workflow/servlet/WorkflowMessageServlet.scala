package com.tle.web.workflow.servlet

import javax.inject.Inject

import com.tle.common.PathUtils
import com.tle.core.institution.InstitutionService
import com.tle.web.workflow.guice.StaticServices

object WorkflowMessageServlet {

  import StaticServices._

  val basePath = "workflow/message/"

  def messageUrl(msgUuid: String, filename: String): String =
    institutionService.institutionalise(PathUtils.urlPath(basePath, msgUuid, filename))

  def stagingUrl(stagingUuid: String, filename: String): String =
    institutionService.institutionalise(PathUtils.urlPath(basePath, "$", stagingUuid, filename))
}
