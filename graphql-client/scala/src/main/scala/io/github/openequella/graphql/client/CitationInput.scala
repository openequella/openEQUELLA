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

import caliban.client._
import caliban.client.__Value._

final case class CitationInput(name: String, transformation: String)
object CitationInput {
  implicit val encoder: ArgEncoder[CitationInput] = new ArgEncoder[CitationInput] {
    override def encode(value: CitationInput): __Value =
      __ObjectValue(
        List(
          "name"           -> implicitly[ArgEncoder[String]].encode(value.name),
          "transformation" -> implicitly[ArgEncoder[String]].encode(value.transformation)
        )
      )
  }
}
