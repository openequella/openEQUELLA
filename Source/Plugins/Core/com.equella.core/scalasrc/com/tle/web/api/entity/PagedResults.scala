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

import scala.annotation.tailrec
import scala.collection.JavaConverters._

object PagedResults {

  def decodeOffsetStart(resumption: String): (Int, Option[Int]) = {
    Option(resumption)
      .map(_.split(":").map(_.toInt))
      .collect {
        case Array(o, s) => (o, Some(s))
      }
      .getOrElse((0, None))
  }

  val MaxEntities = 200

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

    // All Collections, including dynamic Collections.
    val baseEntities = res.getEntityService.query(
      new EnumerateOptions(q, -1, MaxEntities, system, if (includeDisabled) null else false))
    // A list of Collections which users have permissions to access.
    val availableEntities = baseEntities.asScala.collect {
      case be if !LegacyGuice.aclManager.filterNonGrantedPrivileges(be, privilege.asJava).isEmpty =>
        be
    }
    // Return a map where the key is a base entity and the value is a another map for the entity's ACLs.
    // Depending on the value of offset, some available entities are not added into the map.
    def getPrivilegeMap(offset: Int): Map[BE, java.util.Map[String, java.lang.Boolean]] =
      LegacyGuice.aclManager
        .getPrivilegesForObjects(allReqPriv.asJavaCollection, availableEntities.drop(offset).asJava)
        .asScala
        .toMap

    @tailrec
    def collectMore(
        len: Int,
        offset: Int,
        vec: Vector[(BE, Boolean, Set[String])]): (Vector[(BE, Boolean, Set[String])]) = {
      if (len <= 0 || availableEntities.length < offset) {
        (vec)
      } else {
        object ExtraPrivs {
          def unapply(be: BE): Option[(BE, Set[String])] = getPrivilegeMap(offset).get(be).map {
            p =>
              (be, allReqPriv & p.asScala.keySet)
          }
        }
        val withPriv = availableEntities
          .collect {
            case ExtraPrivs(be, privs) if privs.count(privilege) > 0 =>
              (be, full && privs.exists(forFull), privs)
          }
          .take(len)
        val nextOffset = offset + len
        collectMore(len - withPriv.size, nextOffset, vec ++ withPriv)
      }

    }
    def addPrivs(privs: Set[String], b: BEB): BEB = {
      b.setReadonly(new BaseEntityReadOnly(privs.asJavaCollection))
      b
    }

    val (results) = collectMore(_length, firstOffset, Vector.empty)
    val available = availableEntities.length
    val pb        = new PagingBean[BEB]
    pb.setStart(firstOffset)
    pb.setLength(results.length)
    pb.setAvailable(available)
    // Include resumption token if there are items which can be retrieved in next request.
    if (results.length + firstOffset < available)
      pb.setResumptionToken(s"${firstOffset + results.length}:${_length}")
    pb.setResults(results.map {
      case (be, canFull, privs) => addPrivs(privs, res.serialize(be, null, canFull))
    }.asJava)
    pb
  }
}
