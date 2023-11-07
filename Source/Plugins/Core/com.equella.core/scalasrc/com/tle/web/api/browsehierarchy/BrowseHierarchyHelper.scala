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

import com.tle.beans.entity.LanguageBundle
import com.tle.beans.hierarchy.{HierarchyTopic => HierarchyTopicEntity}
import com.tle.common.interfaces.equella.BundleString
import com.tle.core.guice.Bind
import com.tle.core.hierarchy.HierarchyService
import com.tle.core.search.VirtualisableAndValue
import com.tle.web.api.browsehierarchy.model.HierarchyTopic

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

@Bind
@Singleton
class BrowseHierarchyHelper {
  private var hierarchyService: HierarchyService = _

  @Inject
  def this(hierarchyService: HierarchyService) = {
    this()
    this.hierarchyService = hierarchyService
  }

  /**
    * Get the localized string for the given bundle and
    * if virtual text is existing, replace the placeholder %s with matched virtual text.
    */
  private def buildVirtualTopicText(bundle: LanguageBundle,
                                    matchedVirtualText: Option[String]): Option[String] = {
    Option(BundleString.getString(bundle))
      .map(_.toString)
      .map(topicFullName => {
        matchedVirtualText
          .map(topic => topicFullName.replaceAll("%s", topic))
          .getOrElse(topicFullName)
      })
  }

  def buildHierarchyTopic(
      topicWrapper: VirtualisableAndValue[HierarchyTopicEntity]): HierarchyTopic = {
    val topic: HierarchyTopicEntity = topicWrapper.getVt
    val matchedVirtualText          = Option(topicWrapper.getVirtualisedValue)
    buildHierarchyTopic(topic, matchedVirtualText)
  }

  /**
    * Build an instance of HierarchyTopic based on the supplied entity and virtual topic name.
    */
  def buildHierarchyTopic(topic: HierarchyTopicEntity,
                          virtualTopicName: Option[String]): HierarchyTopic = {
    val uuid            = topic.getUuid
    val collectionUuids = hierarchyService.getCollectionUuids(topic)
    val compoundUuidMap = virtualTopicName.map(t => Map(uuid -> t))

    // Normal sub topics and sub virtual topics.
    val subTopics = hierarchyService
      .expandVirtualisedTopics(hierarchyService.getSubTopics(topic),
                               compoundUuidMap.orNull.asJava,
                               collectionUuids.orElse(null))
      .asScala
      .toList
      .map(buildHierarchyTopic)

    HierarchyTopic(
      virtualTopicName.map(uuid + ":" + _).getOrElse(uuid),
      hierarchyService.getMatchingItemCount(topic, virtualTopicName.orNull),
      buildVirtualTopicText(topic.getName, virtualTopicName),
      buildVirtualTopicText(topic.getShortDescription, virtualTopicName),
      buildVirtualTopicText(topic.getLongDescription, virtualTopicName),
      topic.isShowResults,
      topic.isInheritFreetext,
      topic.isHideSubtopicsWithNoResults,
      subTopics
    )
  }
}
