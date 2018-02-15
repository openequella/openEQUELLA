package com.tle.core.i18n

import com.tle.web.resources.ResourcesService

object CoreStrings {

  val lookup = ResourcesService.getResourceHelper("com.equella.core")

  def text(key: String, vals: String*): String = lookup.getString(key, vals: _*)

  def key(local: String): String = lookup.key(local)
}
