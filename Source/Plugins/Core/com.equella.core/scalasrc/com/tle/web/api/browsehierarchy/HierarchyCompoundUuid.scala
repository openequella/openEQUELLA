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

package com.tle.web.api.browsehierarchy

import com.tle.common.URLUtils
import com.tle.web.api.browsehierarchy.HierarchyCompoundUuid.base64Encode

import java.nio.charset.StandardCharsets
import java.util.Base64

/**
  * A case class to represent a hierarchy compound UUID.
  *
  * @param uuid The pure UUID part of the hierarchy.
  * @param name The virtual name (plain text format) of the hierarchy. None for normal hierarchy.
  * @param parentCompoundUuidList A list of HierarchyCompoundUuid for the given topic's virtual ancestors (all virtual parents) .
  *                              None for normal hierarchy or there is no virtual parent.
  */
case class HierarchyCompoundUuid(
    uuid: String,
    name: Option[String],
    parentCompoundUuidList: Option[List[HierarchyCompoundUuid]] = None
) {
  // Combine uuid and virtual name into a compound UUID, which the name part will encoded in base64 format or legacy application/x-www-form-urlencoded format.
  private def buildSingleCompoundUuid(id: String,
                                      virtualName: Option[String],
                                      isLegacy: Boolean): String =
    virtualName
      .map(if (isLegacy) URLUtils.basicUrlEncode else base64Encode)
      .map(n => s"$id:$n")
      .getOrElse(id)

  /**
    * Return the string representation of HierarchyCompoundUuid based on topic UUID,
    * name and the parent compound UUID map.
    *
    * @param inLegacyFormat Whether to return string representation in legacy format.
    *
    * Example:
    *{{{
    * uuid = 46249813-019d-4d14-b772-2a8ca0120c99
    * name = D, David
    * parentCompoundUuidMap = Some(Map("886aa61d-f8df-4e82-8984-c487849f80ff" -> "A James"))
    *
    * inLegacyFormat = false:
    * Output:
    * "46249813-019d-4d14-b772-2a8ca0120c99:RCwgRGF2aWQ=,886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw=="
    * inLegacyFormat = true:
    * Output:
    * "46249813-019d-4d14-b772-2a8ca0120c99:D%2C+David,886aa61d-f8df-4e82-8984-c487849f80ff:A+James"
    * }}}
    */
  def buildString(inLegacyFormat: Boolean = false): String = {
    val mainCompoundUuid = buildSingleCompoundUuid(uuid, name, inLegacyFormat)

    parentCompoundUuidList
      .map(
        _.foldLeft(mainCompoundUuid) {
          case (compoundUuidString, nextCompoundUuid) =>
            s"$compoundUuidString,${nextCompoundUuid.buildString(inLegacyFormat)}"
        }
      )
      .getOrElse(mainCompoundUuid)
  }

  /**
    * Return a List of HierarchyCompoundUuid for all virtual hierarchies.
    */
  def getAllVirtualHierarchyList: List[HierarchyCompoundUuid] =
    parentCompoundUuidList.map(this :: _).getOrElse(List(this)).filter {
      case HierarchyCompoundUuid(_, name, _) => name.isDefined
    }

  /**
    * Return a map for all virtual hierarchies, where the key is UUID and value is name.
    */
  def getAllVirtualHierarchyMap: Map[String, String] =
    getAllVirtualHierarchyList.collect {
      case HierarchyCompoundUuid(id, Some(name), _) => (id, name)
    }.toMap

}

