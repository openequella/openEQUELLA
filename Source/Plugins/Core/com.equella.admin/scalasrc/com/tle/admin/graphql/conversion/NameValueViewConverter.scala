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

package com.tle.admin.graphql.conversion

import com.tle.common.NameValue
import io.github.openequella.graphql.api.views.NameValueView

/** Converter for transforming GraphQL [[NameValueView]] to the [[NameValue]].
  */
object NameValueViewConverter {

  /** Converts a GraphQL [[NameValueView]] into a [[NameValue]].
    *
    * @param view
    *   the GraphQL name-value pair containing name and value.
    * @return
    *   a new [[NameValue]] entity populated with the provided values.
    */
  def toNameValue(
      view: NameValueView
  ): NameValue = {
    val to = new NameValue
    to.setName(view.name)
    to.setValue(view.value)

    to
  }
}
