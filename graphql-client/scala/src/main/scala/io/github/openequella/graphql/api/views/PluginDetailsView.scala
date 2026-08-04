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

package io.github.openequella.graphql.api.views

import caliban.client.SelectionBuilder
import io.github.openequella.graphql.client._
import io.github.openequella.graphql.client.{PluginDetails => PluginDetailsGQL}

/** View model for downloadable plugin details.
  *
  * @param baseUrl
  *   Base URL from which the plugin can be downloaded.
  * @param manifestXml
  *   XML manifest content describing the plugin.
  */
final case class PluginDetailsView(baseUrl: String, manifestXml: String)

object PluginDetailsView {

  /** The selection builder for [[PluginDetailsView]].
    */
  val selector: SelectionBuilder[PluginDetails, PluginDetailsView] =
    (PluginDetailsGQL.baseUrl ~ PluginDetailsGQL.manifestXml).mapN(PluginDetailsView.apply _)
}
