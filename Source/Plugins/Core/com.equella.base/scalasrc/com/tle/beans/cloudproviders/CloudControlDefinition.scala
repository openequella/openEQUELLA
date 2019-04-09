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

package com.tle.beans.cloudproviders

import java.util.UUID

case class CloudConfigOption(name: String, value: String)

object CloudControlConfigType extends Enumeration {
  val XPath, Textfield, Dropdown, Check, Radio = Value
}

case class CloudControlConfig(id: String,
                              name: String,
                              description: String,
                              configType: CloudControlConfigType.Value,
                              options: java.lang.Iterable[CloudConfigOption],
                              min: Int,
                              max: Int) {
  def isConfigMandatory: Boolean = {
    !(min < max)
  }
}

case class CloudControlDefinition(providerId: UUID,
                                  controlId: String,
                                  name: String,
                                  iconUrl: String,
                                  configDefinition: java.util.List[CloudControlConfig])
