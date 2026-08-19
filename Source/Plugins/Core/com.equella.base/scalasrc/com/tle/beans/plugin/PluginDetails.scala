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

package com.tle.beans.plugin

import java.net.URL
import scala.beans.BeanProperty

/** Details required to download and register a plugin.
  *
  * @param baseUrl
  *   the base URL from which the plugin resources are served
  * @param manifestXml
  *   the JPF plugin manifest XML content
  */
case class PluginDetails(
    @BeanProperty baseUrl: URL,
    @BeanProperty manifestXml: String
)
