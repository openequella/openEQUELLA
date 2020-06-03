package com.tle.web.api.search.model

case class SearchResult(start: Int, length: Int, available: Int, results: List[SearchResultItem])
