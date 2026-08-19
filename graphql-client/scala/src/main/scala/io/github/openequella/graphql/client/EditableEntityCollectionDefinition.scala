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

object EditableEntityCollectionDefinition {

  /** Base entity being edited
    */
  def entity[A](
      innerSelection: SelectionBuilder[CollectionDefinition, A]
  ): SelectionBuilder[EditableEntityCollectionDefinition, A] =
    _root_.caliban.client.SelectionBuilder.Field("entity", Obj(innerSelection))

  /** The ID of the staging area for this entity. This is used to store files before they are
    * committed.
    */
  def stagingId: SelectionBuilder[EditableEntityCollectionDefinition, String] =
    _root_.caliban.client.SelectionBuilder.Field("stagingId", Scalar())

  /** The version of openEQUELLA this entity is being edited against. This is used to ensure that
    * edits are applied to the correct version.
    */
  def version: SelectionBuilder[EditableEntityCollectionDefinition, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("version", OptionOf(Scalar()))

  /** A list of access control entries, each specifying a privilege granted to a user or group,
    * along with whether it is overridden or granted.
    */
  def targetList[A](
      innerSelection: SelectionBuilder[TargetListEntry, A]
  ): SelectionBuilder[EditableEntityCollectionDefinition, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("targetList", ListOf(Obj(innerSelection)))

  /** Sub-entity access control lists, keyed by target type (e.g. per item status, per metadata
    * rule, per workflow task). Empty for entity types that do not have sub-entity ACLs.
    */
  def otherTargetLists[A](
      innerSelection: SelectionBuilder[OtherTargetList, A]
  ): SelectionBuilder[EditableEntityCollectionDefinition, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("otherTargetLists", ListOf(Obj(innerSelection)))
}
