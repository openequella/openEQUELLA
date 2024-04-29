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
import {
  HierarchyTopicCodec,
  HierarchyTopicSummaryCodec,
} from './gen/BrowseHierarchy';
import { SearchResultItemRawCodec } from './gen/Search';
import type { SearchResultItem, SearchResultItemRaw } from './Search';
import { convertDateFields, STANDARD_DATE_FIELDS, validate } from './Utils';

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
   * A list of subtopics under this topic.
   */
  subHierarchyTopics: HierarchyTopicSummary[];
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
   * The key resources of the given topic (include dynamic key resources).
   */
  keyResources: T[];
}

const processRawKeyResource = (data: HierarchyTopic<SearchResultItemRaw>) =>
  convertDateFields<HierarchyTopic<SearchResultItem>>(
    data,
    STANDARD_DATE_FIELDS
  );

/**
 * Retrieve summaries of all the Hierarchy topics for the current institution.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 */
export const browseHierarchies = (
  apiBasePath: string
): Promise<HierarchyTopicSummary[]> =>
  GET<HierarchyTopicSummary[]>(
    apiBasePath + BROWSE_HIERARCHY_ROOT_PATH,
    validate(t.array(HierarchyTopicSummaryCodec))
  );

/**
 * Retrieve a Hierarchy topic details for a given topic compound UUID.
 * This compound UUID MUST include compound UUIDs of all the virtual parent topic, seperated by comma.
 *
 * @param apiBasePath Base URI to the oEQ institution and API.
 * @param compoundUuid Topic compound UUID.
 */
export const browseHierarchy = (
  apiBasePath: string,
  compoundUuid: string
): Promise<HierarchyTopic<SearchResultItem>> =>
  GET<HierarchyTopic<SearchResultItemRaw>>(
    apiBasePath + BROWSE_HIERARCHY_ROOT_PATH + `/${compoundUuid}`,
    validate(HierarchyTopicCodec(SearchResultItemRawCodec))
  ).then(processRawKeyResource);
