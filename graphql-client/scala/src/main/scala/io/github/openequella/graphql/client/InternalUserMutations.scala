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

object InternalUserMutations {

  /** Create a new internal user
    */
  def create[A](
      username: String,
      email: scala.Option[String] = None,
      firstName: String,
      lastName: String,
      password: String
  )(innerSelection: SelectionBuilder[User, A])(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[InternalUserMutations, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "create",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("username", username, "String!"),
        Argument("email", email, "String"),
        Argument("firstName", firstName, "String!"),
        Argument("lastName", lastName, "String!"),
        Argument("password", password, "String!")
      )
    )

  /** Update an existing internal user
    */
  def update[A](
      id: String,
      username: scala.Option[String] = None,
      email: scala.Option[String] = None,
      firstName: scala.Option[String] = None,
      lastName: scala.Option[String] = None,
      password: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[User, A])(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[InternalUserMutations, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "update",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("id", id, "String!"),
        Argument("username", username, "String"),
        Argument("email", email, "String"),
        Argument("firstName", firstName, "String"),
        Argument("lastName", lastName, "String"),
        Argument("password", password, "String")
      )
    )

  /** Delete an existing internal user
    */
  def delete(id: String)(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[InternalUserMutations, scala.Option[Unit]] =
    _root_.caliban.client.SelectionBuilder
      .Field("delete", OptionOf(Scalar()), arguments = List(Argument("id", id, "String!")))
}
