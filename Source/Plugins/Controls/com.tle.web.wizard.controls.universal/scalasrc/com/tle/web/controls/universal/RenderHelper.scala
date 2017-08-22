package com.tle.web.controls.universal

import com.tle.web.freemarker.{FreemarkerFactory, FreemarkerSectionResult}
import com.tle.web.sections.render.SectionRenderable

trait RenderHelper {
  def viewFactory : FreemarkerFactory
  def renderModel(ftl: String, m: AnyRef): FreemarkerSectionResult = {
    val fr = viewFactory.createResultWithModel(ftl, m).asInstanceOf[FreemarkerSectionResult]
    fr.addExtraObject("s", m)
    fr
  }
}
