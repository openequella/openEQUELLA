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

package io.github.openequella.graphql.api.views

import caliban.client.SelectionBuilder
import io.github.openequella.graphql.client.CollectionDynamicMetadataRule

/** View model for a collection dynamic metadata rule that applies ACLs based on metadata.
  *
  * @param ruleId
  *   Unique identifier for the rule.
  * @param name
  *   Display name of the rule.
  * @param path
  *   Metadata path that the rule applies to.
  * @param ruleType
  *   Type of the dynamic rule.
  * @param targetList
  *   Access control entries that this rule applies.
  */
final case class CollectionDynamicMetadataRuleView(
    ruleId: Option[String],
    name: Option[String],
    path: Option[String],
    ruleType: Option[String],
    targetList: List[TargetListEntryView]
)

object CollectionDynamicMetadataRuleView {
  val selector: SelectionBuilder[CollectionDynamicMetadataRule, CollectionDynamicMetadataRuleView] =
    (
      CollectionDynamicMetadataRule.ruleId ~
        CollectionDynamicMetadataRule.name ~
        CollectionDynamicMetadataRule.path ~
        CollectionDynamicMetadataRule.ruleType ~
        CollectionDynamicMetadataRule.targetList(TargetListEntryView.selector)
    ).mapN(CollectionDynamicMetadataRuleView.apply _)
}
