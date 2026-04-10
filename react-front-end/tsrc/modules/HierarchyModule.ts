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
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as S from "fp-ts/string";
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

// Extract topic ID from the new UI hierarchy page path.
const getTopicIDFromPath = (path: string): string | undefined => {
  const newHierarchyPagePath = /(\/page\/hierarchy\/)(.+)/;
  const matches: string[] | null = path.match(newHierarchyPagePath);

  return matches?.pop();
};

// Extract topic ID from the old UI hierarchy page query parameter.
const getTopicIDFromQueryParam = (queryParam: string): string | undefined =>
  pipe(
    new URLSearchParams(queryParam).get("topic"),
    O.fromNullable,
    O.map((uuids) => uuids.split(",")),
    O.map(A.map(convertSingleLegacyIdToNewFormat)),
    O.map(A.intercalate(S.Monoid)(",")),
    O.toUndefined,
  );

// Converts a single legacy formatted compound UUID to new base64 format.
//
// Legacy format:
//   "46249813-019d-4d14-b772-2a8ca0120c99:D%2C+David"
// New format:
//   "46249813-019d-4d14-b772-2a8ca0120c99:RCwgRGF2aWQ="
const convertSingleLegacyIdToNewFormat = (legacyCompoundUuid: string) => {
  const [uuid, encodedNameMaybe] = legacyCompoundUuid.split(":");

  const decodeFormUrlEncodedSpaces = (name: string) => name.replace(/\+/g, " ");

  const toBase64 = (name: string) =>
    Buffer.from(name, "utf8").toString("base64");

  return pipe(
    O.fromNullable(encodedNameMaybe),
    // Replace special character '+' first since the legacy format uses application/x-www-form-urlencoded.
    O.map(decodeFormUrlEncodedSpaces),
    O.map(decodeURIComponent),
    O.map(toBase64),
    O.map((base64Name) => `${uuid}:${base64Name}`),
    O.getOrElse(() => legacyCompoundUuid),
  );
};

// Converts a single new base64 formatted compound UUID back to legacy URL-encoded format.
//
// New format:
//   "46249813-019d-4d14-b772-2a8ca0120c99:RCwgRGF2aWQ="
// Legacy format:
//   "46249813-019d-4d14-b772-2a8ca0120c99:D%2C+David"
const convertSingleNewIdToLegacyFormat = (compoundUuid: string) => {
  const [uuid, base64NameMaybe] = compoundUuid.split(":");

  const decodeBase64 = (base64: string) =>
    Buffer.from(base64, "base64").toString("utf8");

  // Legacy format expects application/x-www-form-urlencoded encoding:
  //   - spaces → '+'
  //   - special chars → percent-encoded (encodeURIComponent)
  const encodeFormUrl = (name: string) =>
    encodeURIComponent(name).replace(/%20/g, "+");

  return pipe(
    O.fromNullable(base64NameMaybe),
    O.map(decodeBase64),
    O.map(encodeFormUrl),
    O.map((legacyEncodedName) => `${uuid}:${legacyEncodedName}`),
    O.getOrElse(() => compoundUuid),
  );
};

/**
 * Convert new base64 formatted compound UUIDs to legacy application/x-www-form-urlencoded format.
 * To follow the legacy logic the final result will be encoded again, to handle the case if the virtual topic name also contains commas.
 *
 * @param compoundUuid The compound UUIDs in new format, separated by commas.
 */
export const convertNewTopicIdToLegacyFormat = (compoundUuid: string): string =>
  pipe(
    compoundUuid.split(","),
    A.map(convertSingleNewIdToLegacyFormat),
    A.intercalate(S.Monoid)(","),
    encodeURIComponent,
  );

/**
 * Extract topic ID from a given URL.
 *
 * @param url The URL object to extract the topic ID from.
 */
export const getTopicIdFromUrl = (url: URL): string | undefined =>
  getTopicIDFromPath(url.pathname) ?? getTopicIDFromQueryParam(url.search);

export const getTopicIDFromLocation = (): string | undefined => {
  const location = window.location;
  return (
    getTopicIDFromPath(location.pathname) ??
    getTopicIDFromQueryParam(location.search)
  );
};
