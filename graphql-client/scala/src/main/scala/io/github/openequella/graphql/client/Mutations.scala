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

object Mutations {

  /** Operations for managing Collections
    */
  def collection[A](
      innerSelection: SelectionBuilder[CollectionMutations, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, A] =
    _root_.caliban.client.SelectionBuilder.Field("collection", Obj(innerSelection))

  /** Operations for managing Metadata Schemas
    */
  def metadataSchema[A](
      innerSelection: SelectionBuilder[MetadataSchemaMutations, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, A] =
    _root_.caliban.client.SelectionBuilder.Field("metadataSchema", Obj(innerSelection))

  /** Operations for managing internal groups
    */
  def internalGroups[A](
      innerSelection: SelectionBuilder[InternalGroupMutations, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, A] =
    _root_.caliban.client.SelectionBuilder.Field("internalGroups", Obj(innerSelection))

  /** Operations for managing internal users
    */
  def internalUsers[A](
      innerSelection: SelectionBuilder[InternalUserMutations, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, A] =
    _root_.caliban.client.SelectionBuilder.Field("internalUsers", Obj(innerSelection))
}
