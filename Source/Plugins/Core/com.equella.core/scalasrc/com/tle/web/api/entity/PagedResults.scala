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
import com.tle.legacy.LegacyGuice
import com.tle.web.api.entity.resource.AbstractBaseEntityResource
import com.tle.web.api.interfaces.beans.{BaseEntityBean, BaseEntityReadOnly, PagingBean}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object PagedResults {

  def decodeOffsetStart(resumption: String): (Int, Option[Int]) = {
    Option(resumption)
      .map(_.split(":").map(_.toInt))
      .collect {
        case Array(o, s) => (o, Some(s))
      }
      .getOrElse((0, None))
  }

  def pagedResults[BE <: BaseEntity, BEB <: BaseEntityBean](
      res: AbstractBaseEntityResource[BE, _, BEB],
      q: String,
      _privilege: java.util.List[String],
      resumption: String,
      length: Int,
      full: Boolean,
      system: Boolean,
      includeDisabled: Boolean): PagingBean[BEB] = {
    val (firstOffset, lengthFromToken) = decodeOffsetStart(resumption)
    // If resumption token provides a length then use it, or otherwise use the one from params.
    val _length = lengthFromToken.getOrElse(length)
    val privilege =
      if (_privilege.isEmpty) Set("LIST_" + res.getPrivilegeType) else _privilege.asScala.toSet
    val forFull = Set("VIEW_" + res.getPrivilegeType, "EDIT_" + res.getPrivilegeType)

    val allReqPriv = if (full) forFull ++ privilege else privilege

    def getBaseEntities(offset: Int, max: Int): List[BE] =
      res.getEntityService
        .query(new EnumerateOptions(q, offset, max, system, if (includeDisabled) null else false))
        .asScala
        .toList

    val available: Int = {
      // It's acceptable to retrieve all because targeted BaseEntities are all small datasets.
      val entities = getBaseEntities(0, -1)
      entities.collect {
        case entity
            if !LegacyGuice.aclManager
              .filterNonGrantedPrivileges(entity, allReqPriv.asJavaCollection)
              .isEmpty =>
          entity
      }.length
    }

    def collectMore(length: Int, initialOffset: Int): (Int, List[BE]) = {
      val results: ListBuffer[BE] = ListBuffer()
      var offset                  = initialOffset
      var filteredEntityQuota     = length // Indicate how many entities can be put in the result list.
      while (filteredEntityQuota > 0) {
        val entities: List[BE] = getBaseEntities(offset, length)
        if (entities.isEmpty) {
          return (offset, results.toList)
        }

        // Filter entities by permissions.
        val filteredEntities = entities.filter(
          entity =>
            !LegacyGuice.aclManager
              .filterNonGrantedPrivileges(entity, allReqPriv.asJavaCollection)
              .isEmpty)
        results ++= filteredEntities.take(filteredEntityQuota)

        // If there is no enough quota for all filtered entities, find out index of the
        // entity which is the last one in the result list. Then, plus the index to offset.
        // If quota is enough then simply plus the length of entities to offset.
        if (filteredEntityQuota < filteredEntities.length && results.nonEmpty) {
          offset += entities.indexOf(results.last) + 1
        } else {
          offset += entities.length
        }
        filteredEntityQuota = length - results.length
      }
      (offset, results.toList)
    }

    def addPrivs(privs: Set[String], b: BEB): BEB = {
      b.setReadonly(new BaseEntityReadOnly(privs.asJavaCollection))
      b
    }

    val (nextOffset, results) = collectMore(_length, firstOffset)
    val pb                    = new PagingBean[BEB]
    pb.setStart(firstOffset)
    pb.setLength(results.length)
    pb.setAvailable(available)
    // When there are no enough entities retrieved, do not return a resumption token.
    if (results.length == _length)
      pb.setResumptionToken(s"${nextOffset}:${_length}")
    // todo: Support getting full information.
    pb.setResults(results.map { be =>
      addPrivs(privilege, res.serialize(be, null, false))
    }.asJava)
    pb
  }
}
