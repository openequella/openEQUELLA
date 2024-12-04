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

package com.tle.web.api.search

import com.tle.common.searching.SortField
import com.tle.common.searching.SortField.Type
import com.tle.core.workflow.freetext.TasksIndexer

/** Represents the `order` values which can be used for sorting specific to items under moderation -
  * which are stored in the 'task' index (hence the name).
  */
object TaskSortOrder {
  val ID_LASTACTION: String = "task_lastaction"
  val ID_SUBMITTED: String  = "task_submitted"

  /** Give an ID of an expected Task sort order, will create a `SortField` that can be used with
    * DefaultSearch to achieve that order. However if not one of the Task orders, then will simply
    * return None.
    *
    * @param id
    *   a possible Task sort order specifier
    * @return
    *   a `SortField` representing the specified order, or None if the provided ID is unknown
    */
  def apply(id: String): Option[SortField] = id match {
    case ID_LASTACTION => Some(new SortField(TasksIndexer.FIELD_LASTACTION, false, Type.LONG))
    case ID_SUBMITTED  => Some(new SortField(TasksIndexer.FIELD_STARTWORKFLOW, true, Type.LONG))
    case _             => None
  }
}
