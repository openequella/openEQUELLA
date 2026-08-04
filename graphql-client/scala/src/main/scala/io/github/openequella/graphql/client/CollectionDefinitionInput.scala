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

final case class CollectionDefinitionInput(
    details: EntityDetailsInput,
    schemaId: scala.Option[Long] = None,
    wizardCategory: scala.Option[String] = None,
    workflowId: scala.Option[Long] = None,
    reviewPeriod: scala.Option[Int] = None,
    scormPackagingTransformation: scala.Option[String] = None,
    denyDirectContribution: Boolean,
    wizard: scala.Option[CollectionWizardInput] = None,
    searchDetails: scala.Option[CollectionSearchDetailsInput] = None,
    metadataMapping: scala.Option[CollectionMetadataMappingInput] = None,
    itemMetadataRules: List[CollectionItemMetadataRuleInput] = Nil,
    dynamicMetadataRules: List[CollectionDynamicMetadataRuleInput] = Nil,
    itemSummaryDisplayTemplate: scala.Option[CollectionSummaryDisplayTemplateInput] = None
)
object CollectionDefinitionInput {
  implicit val encoder: ArgEncoder[CollectionDefinitionInput] =
    new ArgEncoder[CollectionDefinitionInput] {
      override def encode(value: CollectionDefinitionInput): __Value =
        __ObjectValue(
          List(
            "details"  -> implicitly[ArgEncoder[EntityDetailsInput]].encode(value.details),
            "schemaId" -> value.schemaId.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[Long]].encode(value)
            ),
            "wizardCategory" -> value.wizardCategory.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[String]].encode(value)
            ),
            "workflowId" -> value.workflowId.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[Long]].encode(value)
            ),
            "reviewPeriod" -> value.reviewPeriod.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[Int]].encode(value)
            ),
            "scormPackagingTransformation" -> value.scormPackagingTransformation.fold(
              __NullValue: __Value
            )(value => implicitly[ArgEncoder[String]].encode(value)),
            "denyDirectContribution" -> implicitly[ArgEncoder[Boolean]]
              .encode(value.denyDirectContribution),
            "wizard" -> value.wizard.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[CollectionWizardInput]].encode(value)
            ),
            "searchDetails" -> value.searchDetails.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[CollectionSearchDetailsInput]].encode(value)
            ),
            "metadataMapping" -> value.metadataMapping.fold(__NullValue: __Value)(value =>
              implicitly[ArgEncoder[CollectionMetadataMappingInput]].encode(value)
            ),
            "itemMetadataRules" -> __ListValue(
              value.itemMetadataRules.map(value =>
                implicitly[ArgEncoder[CollectionItemMetadataRuleInput]].encode(value)
              )
            ),
            "dynamicMetadataRules" -> __ListValue(
              value.dynamicMetadataRules.map(value =>
                implicitly[ArgEncoder[CollectionDynamicMetadataRuleInput]].encode(value)
              )
            ),
            "itemSummaryDisplayTemplate" -> value.itemSummaryDisplayTemplate.fold(
              __NullValue: __Value
            )(value => implicitly[ArgEncoder[CollectionSummaryDisplayTemplateInput]].encode(value))
          )
        )
    }
}
