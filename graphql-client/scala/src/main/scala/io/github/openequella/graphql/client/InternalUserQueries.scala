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

object InternalUserQueries {

  /** List all internal users, optionally filtered by a query
    */
  def list[A](
      query: scala.Option[String] = None,
      first: scala.Option[Int] = None,
      last: scala.Option[Int] = None,
      before: scala.Option[String] = None,
      after: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[UserConnection, A])(implicit
      encoder0: ArgEncoder[scala.Option[String]],
      encoder1: ArgEncoder[scala.Option[Int]]
  ): SelectionBuilder[InternalUserQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "list",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("query", query, "String"),
        Argument("first", first, "Int"),
        Argument("last", last, "Int"),
        Argument("before", before, "String"),
        Argument("after", after, "String")
      )
    )

  /** Retrieve details of a user based on username
    */
  def byUsername[A](username: String)(
      innerSelection: SelectionBuilder[User, A]
  )(implicit encoder0: ArgEncoder[String]): SelectionBuilder[InternalUserQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "byUsername",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("username", username, "String!"))
    )

  /** Retrieve details of a user based on unique ID
    */
  def byId[A](id: String)(innerSelection: SelectionBuilder[User, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[InternalUserQueries, scala.Option[A]] = _root_.caliban.client.SelectionBuilder
    .Field("byId", OptionOf(Obj(innerSelection)), arguments = List(Argument("id", id, "String!")))
}
