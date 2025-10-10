/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.tle.web.searching.section.{RootAdvancedSearchSection, RootSearchSection}
import com.tle.web.sections.ajax.AjaxEffects
import com.tle.web.sections.events.{RenderEventContext, StandardRenderContext}
import com.tle.web.sections.js.JSStatements
import com.tle.web.sections.render.SimpleSectionResult

object RenderNewSearchPage {
  private def buildSection(
      context: RenderEventContext,
      file: String,
      additionalJS: Option[JSStatements] = None
  ): SimpleSectionResult = {
    val (p, body) = RenderNewTemplate.parseEntryHtml(file)
    context.getBody.addPreRenderable(p)
    context.getBody.addPreRenderable(AjaxEffects.EFFECTS_LIB)

    for {
      js <- additionalJS
    } yield context.getRootRenderContext.asInstanceOf[StandardRenderContext].addStatements(js)

    new SimpleSectionResult(body.body().children())
  }

  def renderNewSearchPage(
      context: RenderEventContext,
      additionalJS: JSStatements
  ): SimpleSectionResult = {
    val file = context.getSectionObject match {
      case _: RootAdvancedSearchSection => "AdvancedSearchPage.html"
      case _: RootSearchSection         => "SearchPage.html"
    }
    buildSection(context, file, Option(additionalJS))
  }

  def renderNewMyResourcesPage(context: RenderEventContext): SimpleSectionResult =
    buildSection(context, "MyResourcesPage.html")

  def renderNewHierarchyPage(context: RenderEventContext): SimpleSectionResult =
    buildSection(context, "HierarchyPage.html")

  def renderNewHierarchyBrowsePage(context: RenderEventContext): SimpleSectionResult =
    buildSection(context, "HierarchyBrowsePage.html")

  def renderNewFavouritesPage(context: RenderEventContext): SimpleSectionResult =
    buildSection(context, "FavouritesPage.html")
}
