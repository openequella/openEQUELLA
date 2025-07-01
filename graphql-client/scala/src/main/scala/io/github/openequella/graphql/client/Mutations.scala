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

  /** Create a new internal user
    */
  def internalUserCreate[A](
      username: String,
      email: scala.Option[String] = None,
      firstName: String,
      lastName: String,
      password: String
  )(innerSelection: SelectionBuilder[User, A])(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[String]],
      encoder2: ArgEncoder[String],
      encoder3: ArgEncoder[String],
      encoder4: ArgEncoder[String]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalUserCreate",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("username", username, "String!")(encoder0),
        Argument("email", email, "String")(encoder1),
        Argument("firstName", firstName, "String!")(encoder2),
        Argument("lastName", lastName, "String!")(encoder3),
        Argument("password", password, "String!")(encoder4)
      )
    )

  /** Update an existing internal user
    */
  def internalUserUpdate[A](
      id: String,
      username: scala.Option[String] = None,
      email: scala.Option[String] = None,
      firstName: scala.Option[String] = None,
      lastName: scala.Option[String] = None,
      password: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[User, A])(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[String]],
      encoder2: ArgEncoder[scala.Option[String]],
      encoder3: ArgEncoder[scala.Option[String]],
      encoder4: ArgEncoder[scala.Option[String]],
      encoder5: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalUserUpdate",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("id", id, "String!")(encoder0),
        Argument("username", username, "String")(encoder1),
        Argument("email", email, "String")(encoder2),
        Argument("firstName", firstName, "String")(encoder3),
        Argument("lastName", lastName, "String")(encoder4),
        Argument("password", password, "String")(encoder5)
      )
    )

  /** Delete an existing internal user
    */
  def internalUserDelete(id: String)(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Unit]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalUserDelete",
      OptionOf(Scalar()),
      arguments = List(Argument("id", id, "String!")(encoder0))
    )

  /** Create a new group
    */
  def internalGroupCreate[A](name: String, parentId: scala.Option[String] = None)(
      innerSelection: SelectionBuilder[Group, A]
  )(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalGroupCreate",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("name", name, "String!")(encoder0),
        Argument("parentId", parentId, "String")(encoder1)
      )
    )

  /** Delete a group by its unique ID
    */
  def internalGroupDelete(uniqueId: String, deleteChildren: Boolean)(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[Boolean]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[Unit]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalGroupDelete",
      OptionOf(Scalar()),
      arguments = List(
        Argument("uniqueId", uniqueId, "String!")(encoder0),
        Argument("deleteChildren", deleteChildren, "Boolean!")(encoder1)
      )
    )

  /** Update a group by its unique ID - can also be used to move group within the hierarchy by
    * changing the parent ID
    */
  def internalGroupUpdate[A](
      uniqueId: String,
      name: scala.Option[String] = None,
      description: scala.Option[String] = None,
      parentId: scala.Option[String] = None,
      users: scala.Option[List[String]] = None
  )(innerSelection: SelectionBuilder[Group, A])(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[String]],
      encoder2: ArgEncoder[scala.Option[String]],
      encoder3: ArgEncoder[scala.Option[String]],
      encoder4: ArgEncoder[scala.Option[List[String]]]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalGroupUpdate",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("uniqueId", uniqueId, "String!")(encoder0),
        Argument("name", name, "String")(encoder1),
        Argument("description", description, "String")(encoder2),
        Argument("parentId", parentId, "String")(encoder3),
        Argument("users", users, "[String!]")(encoder4)
      )
    )

  /** Start editing an existing metadata schema. Expected that it will be followed by a
    * metadataSchemaStopEdit or metadataSchemaCancelEdit operation.
    */
  def metadataSchemaStartEdit[A](
      id: Long
  )(innerSelection: SelectionBuilder[EditableEntityMetadataSchema, A])(implicit
      encoder0: ArgEncoder[Long]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, A] =
    _root_.caliban.client.SelectionBuilder.Field(
      "metadataSchemaStartEdit",
      Obj(innerSelection),
      arguments = List(Argument("id", id, "Long!")(encoder0))
    )

  /** Start creating a new metadata schema. Expected that it will be followed by a
    * metadataSchemaStopEdit or metadataSchemaCancelEdit operation.
    */
  def metadataSchemaStartCreate[A](
      innerSelection: SelectionBuilder[EditableEntitySkeleton, A]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootMutation, A] =
    _root_.caliban.client.SelectionBuilder.Field("metadataSchemaStartCreate", Obj(innerSelection))
}
