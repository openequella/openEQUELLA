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

package com.tle.web.api.entity

import com.tle.beans.entity.BaseEntity
import com.tle.core.entity.EnumerateOptions
import com.tle.core.entity.service.AbstractEntityService
import com.tle.legacy.LegacyGuice
import com.tle.web.api.entity.resource.AbstractBaseEntityResource
import com.tle.web.api.interfaces.beans.{BaseEntityBean, BaseEntityReadOnly, PagingBean}
import scala.jdk.CollectionConverters._
import scala.collection.mutable.ListBuffer

/** Represent an Base Entity and whether its full detail is accessible and its granted ACLs.
  * @param entity
  *   An Base Entity instance such as a Collection.
  * @param canViewFullDetail
  *   Whether the Base Entity's full detail is accessible.
  * @param grantedPrivileges
  *   A set of granted ACLs, including user-specified privileges and those required to support
  *   viewing full detail.
  */
case class PagedEntity[BE <: BaseEntity](
    entity: BE,
    canViewFullDetail: Boolean,
    grantedPrivileges: Set[String]
)

object PagedResults {

  def decodeOffsetStart(resumption: String): (Int, Option[Int]) = {
    Option(resumption)
      .map(_.split(":").map(_.toInt))
      .collect { case Array(o, s) =>
        (o, Some(s))
      }
      .getOrElse((0, None))
  }

  /** Resolves the privileges an entity ACL check should match against.
    *
    * When no privileges are requested, the entity type's `LIST_<TYPE>` privilege is used as the
    * default gate. When privileges are requested, only those the entity type recognises as filter
    * privileges are honoured; anything else is discarded.
    *
    * Discarding unrecognised privileges is a security requirement, not a convenience. Were any held
    * privilege accepted, a caller could pass one they hold on `TARGET_EVERYTHING` - for example
    * `DASHBOARD_PAGE`, typically granted to every logged in user - and have it satisfy the per
    * entity ACL check for every entity, bypassing the `LIST_<TYPE>` gate and exposing the whole
    * institution.
    *
    * For the same reason, an empty result is returned when every requested privilege is discarded.
    * Only an absent parameter falls back to the default; filtering by something the entity type
    * does not recognise must not silently widen the result set to everything the caller can list.
    *
    * @param requestedPrivileges
    *   privileges supplied by the caller, empty if the parameter was absent
    * @param privilegeType
    *   the entity type's privilege discriminator, used to build the default gate
    * @param entityFilterPrivileges
    *   privileges the entity type accepts as filters
    * @return
    *   the privileges to match on, possibly empty
    */
  def resolveEffectivePrivileges(
      requestedPrivileges: Set[String],
      privilegeType: String,
      entityFilterPrivileges: Set[String]
  ): Set[String] =
    if (requestedPrivileges.isEmpty) Set(s"LIST_$privilegeType")
    else requestedPrivileges intersect entityFilterPrivileges

  /** Builds the options used to query and count Base Entities. Disabled entities are excluded
    * unless explicitly asked for, whereas `null` means no filtering on the enabled flag at all.
    */
  private def enumerateOptions(
      q: String,
      offset: Int,
      max: Int,
      system: Boolean,
      includeDisabled: Boolean
  ): EnumerateOptions =
    new EnumerateOptions(q, offset, max, system, if (includeDisabled) null else false)

  private def getBaseEntities[BE <: BaseEntity](
      entityService: AbstractEntityService[_, BE],
      options: EnumerateOptions
  ): List[BE] =
    entityService.query(options).asScala.toList

  private def getAvailable[BE <: BaseEntity](
      entityService: AbstractEntityService[_, BE],
      options: EnumerateOptions
  ): Int =
    entityService.countAll(options).toInt

  private def filterByPermission[BE <: BaseEntity](
      entities: List[BE],
      privileges: Set[String]
  ): List[BE] =
    entities.filter(entity =>
      !LegacyGuice.aclManager
        .filterNonGrantedPrivileges(entity, privileges.asJavaCollection)
        .isEmpty
    )

