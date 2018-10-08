package com.tle.web.api.searches

object SearchDefaults {

  val defaultMap: Map[String, SearchConfig] = Map(
    "search" -> SearchConfig(None, "item", Map("filters" -> Array(
      OwnerControl(None, true)
    )))
  )

  def mergeDefaults(defaults: SearchConfig, configured: SearchConfig): SearchConfig = {
    configured.copy(sections = defaults.sections ++ configured.sections)
  }
}
