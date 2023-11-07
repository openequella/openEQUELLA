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

/**
  * Represents a topic in the hierarchy.
  *
  * @param compoundUuid The unique identifier for the topic.
  *                      For virtual topics, the compoundUuid consist with uuid and match text,
  *                     for example: `0a8bde97-66f8-4114-8c7c-365545ce00da:textA`.
  * @param matchingItemCount Count of items matching this topic (including key resources).
  * @param name Name of the topic.
  * @param shortDescription A brief description of the topic.
  * @param longDescription A detailed description of the topic.
  * @param showResults Flag to determine if results should be shown.
  * @param inheritFreetext Flag to determine if free text should be inherited.
  * @param hideSubtopicsWithNoResults Flag to hide subtopics when there are no results.
  * @param subHierarchyTopics A list of subtopics under this topic.
  */
case class HierarchyTopic(compoundUuid: String,
                          matchingItemCount: Int = 0,
                          name: Option[String],
                          shortDescription: Option[String],
                          longDescription: Option[String],
                          showResults: Boolean = false,
                          inheritFreetext: Boolean = false,
                          hideSubtopicsWithNoResults: Boolean = false,
                          subHierarchyTopics: List[HierarchyTopic])