  private def addPrivs[BEB <: BaseEntityBean](privs: Set[String], b: BEB): BEB = {
    b.setReadonly(new BaseEntityReadOnly(privs.asJavaCollection))
    b
  }

  def pagedResults[BE <: BaseEntity, BEB <: BaseEntityBean](
      res: AbstractBaseEntityResource[BE, _, BEB],
      q: String,
      _privilege: java.util.List[String],
      resumption: String,
      length: Int,
      full: Boolean,
      system: Boolean,
      includeDisabled: Boolean
  ): PagingBean[BEB] = {
    val (firstOffset, lengthFromToken) = decodeOffsetStart(resumption)
    // If resumption token provides a length then use it, or otherwise use the one from params.
    val _length = lengthFromToken.getOrElse(length)

    val effectivePrivileges = resolveEffectivePrivileges(
      requestedPrivileges = _privilege.asScala.toSet,
      privilegeType = res.getPrivilegeType,
      entityFilterPrivileges = res.getEntityFilterPrivileges.asScala.toSet
    )

    val forFull = Set("VIEW_" + res.getPrivilegeType, "EDIT_" + res.getPrivilegeType)

    val allReqPriv = if (full) forFull ++ effectivePrivileges else effectivePrivileges

    val entityService = res.getEntityService

    def collectMore(length: Int, initialOffset: Int): (Int, List[PagedEntity[BE]]) = {
      val results: ListBuffer[PagedEntity[BE]] = ListBuffer()
      var offset                               = initialOffset
      var filteredEntityQuota = length // Indicate how many entities can be put in the result list.
      while (filteredEntityQuota > 0) {
        val entities: List[BE] =
          getBaseEntities(
            entityService,
            enumerateOptions(q, offset, length, system, includeDisabled)
          )
        if (entities.isEmpty) {
          return (offset, results.toList)
        }

        // Filter entities by permissions.
        val filteredEntities = filterByPermission(entities, effectivePrivileges)

        results ++= filteredEntities.take(filteredEntityQuota).map { entity =>
          // Check whether all required permissions for getting full details are granted.
          val grantedPrivileges = LegacyGuice.aclManager
            .filterNonGrantedPrivileges(entity, allReqPriv.asJavaCollection)
            .asScala
            .toSet
          val canViewFullDetails = full && allReqPriv.forall(p => grantedPrivileges.contains(p))
          PagedEntity(entity, canViewFullDetails, grantedPrivileges)
        }

        // The offset for subsequent calls needs to take into account the potential
        // skipping of entities due to filtering by ACLs.
        // If there is not enough quota after filtering entities, calculate the offset
        // for the next call by adding the index of the last entity in the result list
        // to the current offset.
        // If there is enough for the quota, then simply return the current offset plus
        // the length of retrieved entities.
        if (filteredEntityQuota < filteredEntities.length && results.nonEmpty) {
          offset += entities.indexOf(results.last.entity) + 1
        } else {
          offset += entities.length
        }
        filteredEntityQuota = length - results.length
      }
      (offset, results.toList)
    }

    val (nextOffset, results) = collectMore(_length, firstOffset)
    val pb                    = new PagingBean[BEB]
    pb.setStart(firstOffset)
    pb.setLength(results.length)
    pb.setAvailable(
      getAvailable(entityService, enumerateOptions(q, 0, -1, system, includeDisabled))
    )
    // When the number of results returned are less than asked for, we have reached the end.
    // So do not return a resumption token.
    if (results.length == _length)
      pb.setResumptionToken(s"${nextOffset}:${_length}")
    pb.setResults(results.map { pagedEntity =>
      addPrivs(
        pagedEntity.grantedPrivileges,
        res.serialize(pagedEntity.entity, null, pagedEntity.canViewFullDetail)
      )
    }.asJava)
    pb
  }
}
