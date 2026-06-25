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

package com.tle.admin.graphql.conversion

import com.tle.admin.graphql.conversion.TargetListConverter.{fromTargetList, toTargetList}
import com.tle.beans.entity.itemdef.DynamicMetadataRule
import io.github.openequella.graphql.api.views.CollectionDynamicMetadataRuleView

import scala.util.chaining.scalaUtilChainingOps

object CollectionDynamicMetadataRuleViewConverter {
  def toDynamicMetadataRule(
      view: CollectionDynamicMetadataRuleView
  ): DynamicMetadataRule = new DynamicMetadataRule().tap { r =>
    r.setId(view.ruleId.orNull)
    r.setName(view.name.orNull)
    r.setPath(view.path.orNull)
    r.setType(view.ruleType.orNull)
    r.setTargetList(view.targetList convert toTargetList)
  }

  def fromDynamicMetadataRule(
      rule: DynamicMetadataRule
  ): CollectionDynamicMetadataRuleView =
    CollectionDynamicMetadataRuleView(
      ruleId = Option(rule.getId),
      name = Option(rule.getName),
      path = Option(rule.getPath),
      ruleType = Option(rule.getType),
      targetList = rule.getTargetList convert fromTargetList
    )
}
