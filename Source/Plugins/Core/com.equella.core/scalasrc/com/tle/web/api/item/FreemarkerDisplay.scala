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

package com.tle.web.api.item

import java.io.StringReader

import com.dytech.devlib.PropBagEx
import com.dytech.edge.common.PropBagWrapper
import com.tle.beans.item.ItemPack
import com.tle.core.scripting.service.StandardScriptContextParams
import com.tle.legacy.LegacyGuice
import com.tle.web.api.item.interfaces.beans.{HtmlSummarySection, ItemSummarySection}
import com.tle.web.sections.events.StandardRenderContext
import com.tle.web.sections.{SectionInfo, SectionUtils}
import com.tle.web.viewurl.ItemSectionInfo
import scala.jdk.CollectionConverters._

object FreemarkerDisplay {

  def create(info: SectionInfo, itemInfo: ItemSectionInfo, sectionTitle: String, config: String) = {

    val cxml   = new PropBagEx(config)
    val script = cxml.getNode("script")
    val markup = cxml.getNode("markup")

    // The xmlService.getXmlForXslt call happens twice if you have both XSLT
    // and Freemarker. No harm but a bit ghetto

    val context = new StandardRenderContext(info)
    val itemPack = new ItemPack(
      itemInfo.getItem,
      LegacyGuice.itemXsltService.getXmlForXslt(context, itemInfo),
      null
    )
    val params = new StandardScriptContextParams(itemPack, null, true, null)

    params.getAttributes.put("context", context.getPreRenderContext)
    val scriptService = LegacyGuice.scriptingService
    val scriptContext = scriptService.createScriptContext(params)
    scriptContext.addScriptObject("attributes", new PropBagWrapper(new PropBagEx))
    // Run script
    scriptService.executeScript(script, "itemSummary", scriptContext, false)

    // Uses custom Freemarker Factory (Removes access to internal sections
    // functions) AdvancedWebScriptControl uses similar
    val result = LegacyGuice.basicFreemarkerFactory.createResult(
      "viewItemFreemarker", // $NON-NLS-1$
      new StringReader(markup),
      context
    )
    for (entry <- scriptContext.getScriptObjects.entrySet.asScala) {
      result.addExtraObject(entry.getKey, entry.getValue)
    }
    HtmlSummarySection(
      sectionTitle,
      false,
      "freemarker",
      SectionUtils.renderToString(context, result)
    )
  }
}
