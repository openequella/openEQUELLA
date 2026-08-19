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

final case class CollectionSummaryDisplayTemplateInput(
    configList: List[CollectionSummarySectionConfigInput] = Nil,
    hideOwner: Boolean,
    hideCollaborators: Boolean
)
object CollectionSummaryDisplayTemplateInput {
  implicit val encoder: ArgEncoder[CollectionSummaryDisplayTemplateInput] =
    new ArgEncoder[CollectionSummaryDisplayTemplateInput] {
      override def encode(value: CollectionSummaryDisplayTemplateInput): __Value =
        __ObjectValue(
          List(
            "configList" -> __ListValue(
              value.configList.map(value =>
                implicitly[ArgEncoder[CollectionSummarySectionConfigInput]].encode(value)
              )
            ),
            "hideOwner"         -> implicitly[ArgEncoder[Boolean]].encode(value.hideOwner),
            "hideCollaborators" -> implicitly[ArgEncoder[Boolean]].encode(value.hideCollaborators)
          )
        )
    }
}
