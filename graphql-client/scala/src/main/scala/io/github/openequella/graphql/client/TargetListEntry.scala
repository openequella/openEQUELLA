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

package io.github.openequella.graphql.client

import caliban.client.FieldBuilder._
import caliban.client._

object TargetListEntry {

  /** Whether the privilege is granted or revoked
    */
  def granted: SelectionBuilder[TargetListEntry, Boolean] =
    _root_.caliban.client.SelectionBuilder.Field("granted", Scalar())

  /** Has precedence over other actions in the list
    */
  def overridden: SelectionBuilder[TargetListEntry, Boolean] =
    _root_.caliban.client.SelectionBuilder.Field("overridden", Scalar())

  /** The privilege being granted or revoked, e.g., 'VIEW_ITEM', 'EDIT_SCHEMA', etc.
    */
  def privilege: SelectionBuilder[TargetListEntry, String] =
    _root_.caliban.client.SelectionBuilder.Field("privilege", Scalar())

  /** The user, group or other entity this applies to. Expecting values like '*', 'U:<username>',
    * 'G:<groupname>' etc.
    */
  def who: SelectionBuilder[TargetListEntry, String] =
    _root_.caliban.client.SelectionBuilder.Field("who", Scalar())

  /** An optional postfix for the entry, used for additional context or information - defaults to
    * empty string
    */
  def postfix: SelectionBuilder[TargetListEntry, String] =
    _root_.caliban.client.SelectionBuilder.Field("postfix", Scalar())
}
