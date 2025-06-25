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

object EditableEntityMetadataSchema {

  /** Base entity being edited
    */
  def entity[A](
      innerSelection: SelectionBuilder[MetadataSchema, A]
  ): SelectionBuilder[EditableEntityMetadataSchema, A] =
    _root_.caliban.client.SelectionBuilder.Field("entity", Obj(innerSelection))

  /** The ID of the staging area for this entity. This is used to store files before they are
    * committed.
    */
  def stagingId: SelectionBuilder[EditableEntityMetadataSchema, String] =
    _root_.caliban.client.SelectionBuilder.Field("stagingId", Scalar())

  /** The version of openEQUELLA this entity is being edited against. This is used to ensure that
    * edits are applied to the correct version.
    */
  def version: SelectionBuilder[EditableEntityMetadataSchema, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("version", OptionOf(Scalar()))

  /** A list of access control entries, each specifying a privilege granted to a user or group,
    * along with whether it is overridden or granted.
    */
  def targetList[A](
      innerSelection: SelectionBuilder[TargetListEntry, A]
  ): SelectionBuilder[EditableEntityMetadataSchema, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("targetList", ListOf(Obj(innerSelection)))
}
