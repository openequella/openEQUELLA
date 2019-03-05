package com.tle.core.searches
import java.util.{Date, UUID}

object SearchDefaults {

  val defaultMap: Map[String, SearchConfig] = Map(
    "search" -> SearchConfig(
      id = UUID.randomUUID(),
      index = "item",
      name = "Default search",
      nameStrings = None,
      description = None,
      descriptionStrings = None,
      created = new Date,
      modified = new Date,
      sections = Map(
        "resultsheader" -> Array(
          SortControl("relevance", true)
        ),
        "filters" -> Array(
          CollectionsControl(None, true),
          OwnerControl(None, true),
          ModifiedWithinControl(0.0, true)
        )
      )
    )
  )

  def mergeDefaults(defaults: SearchConfig, configured: SearchConfig): SearchConfig = {
    configured.copy(sections = defaults.sections ++ configured.sections)
  }
}
