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
import io.github.openequella.graphql.client.NameValue

/** View model for a name-value pair.
  *
  * @param name
  *   Display name for the value.
  * @param value
  *   Value associated with the name.
  */
final case class NameValueView(name: String, value: String)
object NameValueView {

  /** The selection builder for NameValueView.
    */
  val selector: SelectionBuilder[NameValue, NameValueView] =
    (NameValue.name ~ NameValue.value).mapN(NameValueView.apply _)
}
