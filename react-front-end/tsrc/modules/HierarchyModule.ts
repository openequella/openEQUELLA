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
import * as OEQ from "@openequella/rest-api-client";
import { API_BASE_URL } from "../AppConfig";

/**
 * Get summaries of all the root hierarchy topics.
 */
export const getRootHierarchies = (): Promise<
  OEQ.BrowseHierarchy.HierarchyTopicSummary[]
> => OEQ.BrowseHierarchy.browseRootHierarchies(API_BASE_URL);

/**
 * Get summaries of all the sub hierarchy topics.
 */
export const getSubHierarchies = (
  compoundUuid: string,
): Promise<OEQ.BrowseHierarchy.HierarchyTopicSummary[]> =>
  OEQ.BrowseHierarchy.browseSubHierarchies(API_BASE_URL, compoundUuid);

/**
 * Get details of a hierarchy topic, including all the key resources and basic information of parent topics.
 *
 * @param compoundUuid Compound UUID of the hierarchy topic.
 */
export const getHierarchyDetails = (
  compoundUuid: string,
): Promise<
  OEQ.BrowseHierarchy.HierarchyTopic<OEQ.BrowseHierarchy.KeyResource>
> => OEQ.BrowseHierarchy.browseHierarchyDetails(API_BASE_URL, compoundUuid);

/**
 * Get hierarchy IDs which contains the given key resource.
 *
 * @param itemUuid The item UUID.
 * @param itemVersion The item version.
 */
export const getHierarchyIdsWithKeyResource = (
  itemUuid: string,
  itemVersion: number,
): Promise<string[]> =>
  OEQ.BrowseHierarchy.getHierarchyIdsWithKeyResource(
    API_BASE_URL,
    itemUuid,
    itemVersion,
  );

/**
 * Get all ACLs for the hierarchy topic.
 *
 * @param compoundUuid Topic compound UUID.
 */
export const getMyAcls = (
  compoundUuid: string,
): Promise<OEQ.Hierarchy.HierarchyTopicAcl> =>
  OEQ.Hierarchy.getMyAcls(API_BASE_URL, compoundUuid);

/**
 * Add a key resource to a hierarchy topic
 *
 * @param compoundUuid Topic compound UUID.
 * @param itemUuid The item UUID.
 * @param itemVersion The item version.
 */
export const addKeyResource = (
  compoundUuid: string,
  itemUuid: string,
  itemVersion: number,
): Promise<void> =>
  OEQ.Hierarchy.addKeyResource(
    API_BASE_URL,
    compoundUuid,
    itemUuid,
    itemVersion,
  );

/**
 * Delete a key resource to a hierarchy topic
 *
 * @param compoundUuid Topic compound UUID.
 * @param itemUuid The item UUID.
 * @param itemVersion The item version.
 */
export const deleteKeyResource = (
  compoundUuid: string,
  itemUuid: string,
  itemVersion: number,
): Promise<void> =>
  OEQ.Hierarchy.deleteKeyResource(
    API_BASE_URL,
    compoundUuid,
    itemUuid,
    itemVersion,
  );

export const defaultHierarchyAcl: OEQ.Hierarchy.HierarchyTopicAcl = {
  VIEW_HIERARCHY_TOPIC: false,
  EDIT_HIERARCHY_TOPIC: false,
  MODIFY_KEY_RESOURCE: false,
};
