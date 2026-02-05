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

object MetadataSchemaMutations {

  /** Start editing an existing metadata schema. Expected that it will be followed by a stopEdit or
    * cancelEdit operation.
    */
  def startEdit[A](id: Long)(innerSelection: SelectionBuilder[EditableEntityMetadataSchema, A])(
      implicit encoder0: ArgEncoder[Long]
  ): SelectionBuilder[MetadataSchemaMutations, A] = _root_.caliban.client.SelectionBuilder
    .Field("startEdit", Obj(innerSelection), arguments = List(Argument("id", id, "Long!")))

  /** Start creating a new metadata schema. Typically followed by an add operation with details for
    * new schema.
    */
  def startCreate[A](
      innerSelection: SelectionBuilder[EditableEntitySkeleton, A]
  ): SelectionBuilder[MetadataSchemaMutations, A] =
    _root_.caliban.client.SelectionBuilder.Field("startCreate", Obj(innerSelection))

  /** Stop editing a metadata schema - discarding any changes made, and unlocking schema.
    */
  def cancelEdit(id: Long, force: scala.Option[Boolean] = None)(implicit
      encoder0: ArgEncoder[Long],
      encoder1: ArgEncoder[scala.Option[Boolean]]
  ): SelectionBuilder[MetadataSchemaMutations, scala.Option[Unit]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "cancelEdit",
      OptionOf(Scalar()),
      arguments = List(Argument("id", id, "Long!"), Argument("force", force, "Boolean"))
    )
}
