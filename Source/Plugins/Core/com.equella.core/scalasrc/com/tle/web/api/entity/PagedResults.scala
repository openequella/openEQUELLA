/*
 * Copyright 2017 Apereo
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

import java.util.Collections

import com.google.common.collect.ImmutableCollection
import com.tle.beans.entity.BaseEntity
import com.tle.core.entity.EnumerateOptions
import com.tle.legacy.LegacyGuice
import com.tle.web.api.entity.resource.AbstractBaseEntityResource
import com.tle.web.api.interfaces.beans.{BaseEntityBean, PagingBean}

import scala.collection.JavaConverters._
import scala.annotation.tailrec

object PagedResults {

  def decodeOffsetStart(resumption: String): (Int, Int) = {
    Option(resumption).map(_.split(":").map(_.toInt)).collect {
      case Array(o, s) => (o, s)
    }.getOrElse((0, 0))
  }

  def pagedResults[BE <: BaseEntity, BEB <: BaseEntityBean]
  (res: AbstractBaseEntityResource[BE, _, BEB], q: String,
   _privilege: String, resumption: String, length: Int, full: Boolean, system: Boolean) : PagingBean[BEB] = {
    val (firstOffset, start) = decodeOffsetStart(resumption)

    val forFull = Set("VIEW_"+res.getPrivilegeType, "EDIT_" + res.getPrivilegeType)

    val privilege = Option(_privilege).getOrElse("LIST_"+res.getPrivilegeType)
    val allReqPriv = if (full) (forFull + privilege).asJavaCollection else Collections.singleton(privilege)

    @tailrec
    def collectMore(len: Int, offset: Int, vec: Vector[(BE, Boolean)]): (Int, Vector[(BE, Boolean)]) = {
      if (len <= 0) (offset, vec)
      else {
        val nextLot = res.getEntityService.query(new EnumerateOptions(q, offset, len, system, null))
        val nextOffset = offset + nextLot.size()
        if (nextOffset == offset)
        {
          (offset, vec)
        }
        else
        {
          val privMap = LegacyGuice.aclManager.getPrivilegesForObjects(allReqPriv, nextLot).asScala
          val withPriv = nextLot.asScala.collect {
            case be if privMap.get(be).exists(_.get(privilege)) =>
              (be, full &&
                privMap.get(be).exists(privs => forFull.exists(p => privs.asScala.getOrElse(p,
                  java.lang.Boolean.FALSE).booleanValue()))
              )
          }
          collectMore(len - withPriv.size, nextOffset, vec ++ withPriv)
        }
      }
    }
    val (nextOffset, results) = collectMore(length, firstOffset, Vector.empty)
    val pb = new PagingBean[BEB]
    val actualLen = results.length
    pb.setStart(start)
    pb.setLength(actualLen)
    pb.setAvailable(res.getEntityService.countAll(new EnumerateOptions(q, 0, -1, system, null)).toInt)
    pb.setResumptionToken(s"$nextOffset:${start+actualLen}")
    pb.setResults(results.map { case (be,canFull) => res.serialize(be, null, canFull) }.asJava)
    pb
  }
}
