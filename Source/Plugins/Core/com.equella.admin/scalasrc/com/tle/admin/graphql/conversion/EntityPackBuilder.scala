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

import com.tle.beans.entity.BaseEntity
import com.tle.common.EntityPack
import com.tle.common.security.TargetList
import io.github.openequella.graphql.api.views.{OtherTargetListView, TargetListEntryView}

import scala.util.chaining.scalaUtilChainingOps

/** Builder for EntityPack, to help with the conversion of GraphQL views to EntityPacks. Because the
  * GraphQL views are not a hierarchy of classes, we can't have a single converter that takes a view
  * and produces an EntityPack. Instead, we have to have a builder that can be used by the various
  * converters to build the EntityPack.
  */
object EntityPackBuilder {
  def forStagedEntity[T <: BaseEntity](entity: T, stagingId: String): EntityPackBuilder[T] =
    new EntityPackBuilder(new EntityPack[T](entity, stagingId))

  /** For a read-only pack: nothing is being edited, so there is no staging area and hence no
    * staging ID - matching
    * `com.tle.core.entity.service.impl.AbstractEntityServiceImpl#getReadOnlyPack`.
    */
  def forEntity[T <: BaseEntity](entity: T): EntityPackBuilder[T] =
    new EntityPackBuilder(new EntityPack[T].tap(_.setEntity(entity)))
}

class EntityPackBuilder[T <: BaseEntity] private (private val entityPack: EntityPack[T]) {
  def withTargetList(targetList: List[TargetListEntryView]): EntityPackBuilder[T] = {
    entityPack.setTargetList(targetList convert TargetListConverter.toTargetList)
    this
  }

  def withOtherTargetList(otherTargetLists: List[OtherTargetListView]): EntityPackBuilder[T] = {
    val toTargetListMap: List[OtherTargetListView] => java.util.Map[Object, TargetList] =
      entityPack.getEntity match {
        case entity: com.tle.beans.entity.itemdef.ItemDefinition =>
          // The ItemDefinition is required for the conversion of OtherTargetListView to the legacy map, as some of the keys require a reference to the ItemDefinition.
          (views: List[OtherTargetListView]) =>
            OtherTargetListViewConverter.toTargetListMap(views, Some(entity))
        case _ =>
          (views: List[OtherTargetListView]) =>
            OtherTargetListViewConverter.toTargetListMap(views, None)
      }

    entityPack.setOtherTargetLists(otherTargetLists convert toTargetListMap)
    this
  }

  def withVersion(version: Option[String]): EntityPackBuilder[T] = {
    version.foreach(entityPack.setVersion)
    this
  }

  def build(): EntityPack[T] = entityPack
}
