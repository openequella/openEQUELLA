package com.tle.web.sections.equella

import com.tle.web.sections.SectionWriter
import com.tle.web.sections.events.PreRenderContext
import com.tle.web.sections.render.SectionRenderable

class ScalaSectionRenderable(render: SectionWriter => Unit, prerender: PreRenderContext => Unit = _ => ()) extends SectionRenderable {
  override def realRender(writer: SectionWriter): Unit = render(writer)
  override def preRender(info: PreRenderContext): Unit = prerender(info)
}
