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

final case class CollectionDynamicMetadataRuleInput(
    ruleId: scala.Option[String] = None,
    name: scala.Option[String] = None,
    path: scala.Option[String] = None,
    ruleType: scala.Option[String] = None,
    targetList: List[TargetListEntryInput] = Nil
)
object CollectionDynamicMetadataRuleInput {
  implicit val encoder: ArgEncoder[CollectionDynamicMetadataRuleInput] =
    new ArgEncoder[CollectionDynamicMetadataRuleInput] {
      override def encode(value: CollectionDynamicMetadataRuleInput): __Value =
        __ObjectValue(
          List(
            "ruleId" -> value.ruleId.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[String]].encode(value)
            ),
            "name" -> value.name.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[String]].encode(value)
            ),
            "path" -> value.path.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[String]].encode(value)
            ),
            "ruleType" -> value.ruleType.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[String]].encode(value)
            ),
            "targetList" -> __ListValue(
              value.targetList.map(value =>
                implicitly[ArgEncoder[TargetListEntryInput]].encode(value)
              )
            )
          )
        )
    }
}
