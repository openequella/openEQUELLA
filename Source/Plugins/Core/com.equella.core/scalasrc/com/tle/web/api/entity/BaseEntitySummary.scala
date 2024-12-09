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

package com.tle.web.api.entity

import com.tle.beans.entity.BaseEntity
import com.tle.web.api.ApiHelper

/** Summary information for a BaseEntity, which should be enough for display purposes and pulling
  * any further information as required (due to the UUID).
  *
  * @param uuid
  *   The unique ID of the underlying BaseEntity
  * @param name
  *   The default locale human readable name for a BaseEntity
  */
case class BaseEntitySummary(uuid: String, name: String)

object BaseEntitySummary {
  def apply(be: BaseEntity): BaseEntitySummary =
    BaseEntitySummary(uuid = be.getUuid, name = ApiHelper.getEntityName(be))
}
