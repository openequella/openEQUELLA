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
import io.github.openequella.graphql.client.EditableEntityCollectionDefinition

/** View model for an editable collection definition, returned from import operations.
  *
  * @param collection
  *   The collection definition being edited.
  * @param stagingId
  *   The ID of the staging area for this entity.
  * @param version
  *   The version of openEQUELLA this entity is being edited against.
  * @param targetList
  *   Access control entries for the collection.
  */
final case class CollectionDefinitionEditView(
    collection: CollectionDefinitionView,
    stagingId: String,
    version: Option[String],
    targetList: List[TargetListEntryView]
)

object CollectionDefinitionEditView {
  val selector: SelectionBuilder[EditableEntityCollectionDefinition, CollectionDefinitionEditView] =
    (
      EditableEntityCollectionDefinition.entity(CollectionDefinitionView.selector) ~
        EditableEntityCollectionDefinition.stagingId ~
        EditableEntityCollectionDefinition.version ~
        EditableEntityCollectionDefinition.targetList(TargetListEntryView.selector)
    ).mapN(CollectionDefinitionEditView.apply _)
}
