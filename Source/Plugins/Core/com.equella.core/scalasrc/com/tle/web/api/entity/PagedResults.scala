/*
 * Copyright 2019 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

  def decodeOffsetStart(resumption: String): (Int, Int) = {
    Option(resumption).map(_.split(":").map(_.toInt)).collect {
      case Array(o, s) => (o, s)
    }.getOrElse((0, 0))
  }

  val MaxEntities = 200

  def pagedResults[BE <: BaseEntity, BEB <: BaseEntityBean]
  (res: AbstractBaseEntityResource[BE, _, BEB], q: String,
   _privilege: java.util.List[String], resumption: String, length: Int, full: Boolean, system: Boolean, includeDisabled: Boolean) : PagingBean[BEB] = {
    val (firstOffset, start) = decodeOffsetStart(resumption)

    val privilege = if (_privilege.isEmpty) Set("LIST_"+res.getPrivilegeType) else _privilege.asScala.toSet
    val forFull = Set("VIEW_"+res.getPrivilegeType, "EDIT_" + res.getPrivilegeType)

    val allReqPriv  = if (full) forFull ++ privilege else privilege

    @tailrec
    def collectMore(len: Int, offset: Int, tried: Int, vec: Vector[(BE, Boolean, Set[String])]): (Int, Vector[(BE, Boolean, Set[String])]) = {
      if (len <= 0 || tried >= MaxEntities) (offset, vec)
      else {
        val amountToTry = if (tried == 0) len * 2 else MaxEntities - tried
        val nextLot = res.getEntityService.query(new EnumerateOptions(q, offset, amountToTry, system, if (includeDisabled) null else false))
        val nextOffset = offset + nextLot.size()
        if (nextOffset == offset)
        {
          (offset, vec)
        }
        else
        {
          val privMap = LegacyGuice.aclManager.getPrivilegesForObjects(allReqPriv.asJavaCollection, nextLot).asScala
          object ExtraPrivs
          {
            def unapply(be: BE): Option[(BE, Set[String])] = privMap.get(be).map {
              p => (be, allReqPriv & p.asScala.keySet)
            }
          }
          val withPriv = nextLot.asScala.collect {
            case ExtraPrivs(be, privs) if privs.count(privilege) > 0 => (be, full && privs.exists(forFull), privs)
          }.take(len)
          collectMore(len - withPriv.size, nextOffset, tried + amountToTry, vec ++ withPriv)
        }
      }
    }
    def addPrivs(privs: Set[String], b: BEB): BEB = {
      b.setReadonly(new BaseEntityReadOnly(privs.asJavaCollection))
      b
    }

    val (nextOffset, results) = collectMore(length, firstOffset, 0, Vector.empty)
    val pb = new PagingBean[BEB]
    val actualLen = results.length
    pb.setStart(start)
    pb.setLength(actualLen)
    pb.setAvailable(res.getEntityService.countAll(new EnumerateOptions(q, 0, -1, system, if (includeDisabled) null else false)).toInt)
    if (actualLen == length) pb.setResumptionToken(s"$nextOffset:${start+actualLen}")
    pb.setResults(results.map { case (be,canFull,privs) => addPrivs(privs, res.serialize(be, null, canFull)) }.asJava)
    pb
  }
}
