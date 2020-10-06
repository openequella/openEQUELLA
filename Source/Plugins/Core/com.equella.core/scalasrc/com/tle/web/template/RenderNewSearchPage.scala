package com.tle.web.template

import com.tle.web.sections.SectionResult
import com.tle.web.sections.events.RenderEventContext
import com.tle.web.sections.render.SimpleSectionResult

object RenderNewSearchPage {
  def renderNewSearchPage(context: RenderEventContext): SectionResult = {
    val (p, body) = RenderNewTemplate.parseEntryHtml("NewSearchPage.html")
    context.getBody.addPreRenderable(p)
    new SimpleSectionResult(body.body().children())
  }
}
