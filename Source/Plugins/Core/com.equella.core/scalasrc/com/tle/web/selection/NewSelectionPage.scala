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

package com.tle.web.selection

import com.tle.legacy.LegacyGuice
import com.tle.web.integration.IntegrationSessionData
import com.tle.web.sections.SectionInfo
import com.tle.web.sections.js.generic.expression.ObjectExpression
import com.tle.web.selection.section.RootSelectionSection
import com.tle.web.template.RenderNewTemplate
import com.tle.web.template.RenderNewTemplate.r
import io.circe.generic.auto._
import io.lemonlabs.uri.{AbsolutePath, AbsoluteUrl}
import javax.servlet.http.HttpServletRequest

object NewSelectionPage {

  val SelectionPage = "selection.html"

  val mapper = LegacyGuice.objectMapperService.createObjectMapper()

  def setupSelection(req: HttpServletRequest): Unit = {
    val _sessionId = req.getPathInfo.substring(1)
    val (sessionId, integId) = _sessionId.indexOf(':') match {
      case -1 => (_sessionId, None)
      case i  => (_sessionId.substring(0, i), Some(_sessionId.substring(i + 1)))
    }
    val uss = LegacyGuice.userSessionService
    val ss  = uss.getAttribute(sessionId).asInstanceOf[SelectionSession]
    req.setAttribute(RenderNewTemplate.ReactHtmlKey, SelectionPage)
    req.setAttribute(
      RenderNewTemplate.SetupJSKey,
      { oe: ObjectExpression =>
        oe.put("selection", mapper.writeValueAsString(ss))
        integId
          .flatMap(i => Option(uss.getAttribute(i).asInstanceOf[IntegrationSessionData]))
          .foreach { isd =>
            oe.put("integration", mapper.writeValueAsString(isd))
          }
        oe
      }
    )
  }

  def selectionUrl(info: SectionInfo, integId: String): String = {
    val rootsel =
      info.lookupSection[RootSelectionSection, RootSelectionSection](classOf[RootSelectionSection])
    val sessionid = rootsel.getSessionId(info)
    val request   = info.getRequest
    val baseUri = AbsoluteUrl.parse(LegacyGuice.urlService.getBaseUriFromRequest(request).toString)
    val baseParts = baseUri.path.parts.filter(_.nonEmpty)
    baseUri
      .withPath(AbsolutePath(baseParts).addParts("selection", s"$sessionid:$integId"))
      .toString()
  }
}
