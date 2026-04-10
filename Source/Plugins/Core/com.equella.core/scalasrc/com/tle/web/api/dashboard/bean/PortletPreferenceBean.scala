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

package com.tle.web.api.dashboard.bean

import com.tle.core.dashboard.model.PortletPreferenceUpdate

/** DTO for user's UI preferences for a portlet.
  */
final case class PortletPreferenceBean(
    isClosed: Boolean,
    isMinimised: Boolean,
    column: Int,
    order: Int
)

object PortletPreferenceBean {
  def toPreference(bean: PortletPreferenceBean): PortletPreferenceUpdate =
    PortletPreferenceUpdate(
      isClosed = bean.isClosed,
      isMinimised = bean.isMinimised,
      column = bean.column,
      order = bean.order
    )
}
