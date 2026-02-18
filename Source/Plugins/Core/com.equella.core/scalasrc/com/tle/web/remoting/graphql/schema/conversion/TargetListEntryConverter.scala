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

package com.tle.web.remoting.graphql.schema.conversion

import com.tle.common.security.{TargetList, TargetListEntry}

import scala.jdk.CollectionConverters._

/** Converter for transforming GraphQL access control entries to the security [[TargetList]]
  * structure.
  *
  * Target lists define access control rules specifying which privileges are granted or denied to
  * specific users or groups.
  */
object TargetListEntryConverter {

  /** Converts a list of GraphQL target list entries to a [[TargetList]].
    *
    * @param from
    *   the GraphQL access control entries
    * @return
    *   a new [[TargetList]] containing the converted entries
    */
  def toTargetList(
      from: List[com.tle.web.remoting.graphql.schema.types.TargetListEntry]
  ): TargetList =
    new TargetList(from.map(toTargetListEntry).asJava)

  private def toTargetListEntry(
      from: com.tle.web.remoting.graphql.schema.types.TargetListEntry
  ): TargetListEntry = {
    val to = new TargetListEntry()
    to.setGranted(from.granted)
    to.setOverride(from.overridden)
    to.setPrivilege(from.privilege)
    to.setWho(from.who)
    to.setPostfix(from.postfix)

    to
  }
}
