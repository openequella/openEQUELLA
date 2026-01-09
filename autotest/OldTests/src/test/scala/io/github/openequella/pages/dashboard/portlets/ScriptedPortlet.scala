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

package io.github.openequella.pages.dashboard.portlets

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.portal.FreemarkerPortalSection

class ScriptedPortlet(context: PageContext, name: String)
    extends GenericPortlet[ScriptedPortlet](context, name) {

  /** Delegates to FreemarkerPortalSection for backward compatibility. The scripted portlet
    * functionality is implemented via the legacy Freemarker system, so we wrap it to provide a
    * consistent new UI portlet interface.
    */
  private val freemarkerSection = new FreemarkerPortalSection(context, name)

  /** Checks if the script countdown has finished for the given span ID.
    *
    * @param spanId
    *   The ID of the span element to check.
    */
  def isScriptCountdownFinished(spanId: String): Boolean =
    freemarkerSection.isScriptCountdownFinished(spanId)
}
