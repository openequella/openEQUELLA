/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.template

import com.tle.core.guice.Bind
import com.tle.legacy.LegacyGuice
import com.tle.web.sections.equella.AbstractScalaSection
import com.tle.web.sections.events.RenderEventContext
import com.tle.web.sections.registry.SectionsServlet
import com.tle.web.sections.render.{HtmlRenderer, SimpleSectionResult, TextLabel}
import com.tle.web.sections.{SectionInfo, SectionResult, SectionTree}
import javax.servlet.http.HttpServletRequest

@Bind
class SinglePageAppServlet extends SectionsServlet
{
  val tree = LegacyGuice.treeRegistry.getTreeForPath("/newpage.do")

  override def lookupTree(request: HttpServletRequest): SectionTree = tree
}

@Bind
class SinglePageApp extends AbstractScalaSection with HtmlRenderer {
  override type M = Int

  override def newModel: SectionInfo => Int = _ => 0

  override def renderHtml(context: RenderEventContext): SectionResult = {
    val decs = Decorations.getDecorations(context)
    decs.setSinglePageApp(true)
    decs.setTitle(new TextLabel(""))
    return new SimpleSectionResult("")
  }
}
