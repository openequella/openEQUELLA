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
import org.openqa.selenium.By

/** Represents a Browse Portlet.
  *
  * @param context
  *   The PageContext for the current test session.
  * @param name
  *   The name of the Browse Portlet.
  */
class BrowsePortlet(context: PageContext, name: String)
    extends GenericPortlet[BrowsePortlet](context, name) {

  /** Checks if a topic with the given name exists in the Browse Portlet.
    *
    * @param name
    *   The name of the topic to check for.
    * @return
    *   True if the topic exists, false otherwise.
    */
  def hasTopic(name: String): Boolean = {
    val topicXpath = By.xpath(s"$portletXpath//a[text()='$name']")
    isPresent(topicXpath)
  }
}
