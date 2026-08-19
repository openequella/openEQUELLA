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

final case class EntityDetailsInput(
    id: Long,
    uuid: String,
    owner: String,
    dateCreated: scala.Option[java.time.LocalDateTime] = None,
    dateModified: scala.Option[java.time.LocalDateTime] = None,
    nameBundle: scala.Option[LanguageBundleInput] = None,
    descriptionBundle: scala.Option[LanguageBundleInput] = None,
    attributes: List[KVStringStringInput] = Nil,
    disabled: Boolean
)
object EntityDetailsInput {
  implicit val encoder: ArgEncoder[EntityDetailsInput] = new ArgEncoder[EntityDetailsInput] {
    override def encode(value: EntityDetailsInput): __Value =
      __ObjectValue(
        List(
          "id"          -> implicitly[ArgEncoder[Long]].encode(value.id),
          "uuid"        -> implicitly[ArgEncoder[String]].encode(value.uuid),
          "owner"       -> implicitly[ArgEncoder[String]].encode(value.owner),
          "dateCreated" -> value.dateCreated.fold(__NullValue: __Value)(value =>
            implicitly[ArgEncoder[java.time.LocalDateTime]].encode(value)
          ),
          "dateModified" -> value.dateModified.fold(__NullValue: __Value)(value =>
            implicitly[ArgEncoder[java.time.LocalDateTime]].encode(value)
          ),
          "nameBundle" -> value.nameBundle.fold(__NullValue: __Value)(value =>
            implicitly[ArgEncoder[LanguageBundleInput]].encode(value)
          ),
          "descriptionBundle" -> value.descriptionBundle.fold(__NullValue: __Value)(value =>
            implicitly[ArgEncoder[LanguageBundleInput]].encode(value)
          ),
          "attributes" -> __ListValue(
            value.attributes.map(value => implicitly[ArgEncoder[KVStringStringInput]].encode(value))
          ),
          "disabled" -> implicitly[ArgEncoder[Boolean]].encode(value.disabled)
        )
      )
  }
}