/**
  * This class provides methods to build HierarchyCompoundUUid instance from both legacy compound UUID and new compound UUID.
  *
  * Background:
  *
  * A compound UUID can have 3 different forms:
  *  - Normal hierarchy UUID: "886aa61d-f8df-4e82-8984-c487849f80ff"
  *  - Virtual hierarchy compound UUID: "886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw=="
  *  - Sub virtual hierarchy compound UUID: "46249813-019d-4d14-b772-2a8ca0120c99:SG9iYXJ0,886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw=="
  *
  * Note:
  * `QSBKYW1lcw==` is the base 64 format of `A James`.
  * `SG9iYXJ0` is the base 64 format of `Hobart`.
  *
  * Since we are using the character comma to separate the compound UUID such as `UUID: topic1, UUID: parent1`,
  * If any virtual topic name contains a comma, it will break the workflow.
  *
  * Thus in our the new code, it will encode the name in `base64` format and return it to frontend.
  * For example, if a topic virtual name is `D, David`:
  * - Virtual hierarchy compound UUID presents in the new UI: "886aa61d-f8df-4e82-8984-c487849f80ff:RCwgRGF2aWQ="
  *
  * But the legacy code has has more complex logic to handle this issue.
  * It will double encode the virtual topic name,
  * it first encoded it in `application/x-www-form-urlencoded` format,
  * and then encode it again in `Percent-Encoding` format and return to frontend.
  * And it stores the virtual topic name in `application/x-www-form-urlencoded` format in DB.
  * Note: the difference between `application/x-www-form-urlencoded` format and `Percent-Encoding` format is the way to treat the space character.
  *
  * For example, if a topic virtual name is `D, David`:
  * - Virtual hierarchy compound UUID stored in DB: "886aa61d-f8df-4e82-8984-c487849f80ff:D%2C+David"
  * - Virtual hierarchy compound UUID presents in the old UI: "886aa61d-f8df-4e82-8984-c487849f80ff%3AD%252C%2BDavid"
  */
object HierarchyCompoundUuid {

  /**
    * Create a HierarchyCompoundUuid instance based on the given compound UUID.
    * Since name encode method is different between legacy and new UI, it will decode the name based on the given flag.
    *
    * New encode method: base64
    * Legacy encode method: application/x-www-form-urlencoded
    */
  def apply(compoundUuid: String, inLegacyFormat: Boolean): HierarchyCompoundUuid = {
    val (uuid, name) = getUuidAndName(compoundUuid, inLegacyFormat);
    val parentMap    = buildParentMap(compoundUuid, inLegacyFormat);

    new HierarchyCompoundUuid(uuid, name, Option(parentMap));
  }

  /**
    * Create a HierarchyCompoundUuid instance based on the given new format compound UUID.
    */
  def apply(compoundUuid: String): HierarchyCompoundUuid =
    apply(compoundUuid, inLegacyFormat = false)

  // Encodes a plain string into a base64 string.
  private def base64Encode(originalString: String): String =
    Base64.getEncoder.encodeToString(originalString.getBytes(StandardCharsets.UTF_8))

  // Decodes a base64 string into a plain string.
  private def base64Decode(base64String: String): String = {
    val decoded = Base64.getDecoder.decode(base64String)
    new String(decoded, StandardCharsets.UTF_8)
  }

  /**
    * Given a compound UUID, return a tuple where the first value is UUID and the second value is virtual topic name.
    *
    * @param compoundUuid A compound UUID.
    * @param inLegacyFormat Use legacy decode method to decode the name.
    */
  private def getUuidAndName(compoundUuid: String,
                             inLegacyFormat: Boolean = false): (String, Option[String]) =
    compoundUuid.split(",", 2).head.split(":", 2) match {
      case Array(uuid, name) =>
        (uuid,
         Option(name).map(
           if (inLegacyFormat) URLUtils.basicUrlDecode
           else base64Decode
         ))
      case Array(uuid) => (uuid, None)
    }

  // Build a HierarchyCompoundUuid list for the given topic's virtual ancestors (all virtual parents) .
  private def buildParentMap(compoundUuid: String,
                             inLegacyFormat: Boolean = false): List[HierarchyCompoundUuid] =
    compoundUuid
      .split(",")
      .tail
      .map(getUuidAndName(_, inLegacyFormat))
      .map {
        case (uuid, name) => HierarchyCompoundUuid(uuid, name)
      }
      .toList
}
