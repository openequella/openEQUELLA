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

final case class CollectionSearchDetailsInput(
    attDisplay: scala.Option[String] = None,
    disableThumbnail: Boolean,
    standardOpen: Boolean,
    integrationOpen: Boolean,
    displayNodes: List[CollectionDisplayNodeInput] = Nil
)
object CollectionSearchDetailsInput {
  implicit val encoder: ArgEncoder[CollectionSearchDetailsInput] =
    new ArgEncoder[CollectionSearchDetailsInput] {
      override def encode(value: CollectionSearchDetailsInput): __Value =
        __ObjectValue(
          List(
            "attDisplay" -> value.attDisplay.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[String]].encode(value)
            ),
            "disableThumbnail" -> implicitly[ArgEncoder[Boolean]].encode(value.disableThumbnail),
            "standardOpen"     -> implicitly[ArgEncoder[Boolean]].encode(value.standardOpen),
            "integrationOpen"  -> implicitly[ArgEncoder[Boolean]].encode(value.integrationOpen),
            "displayNodes"     -> __ListValue(
              value.displayNodes.map(value =>
                implicitly[ArgEncoder[CollectionDisplayNodeInput]].encode(value)
              )
            )
          )
        )
    }
}
