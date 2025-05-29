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

package com.tle.web.remoting.graphql.schema.types

import caliban.schema.Annotations.GQLDescription

final case class TargetListEntry(
    @GQLDescription("Whether the privilege is granted or revoked")
    granted: Boolean,
    @GQLDescription("Has precedence over other actions in the list")
    overridden: Boolean,
    @GQLDescription(
      "The privilege being granted or revoked, e.g., 'VIEW_ITEM', 'EDIT_SCHEMA', etc."
    )
    privilege: String,
    @GQLDescription(
      "The user, group or other entity this applies to. Expecting values like '*', 'U:<username>', 'G:<groupname>' etc."
    )
    who: String,
    @GQLDescription(
      "An optional postfix for the entry, used for additional context or information - defaults to empty string"
    )
    postfix: String = ""
)

object TargetListEntry {
  def apply(entry: com.tle.common.security.TargetListEntry): TargetListEntry = {
    TargetListEntry(
      granted = entry.isGranted,
      overridden = entry.isOverride,
      privilege = entry.getPrivilege,
      who = entry.getWho,
      postfix = entry.getPostfix
    )
  }
}
