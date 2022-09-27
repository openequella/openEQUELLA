package com.tle.web.template

import io.lemonlabs.uri.{QueryString, RelativeUrl}

/**
  * Centralises the various routes for the New UI - especially useful when doing `forwardToUrl`.
  */
object NewUiRoutes {
  private def buildRelativeUrl(path: String, params: Vector[(String, Option[String])]): String =
    RelativeUrl.parse(path).withQueryString(QueryString(params)).toString()

  def myResources(myResourcesType: String, subStatus: Option[String] = None): String = {
    val params = Vector(("type", Some(myResourcesType)))
    buildRelativeUrl(path = "page/myresources", params = subStatus match {
      case Some(status) => params.appended(("mstatus", Some(status)))
      case None         => params
    })
  }
}
