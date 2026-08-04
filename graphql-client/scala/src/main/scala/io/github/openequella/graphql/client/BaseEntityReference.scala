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

object BaseEntityReference {

  /** The database ID of the entity
    */
  def id: SelectionBuilder[BaseEntityReference, Long] =
    _root_.caliban.client.SelectionBuilder.Field("id", Scalar())

  /** The UUID of the entity
    */
  def uuid: SelectionBuilder[BaseEntityReference, String] =
    _root_.caliban.client.SelectionBuilder.Field("uuid", Scalar())

  /** The language bundle ID of the entity
    */
  def bundleId: SelectionBuilder[BaseEntityReference, Long] =
    _root_.caliban.client.SelectionBuilder.Field("bundleId", Scalar())

  /** The owner of the entity
    */
  def owner: SelectionBuilder[BaseEntityReference, String] =
    _root_.caliban.client.SelectionBuilder.Field("owner", Scalar())

  /** Whether this entity is a collection
    */
  def forCollection: SelectionBuilder[BaseEntityReference, Boolean] =
    _root_.caliban.client.SelectionBuilder.Field("forCollection", Scalar())
}
