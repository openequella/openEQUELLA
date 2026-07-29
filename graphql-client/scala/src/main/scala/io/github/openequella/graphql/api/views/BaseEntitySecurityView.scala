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
import io.github.openequella.graphql.client.BaseEntitySecurity

/** View model for the access control details of an entity, without the entity itself. This is the
  * ACL half of [[CollectionDefinitionEditView]] and [[MetadataSchemaEditView]], for the read-only
  * case where there is no staging area and no lock.
  *
  * Both lists being empty is a normal result - it means the entity has no access control entries of
  * its own - and is distinct from the entity not being found, which is reported as an error.
  *
  * @param targetList
  *   Access control entries for the entity.
  * @param otherTargetLists
  *   Sub-entity access control lists (e.g. per item status, per metadata rule, per workflow task).
  */
final case class BaseEntitySecurityView(
    targetList: List[TargetListEntryView],
    otherTargetLists: List[OtherTargetListView]
)

object BaseEntitySecurityView {
  val selector: SelectionBuilder[BaseEntitySecurity, BaseEntitySecurityView] =
    (
      BaseEntitySecurity.targetList(TargetListEntryView.selector) ~
        BaseEntitySecurity.otherTargetLists(OtherTargetListView.selector)
    ).mapN(BaseEntitySecurityView.apply _)
}
