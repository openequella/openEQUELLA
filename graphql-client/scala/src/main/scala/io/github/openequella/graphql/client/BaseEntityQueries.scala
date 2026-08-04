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

object BaseEntityQueries {

  /** Retrieve the name of the base entity by its unique ID
    */
  def nameById[A](id: Long)(innerSelection: SelectionBuilder[LanguageBundle, A])(implicit
      encoder0: ArgEncoder[Long]
  ): SelectionBuilder[BaseEntityQueries, scala.Option[A]] = _root_.caliban.client.SelectionBuilder
    .Field("nameById", OptionOf(Obj(innerSelection)), arguments = List(Argument("id", id, "Long!")))

  /** Retrieve the access control details (ACLs) of the base entity by its unique ID. Entity type
    * agnostic - the owning entity service is resolved from the ID. An entity with no access control
    * entries returns empty lists; an unknown ID is an error.
    */
  def securityById[A](id: Long)(
      innerSelection: SelectionBuilder[BaseEntitySecurity, A]
  )(implicit encoder0: ArgEncoder[Long]): SelectionBuilder[BaseEntityQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "securityById",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("id", id, "Long!"))
    )
}
