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

object CollectionMutations {

  /** Start editing an existing collection. Expected to be followed by a stopEdit or cancelEdit
    * operation.
    */
  def startEdit[A](id: Long)(
      innerSelection: SelectionBuilder[EditableEntityCollectionDefinition, A]
  )(implicit encoder0: ArgEncoder[Long]): SelectionBuilder[CollectionMutations, A] =
    _root_.caliban.client.SelectionBuilder
      .Field("startEdit", Obj(innerSelection), arguments = List(Argument("id", id, "Long!")))

  /** Clone a collection - creating a copy of the collection with a new ID.
    */
  def clone$[A](id: Long)(innerSelection: SelectionBuilder[BaseEntityReference, A])(implicit
      encoder0: ArgEncoder[Long]
  ): SelectionBuilder[CollectionMutations, scala.Option[A]] = _root_.caliban.client.SelectionBuilder
    .Field("clone", OptionOf(Obj(innerSelection)), arguments = List(Argument("id", id, "Long!")))

  /** Delete a collection - with consideration to references controllable by args.
    */
  def delete(id: Long, checkReferences: scala.Option[Boolean] = None)(implicit
      encoder0: ArgEncoder[Long],
      encoder1: ArgEncoder[scala.Option[Boolean]]
  ): SelectionBuilder[CollectionMutations, scala.Option[Unit]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "delete",
      OptionOf(Scalar()),
      arguments =
        List(Argument("id", id, "Long!"), Argument("checkReferences", checkReferences, "Boolean"))
    )

  /** Import a collection from a base64-encoded zip file.
    */
  def `import`[A](zipBase64: String)(
      innerSelection: SelectionBuilder[EditableEntityCollectionDefinition, A]
  )(implicit encoder0: ArgEncoder[String]): SelectionBuilder[CollectionMutations, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "import",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("zipBase64", zipBase64, "String!"))
    )

  /** Cancel editing a collection - discarding any changes made and unlocking the collection.
    */
  def cancelEdit(id: Long, force: scala.Option[Boolean] = None)(implicit
      encoder0: ArgEncoder[Long],
      encoder1: ArgEncoder[scala.Option[Boolean]]
  ): SelectionBuilder[CollectionMutations, scala.Option[Unit]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "cancelEdit",
      OptionOf(Scalar()),
      arguments = List(Argument("id", id, "Long!"), Argument("force", force, "Boolean"))
    )
}
