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

object LanguageQueries {

  /** List all configured languages
    */
  def list[A](
      innerSelection: SelectionBuilder[Language, A]
  ): SelectionBuilder[LanguageQueries, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("list", ListOf(Obj(innerSelection)))

  /** Resolve display text for the provided language bundle IDs. If a bundle ID cannot be resolved,
    * it will be omitted from the results.
    */
  def names[A](ids: List[Long] = Nil)(innerSelection: SelectionBuilder[LanguageBundleName, A])(
      implicit encoder0: ArgEncoder[List[Long]]
  ): SelectionBuilder[LanguageQueries, List[A]] = _root_.caliban.client.SelectionBuilder
    .Field("names", ListOf(Obj(innerSelection)), arguments = List(Argument("ids", ids, "[Long!]!")))
}
