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

package com.tle.web.api.browsehierarchy.model

import com.tle.web.api.search.model.SearchResultItem

/** Provides summary of a topic, including the number of matching Items and summaries of child
  * topics.
  *
  * @param compoundUuid
  *   The unique identifier for the topic. For virtual topics, the compoundUuid consist with uuid
  *   and match text, for example: `0a8bde97-66f8-4114-8c7c-365545ce00da:textA`.
  * @param matchingItemCount
  *   Count of items matching this topic.
  * @param name
  *   Name of the topic.
  * @param shortDescription
  *   A brief description of the topic.
  * @param longDescription
  *   A detailed description of the topic.
  * @param subTopicSectionName
  *   The name of the subtopic section, shown above the sub topic section.
  * @param searchResultSectionName
  *   The name of the search result section, shown above the results section.
  * @param showResults
  *   Flag to determine if results should be shown.
  * @param hideSubtopicsWithNoResults
  *   Flag to hide subtopics when there are no results.
  * @param subHierarchyTopics
  *   A list of subtopics under this topic.
  */
case class HierarchyTopicSummary(
    compoundUuid: String,
    matchingItemCount: Int,
    name: Option[String],
    shortDescription: Option[String],
    longDescription: Option[String],
    subTopicSectionName: Option[String],
    searchResultSectionName: Option[String],
    showResults: Boolean,
    hideSubtopicsWithNoResults: Boolean,
    subHierarchyTopics: List[HierarchyTopicSummary]
)

/** Contains basic topic info to represent an parent topic.
  *
  * @param compoundUuid
  *   The string representation of HierarchyCompoundUuid class.
  * @param name
  *   The name of the topic.
  */
case class ParentTopic(compoundUuid: String, name: Option[String])

/** Represents a key resource in the hierarchy, including the referenced Item and a flag which
  * indicates if the key resource points to the latest version of that Item.
  */
case class KeyResource(
    item: SearchResultItem,
    isLatest: Boolean
)

/** Based on [[HierarchyTopicSummary]], provides more information about parent topics and key
  * resources.
  *
  * @param summary
  *   Basic information of the topic.
  * @param parents
  *   Basic information of all the parent topics.
  * @param keyResources
  *   All key resources of the given topic.
  */
case class HierarchyTopic(
    summary: HierarchyTopicSummary,
    parents: List[ParentTopic],
    keyResources: List[KeyResource]
)
