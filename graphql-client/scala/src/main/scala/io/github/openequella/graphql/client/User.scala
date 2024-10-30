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

object User {

  /** The unique identifier for the user
    */
  def uniqueId: SelectionBuilder[User, String] =
    _root_.caliban.client.SelectionBuilder.Field("uniqueId", Scalar())

  /** The username a user authenticates with
    */
  def username: SelectionBuilder[User, String] =
    _root_.caliban.client.SelectionBuilder.Field("username", Scalar())

  /** User's email address
    */
  def email: SelectionBuilder[User, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("email", OptionOf(Scalar()))

  /** User's first name
    */
  def firstName: SelectionBuilder[User, String] =
    _root_.caliban.client.SelectionBuilder.Field("firstName", Scalar())

  /** User's last name
    */
  def lastName: SelectionBuilder[User, String] =
    _root_.caliban.client.SelectionBuilder.Field("lastName", Scalar())
}
