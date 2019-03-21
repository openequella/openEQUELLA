package com.tle.web.searching

import java.util

import com.tle.web.sections.{BookmarkModifier, SectionInfo}

case class SearchIndexModifier(searchPage: String, index: Int) extends BookmarkModifier {
  override def addToBookmark(info: SectionInfo,
                             bookmarkState: util.Map[String, Array[String]]): Unit = {
    bookmarkState.put("search", Array(searchPage))
    bookmarkState.put("index", Array(index.toString()))
  }
}
