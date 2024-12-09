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

package com.tle.core.newentity.service

import com.tle.beans.Institution
import com.tle.beans.newentity.Entity
import java.time.Instant

trait EntityService {

  /** Get an Entity by UUID limited to those in the current institution.
    *
    * @param uuid
    *   UUID of the Entity.
    */
  def getByUuid(uuid: String): Entity

  /** Get all the Entity of an Institution.
    *
    * @param institution
    *   The Institution from where to get the list of Entity.
    */
  def getAllByInstitution(institution: Institution): java.util.List[Entity]

  /** Get a list of Entity by Entity type limited to those in the current institution.
    * @param typeId
    *   The type representing an Entity (e.g. cloudprovider)
    */
  def getAllByType(typeId: String): java.util.List[Entity]

  /** Create a new Entity or update an existing entity.
    *
    * @param uuid
    *   The unique ID of an Entity.
    * @param typeId
    *   Unique ID indicating what type the Entity is. (e.g. CloudProvider)
    * @param name
    *   Name of the Entity.
    * @param nameStrings
    *   Name of the Entity including locale in Json format (e.g. {"en-GB": "name"} ).
    * @param description
    *   Description of the Entity.
    * @param descriptionStrings
    *   Description of the Entity including locale in Json format (e.g. {"en-GB": "desc"}).
    * @param created
    *   The time when the Entity was created.
    * @param modified
    *   The time when the Entity was lastly modified.
    * @param owner
    *   Owner of the Entity.
    * @param data
    *   Custom data of the Entity in Json format.
    * @param institution
    *   Institution which the Entity belongs to.
    */
  def createOrUpdate(
      uuid: String,
      typeId: String,
      name: String,
      nameStrings: String,
      description: String,
      descriptionStrings: String,
      created: Instant,
      modified: Instant,
      owner: String,
      data: String,
      institution: Institution
  ): Unit

  /** Create a new Entity or update an existing entity.
    *
    * @param entity
    *   The Entity to be created or updated.
    */
  def createOrUpdate(entity: Entity): Unit

  /** Delete an Entity.
    *
    * @param entity
    *   The Entity to be delted.
    */
  def delete(entity: Entity): Unit

  /** Delete all the Entity of an Institution.
    *
    * @param institution
    *   The Institution for which to delete Entity.
    */
  def deleteAllEntity(institution: Institution): Unit
}
