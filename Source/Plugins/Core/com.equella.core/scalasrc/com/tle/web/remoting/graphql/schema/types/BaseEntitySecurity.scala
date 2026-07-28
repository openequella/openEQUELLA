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
import com.tle.beans.entity.BaseEntity
import com.tle.common.EntityPack

/** The access control details of an entity, without the entity itself. This is the ACL half of
  * [[EditableEntity]], for the read-only case where there is no staging area and no lock - i.e. the
  * caller already has the entity from its type specific query (e.g. `collection.byId`) and only
  * needs its security details.
  *
  * Empty lists are a normal, meaningful result - most entities carry no ACLs of their own - so
  * queries returning this type use error semantics for the not-found case rather than nullability,
  * to keep "this entity has no access control entries" distinct from "this entity could not be
  * found".
  */
@GQLDescription(
  "The access control details (ACLs) of an entity, independent of the entity's own attributes."
)
final case class BaseEntitySecurity(
    @GQLDescription(
      "A list of access control entries, each specifying a privilege granted to a user or group, " +
        "along with whether it is overridden or granted. Empty if the entity has no entries of its own."
    )
    targetList: List[TargetListEntry],
    @GQLDescription(
      "Sub-entity access control lists, keyed by target type (e.g. per item status, per metadata " +
        "rule, per workflow task). Empty for entity types that do not have sub-entity ACLs."
    )
    otherTargetLists: List[OtherTargetList]
)

object BaseEntitySecurity {

  /** Extracts the access control details from an `EntityPack`, discarding the entity itself.
    *
    * @param pack
    *   The pack to take the target lists from - typically from `BaseEntityService.getReadOnlyPack`.
    *   Both target lists may be null, which is normal for an entity without ACLs, and yields empty
    *   lists.
    * @return
    *   The access control details of the packed entity.
    */
  def apply(pack: EntityPack[_ <: BaseEntity]): BaseEntitySecurity =
    BaseEntitySecurity(
      targetList = TargetListEntry.fromTargetList(pack.getTargetList),
      otherTargetLists = OtherTargetList.fromMap(pack.getOtherTargetLists)
    )
}
