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

package com.tle.beans.ump

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.json.JsonMapper

/** JSON (de)serialisation for [[UserManagementSettings]] beans. Shared by the server-side GraphQL
  * user directory configuration endpoints and the Admin Console, so that both sides always use the
  * same Jackson configuration.
  *
  * Settings beans are persisted field-by-field (see the `@Property` annotations), and some expose
  * derived getters with no backing field (e.g.
  * [[com.tle.beans.usermanagement.shibboleth.wrapper.ExternalAuthorisationWrapperSettings]] derives
  * `isRemoteUser` from `usageType`), so (de)serialisation must be field-based.
  */
object UserManagementSettingsSerialization {
  private val objectMapper = JsonMapper
    .builder()
    .visibility(PropertyAccessor.ALL, Visibility.NONE)
    .visibility(PropertyAccessor.FIELD, Visibility.ANY)
    .build()

  /** Deserialises a plugin configuration JSON string into the given settings class. */
  def fromJson[T <: UserManagementSettings](configJson: String, settingsClass: Class[T]): T =
    objectMapper.readValue(configJson, settingsClass)

  /** Serialises a settings instance to a JSON string. */
  def toJson(settings: UserManagementSettings): String =
    objectMapper.writeValueAsString(settings)
}
