package com.tle.web.api.searches

object SearchDefaults {

  val defaultMap: Map[String, SearchConfig] = Map(
    "search" -> SearchConfig(None, "item", Map(
      "resultsheader" -> Array(
        SortControl("relevance", true)
      ),
      "filters" -> Array(
        OwnerControl(None, true),
        ModifiedWithinControl(0.0, true),
      )
    ))
  )

  def mergeDefaults(defaults: SearchConfig, configured: SearchConfig): SearchConfig = {
    configured.copy(sections = defaults.sections ++ configured.sections)
  }
}
