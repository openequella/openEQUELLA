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

    def getBaseEntities(offset: Int, max: Int): List[BE] =
      res.getEntityService
        .query(new EnumerateOptions(q, offset, max, system, if (includeDisabled) null else false))
        .asScala
        .toList

    def getPrivilegeMap(entities: List[BE]): Map[BE, java.util.Map[String, java.lang.Boolean]] =
      LegacyGuice.aclManager
        .getPrivilegesForObjects(allReqPriv.asJavaCollection, entities.asJava)
        .asScala
        .toMap

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

    @tailrec
    def collectMore(
        len: Int,
        offset: Int,
        vec: Vector[(BE, Boolean, Set[String])]): (Int, Vector[(BE, Boolean, Set[String])]) = {
      val baseEntities = getBaseEntities(offset, MaxEntities)
      val privilegeMap = getPrivilegeMap(baseEntities)
      if (len <= 0 || baseEntities.isEmpty) {
        (offset, vec)
      } else {
        object ExtraPrivs {
          def unapply(be: BE): Option[(BE, Set[String])] = privilegeMap.get(be).map { p =>
            (be, allReqPriv & p.asScala.keySet)
          }
        }
        val withPriv = baseEntities
          .collect {
            case ExtraPrivs(be, privs) if privs.count(privilege) > 0 =>
              (be, full && privs.exists(forFull), privs)
          }
          .take(len)

        // When there are no entities available due do ACL, next offset is the sum of
        // current offset and the size of the entity list.
        // When available entities are found, next offset is the sum of current offset,
        // index of the last available entity in the list and 1.
        val nextOffset = withPriv.lastOption match {
          case Some(lastEntity) => offset + baseEntities.indexOf(lastEntity._1) + 1
          case None             => offset + baseEntities.size
        }

        collectMore(len - withPriv.size, nextOffset, vec ++ withPriv)
      }

    }
    def addPrivs(privs: Set[String], b: BEB): BEB = {
      b.setReadonly(new BaseEntityReadOnly(privs.asJavaCollection))
      b
    }

    val (nextOffset, results) = collectMore(_length, firstOffset, Vector.empty)
    val pb                    = new PagingBean[BEB]
    pb.setStart(firstOffset)
    pb.setLength(results.length)
    pb.setAvailable(available)
    // Say length is 10, then the recursive call of collectMore will try to get entities until
    // it gets 10 entities. But if it can't, that means no more entities are available. So
    // do not return resumption token.
    if (results.length == _length)
      pb.setResumptionToken(s"${nextOffset}:${_length}")
    pb.setResults(results.map {
      case (be, canFull, privs) => addPrivs(privs, res.serialize(be, null, canFull))
    }.asJava)
    pb
  }
}
