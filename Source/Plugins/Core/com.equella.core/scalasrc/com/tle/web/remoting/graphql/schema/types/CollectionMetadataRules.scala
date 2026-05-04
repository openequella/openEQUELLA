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

package com.tle.web.remoting.graphql.schema.types

import caliban.schema.Annotations.GQLDescription
import com.tle.beans.entity.itemdef.{DynamicMetadataRule, ItemMetadataRule}

/** GraphQL representation of `com.tle.beans.entity.itemdef.ItemMetadataRule`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.ItemMetadataRule]]
  */
@GQLDescription("A rule that controls access to an item based on its metadata.")
final case class CollectionItemMetadataRule(
    @GQLDescription("Unique identifier for this rule")
    ruleId: Option[String],
    @GQLDescription("Display name for this rule")
    name: Option[String],
    @GQLDescription("Script that evaluates the metadata condition")
    script: Option[String]
)

object CollectionItemMetadataRule {
  def apply(rule: ItemMetadataRule): CollectionItemMetadataRule =
    CollectionItemMetadataRule(
      ruleId = Option(rule.getId),
      name = Option(rule.getName),
      script = Option(rule.getScript)
    )
}

/** GraphQL representation of `com.tle.beans.entity.itemdef.DynamicMetadataRule`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.DynamicMetadataRule]]
  */
@GQLDescription(
  "A rule that applies dynamic ACL entries to an item based on its metadata values."
)
final case class CollectionDynamicMetadataRule(
    @GQLDescription("Unique identifier for this rule")
    ruleId: Option[String],
    @GQLDescription("Display name for this rule")
    name: Option[String],
    @GQLDescription("XPath to the metadata node whose values drive the ACL")
    path: Option[String],
    @GQLDescription("Type of dynamic rule")
    ruleType: Option[String],
    @GQLDescription("ACL entries applied by this rule")
    targetList: List[TargetListEntry]
)

object CollectionDynamicMetadataRule {
  def apply(rule: DynamicMetadataRule): CollectionDynamicMetadataRule =
    CollectionDynamicMetadataRule(
      ruleId = Option(rule.getId),
      name = Option(rule.getName),
      path = Option(rule.getPath),
      ruleType = Option(rule.getType),
      targetList = convertJavaList(rule.getTargetList.getEntries)(TargetListEntry(_))
    )
}
