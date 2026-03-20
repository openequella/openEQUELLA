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

import com.tle.admin.graphql.conversion.TargetListEntryViewConverter.{
  fromTargetListEntry,
  toTargetListEntry
}
import com.tle.common.security.{TargetList, TargetListEntry}
import io.github.openequella.graphql.api.views.TargetListEntryView

import scala.jdk.CollectionConverters._

object TargetListConverter {
  def toTargetList(views: List[TargetListEntryView]): TargetList = {
    val entries = views.map(toTargetListEntry).asJava
    new TargetList(entries)
  }

  def fromTargetList(targetList: TargetList): List[TargetListEntryView] =
    Option(targetList)
      .map(tl => NullSafeList(tl.getEntries) convert fromTargetListEntry)
      .getOrElse(List.empty)
}

object TargetListEntryViewConverter {
  def toTargetListEntry(view: TargetListEntryView): TargetListEntry = {
    val entry = new TargetListEntry
    entry.setGranted(view.granted)
    entry.setOverride(view.overridden)
    entry.setPrivilege(view.privilege)
    entry.setWho(view.who)
    entry.setPostfix(view.postfix)

    entry
  }

  def fromTargetListEntry(entry: TargetListEntry): TargetListEntryView =
    TargetListEntryView(
      granted = entry.isGranted,
      overridden = entry.isOverride,
      privilege = entry.getPrivilege,
      who = entry.getWho,
      postfix = entry.getPostfix
    )
}
