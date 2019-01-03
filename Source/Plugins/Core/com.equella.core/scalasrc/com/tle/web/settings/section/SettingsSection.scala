/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.settings.section

import com.tle.core.guice.Bind
import com.tle.core.i18n.CoreStrings
import com.tle.core.resources.CoreUrls
import com.tle.web.sections.SectionResult
import com.tle.web.sections.equella.AbstractScalaSection
import com.tle.web.sections.events.RenderEventContext
import com.tle.web.sections.render._
import com.tle.web.sections.standard.renderers.DivRenderer
import com.tle.web.template.{Decorations, RenderNewTemplate}

@Bind
class SettingsSection extends AbstractScalaSection with HtmlRenderer {

  override type M = Unit

  override def newModel = _ => ()

  val jsUrl = CoreUrls.url("reactjs/index.js")

  // "title", "settings.filter.all", "settings.categories"

  override def renderHtml(context: RenderEventContext): SectionResult = {
    val decs = Decorations.getDecorations(context)
    decs.setExcludeForm(true)
    decs.setTitle(new TextLabel(CoreStrings.text("title")))
    val scriptTag = new TagState()
    scriptTag.addTagProcessor(new ExtraAttributes("src", jsUrl))
    scriptTag.addPreRenderable(RenderNewTemplate.bundleJs)
    CombinedRenderer.combineResults(
      new DivRenderer(new TagState("settingsPage")),
      new TagRenderer("script", scriptTag)
    )
  }
}
