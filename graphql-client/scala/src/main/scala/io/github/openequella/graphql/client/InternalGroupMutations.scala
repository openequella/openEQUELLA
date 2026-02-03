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

object InternalGroupMutations {

  /** Create a new group
    */
  def create[A](name: String, parentId: scala.Option[String] = None)(
      innerSelection: SelectionBuilder[Group, A]
  )(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[InternalGroupMutations, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "create",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("name", name, "String!"), Argument("parentId", parentId, "String"))
    )

  /** Delete a group by its unique ID
    */
  def delete(uniqueId: String, deleteChildren: Boolean)(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[Boolean]
  ): SelectionBuilder[InternalGroupMutations, scala.Option[Unit]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "delete",
      OptionOf(Scalar()),
      arguments = List(
        Argument("uniqueId", uniqueId, "String!"),
        Argument("deleteChildren", deleteChildren, "Boolean!")
      )
    )

  /** Update a group by its unique ID - can also be used to move group within the hierarchy by
    * changing the parent ID
    */
  def update[A](
      uniqueId: String,
      name: scala.Option[String] = None,
      description: scala.Option[String] = None,
      parentId: scala.Option[String] = None,
      users: scala.Option[List[String]] = None
  )(innerSelection: SelectionBuilder[Group, A])(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[String]],
      encoder2: ArgEncoder[scala.Option[List[String]]]
  ): SelectionBuilder[InternalGroupMutations, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "update",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("uniqueId", uniqueId, "String!"),
        Argument("name", name, "String"),
        Argument("description", description, "String"),
        Argument("parentId", parentId, "String"),
        Argument("users", users, "[String!]")
      )
    )
}
