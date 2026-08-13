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
import io.github.openequella.graphql.client.TargetListEntry

/** View model for a target list entry, representing privilege access control.
  *
  * @param granted
  *   Whether the privilege is granted or revoked.
  * @param overridden
  *   Has precedence over other actions in the list.
  * @param privilege
  *   The privilege being granted or revoked, e.g., 'VIEW_ITEM', 'EDIT_SCHEMA', etc.
  * @param who
  *   The user, group or other entity this applies to. Expecting values like '*', 'U:<username>',
  *   'G:<groupname>' etc.
  * @param postfix
  *   An optional postfix for the entry, used for additional context or information.
  */
final case class TargetListEntryView(
    granted: Boolean,
    overridden: Boolean,
    privilege: String,
    who: String,
    postfix: String
)

object TargetListEntryView {
  val selector: SelectionBuilder[TargetListEntry, TargetListEntryView] =
    (
      TargetListEntry.granted ~
        TargetListEntry.overridden ~
        TargetListEntry.privilege ~
        TargetListEntry.who ~
        TargetListEntry.postfix
    ).mapN(TargetListEntryView.apply _)
}
