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

/**
 * This module matches oEQ REST endpoint '/browsehierarchy2' and provides functions to browse
 * all the hierarchy topics or an individual topic.
 */

import * as t from 'io-ts';
import { GET } from './AxiosInstance';

import type { SearchResultItemRaw } from './Search';
import { validate } from './Utils';
import {
  HierarchyTopicCodec,
  HierarchyTopicSummaryCodec,
  KeyResourceCodec,
} from './gen/BrowseHierarchy';

const BROWSE_HIERARCHY_ROOT_PATH = '/browsehierarchy2';

/**
 * Provides summary of a topic, including the number of matching items and summaries of child topics.
 */
export interface HierarchyTopicSummary {
  /**
   * The unique identifier for the topic. For virtual topics, it consists of uuid and match text,
   * e.g., '0a8bde97-66f8-4114-8c7c-365545ce00da:textA'.
   */
  compoundUuid: string;
  /**
   * Count of items matching this topic.
   */
  matchingItemCount: number;
  /**
   * Name of the topic.
   */
  name?: string;
  /**
   * A brief description of the topic.
   */
  shortDescription?: string;
  /**
   * A detailed description of the topic.
   */
  longDescription?: string;
  /**
   * The name of the subtopic section, shown above the sub topic section.
   */
  subTopicSectionName?: string;
  /**
   * The name of the search result section, shown above the results section.
   */
  searchResultSectionName?: string;
  /**
   * Flag to determine if results should be shown.
   */
  showResults: boolean;
  /**
   * Flag to hide subtopics when there are no results.
   */
  hideSubtopicsWithNoResults: boolean;
  /**
   * Indicates whether this topic contains sub topics.
   */
  hasSubTopic: boolean;
}

/**
 * Contains basic topic info to represent a parent topic.
 */
export interface ParentTopic {
  /**
   * The compound uuid of the topic.
   */
  compoundUuid: string;
  /**
   * The name of the topic.
   */
  name?: string;
}

/**
 * Based on HierarchyTopicSummary, provides more information about parent topics and key resources.
 *
 * @typeParam T Type for Key resource which must be SearchResultItem or SearchResultItemRaw.
 */
export interface HierarchyTopic<T> {
  /**
   * Basic information of the topic.
   */
  summary: HierarchyTopicSummary;
  /**
   * Basic information of all the parent topics.
   */
  parents: ParentTopic[];
  /**
   * Summary information of all the child topics.
   */
  children: HierarchyTopicSummary[];
  /**
   * The key resources of the given topic (include dynamic key resources).
   */
  keyResources: T[];
}

/**
 * Key resource type returned by the API.
 */
export interface KeyResource {
  item: SearchResultItemRaw;
  isLatest: boolean;
}

/**
 * Retrieve summaries of all the root Hierarchy topics for the current institution.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 */
export const browseRootHierarchies = (
  apiBasePath: string
): Promise<HierarchyTopicSummary[]> =>
  GET<HierarchyTopicSummary[]>(
    apiBasePath + BROWSE_HIERARCHY_ROOT_PATH,
    validate(t.array(HierarchyTopicSummaryCodec))
  );

/**
 * Retrieve summaries of all the sub Hierarchy topics for the given topic.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param compoundUuid Topic compound UUID.
 */
export const browseSubHierarchies = (
  apiBasePath: string,
  compoundUuid: string
): Promise<HierarchyTopicSummary[]> =>
  GET<HierarchyTopicSummary[]>(
    apiBasePath + BROWSE_HIERARCHY_ROOT_PATH + `/${compoundUuid}`,
    validate(t.array(HierarchyTopicSummaryCodec))
  );

/**
 * Retrieve a Hierarchy topic details for a given topic compound UUID.
 * This compound UUID MUST include compound UUIDs of all the virtual parent topic, seperated by comma.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param compoundUuid Topic compound UUID.
 */
export const browseHierarchyDetails = (
  apiBasePath: string,
  compoundUuid: string
): Promise<HierarchyTopic<KeyResource>> =>
  GET<HierarchyTopic<KeyResource>>(
    apiBasePath + BROWSE_HIERARCHY_ROOT_PATH + `/details/${compoundUuid}`,
    validate(HierarchyTopicCodec(KeyResourceCodec))
  );

/**
 * Retrieve the compound UUIDs of hierarchies which have the key resource specified by the supplied Item UUID and version.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param itemUuid UUID of the item.
 * @param itemVersion Version of the item.
 */
export const getHierarchyIdsWithKeyResource = (
  apiBasePath: string,
  itemUuid: string,
  itemVersion: number
): Promise<string[]> =>
  GET<string[]>(
    `${apiBasePath}${BROWSE_HIERARCHY_ROOT_PATH}/key-resource/${itemUuid}/${itemVersion}`,
    validate(t.array(t.string))
  );
