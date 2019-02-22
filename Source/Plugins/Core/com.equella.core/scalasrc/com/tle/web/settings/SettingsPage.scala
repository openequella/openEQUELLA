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

package com.tle.web.settings

import com.tle.web.resources.PluginResourceHelper

case class SettingsPage(helper: PluginResourceHelper,
                        id: String,
                        group: String,
                        nameKey: String,
                        descKey: String,
                        uri: String,
                        uriType: String,
                        editable: () => Boolean)
    extends EditableSettings {
  override def name: String = helper.getString(nameKey)

  override def description: String = helper.getString(descKey)

  def isEditable = editable()
}
