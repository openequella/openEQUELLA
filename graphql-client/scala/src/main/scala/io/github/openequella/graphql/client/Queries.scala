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

object Queries {

  /** List all internal users, optionally filtered by a query
    */
  def internalUsers[A](
      query: scala.Option[String] = None,
      first: scala.Option[Int] = None,
      last: scala.Option[Int] = None,
      before: scala.Option[String] = None,
      after: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[UserConnection, A])(implicit
      encoder0: ArgEncoder[scala.Option[String]],
      encoder1: ArgEncoder[scala.Option[Int]],
      encoder2: ArgEncoder[scala.Option[Int]],
      encoder3: ArgEncoder[scala.Option[String]],
      encoder4: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalUsers",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("query", query, "String")(encoder0),
        Argument("first", first, "Int")(encoder1),
        Argument("last", last, "Int")(encoder2),
        Argument("before", before, "String")(encoder3),
        Argument("after", after, "String")(encoder4)
      )
    )

  /** Retrieve details of a user based on username
    */
  def internalUserByUsername[A](username: String)(innerSelection: SelectionBuilder[User, A])(
      implicit encoder0: ArgEncoder[String]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalUserByUsername",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("username", username, "String!")(encoder0))
    )

  /** Retrieve details of a user based on unique ID
    */
  def internalUserById[A](id: String)(innerSelection: SelectionBuilder[User, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalUserById",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("id", id, "String!")(encoder0))
    )

  /** Retrieve a group by its unique ID
    */
  def internalGroupById[A](uniqueId: String)(innerSelection: SelectionBuilder[Group, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalGroupById",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("uniqueId", uniqueId, "String!")(encoder0))
    )

  /** Retrieve a group by its name
    */
  def internalGroupByName[A](name: String)(innerSelection: SelectionBuilder[Group, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalGroupByName",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("name", name, "String!")(encoder0))
    )

  /** List all groups at a specific level in the hierarchy determined by the parent ID - or none for
    * the root.
    */
  def internalGroups[A](
      parentId: scala.Option[String] = None,
      first: scala.Option[Int] = None,
      last: scala.Option[Int] = None,
      before: scala.Option[String] = None,
      after: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[GroupConnection, A])(implicit
      encoder0: ArgEncoder[scala.Option[String]],
      encoder1: ArgEncoder[scala.Option[Int]],
      encoder2: ArgEncoder[scala.Option[Int]],
      encoder3: ArgEncoder[scala.Option[String]],
      encoder4: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalGroups",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("parentId", parentId, "String")(encoder0),
        Argument("first", first, "Int")(encoder1),
        Argument("last", last, "Int")(encoder2),
        Argument("before", before, "String")(encoder3),
        Argument("after", after, "String")(encoder4)
      )
    )

  /** List multiple groups by their unique IDs, invalid IDs will be ignored
    */
  def internalGroupsByIds[A](
      uniqueIds: List[String] = Nil,
      first: scala.Option[Int] = None,
      last: scala.Option[Int] = None,
      before: scala.Option[String] = None,
      after: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[GroupConnection, A])(implicit
      encoder0: ArgEncoder[List[String]],
      encoder1: ArgEncoder[scala.Option[Int]],
      encoder2: ArgEncoder[scala.Option[Int]],
      encoder3: ArgEncoder[scala.Option[String]],
      encoder4: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalGroupsByIds",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("uniqueIds", uniqueIds, "[String!]!")(encoder0),
        Argument("first", first, "Int")(encoder1),
        Argument("last", last, "Int")(encoder2),
        Argument("before", before, "String")(encoder3),
        Argument("after", after, "String")(encoder4)
      )
    )

  /** Search for groups anywhere within the hierarchy by name (wildcard search)
    */
  def internalGroupSearch[A](
      query: String,
      first: scala.Option[Int] = None,
      last: scala.Option[Int] = None,
      before: scala.Option[String] = None,
      after: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[GroupConnection, A])(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[Int]],
      encoder2: ArgEncoder[scala.Option[Int]],
      encoder3: ArgEncoder[scala.Option[String]],
      encoder4: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalGroupSearch",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("query", query, "String!")(encoder0),
        Argument("first", first, "Int")(encoder1),
        Argument("last", last, "Int")(encoder2),
        Argument("before", before, "String")(encoder3),
        Argument("after", after, "String")(encoder4)
      )
    )

  /** List user ids for all users in the specified group
    */
  def internalGroupUsers[A](
      uniqueId: String,
      first: scala.Option[Int] = None,
      last: scala.Option[Int] = None,
      before: scala.Option[String] = None,
      after: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[StringConnection, A])(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[Int]],
      encoder2: ArgEncoder[scala.Option[Int]],
      encoder3: ArgEncoder[scala.Option[String]],
      encoder4: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[_root_.caliban.client.Operations.RootQuery, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "internalGroupUsers",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("uniqueId", uniqueId, "String!")(encoder0),
        Argument("first", first, "Int")(encoder1),
        Argument("last", last, "Int")(encoder2),
        Argument("before", before, "String")(encoder3),
        Argument("after", after, "String")(encoder4)
      )
    )
}
