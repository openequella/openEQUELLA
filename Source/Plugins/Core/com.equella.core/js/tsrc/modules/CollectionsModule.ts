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
import { listEntities } from "./OEQHelpers";

/**
 * A simplified type of Collection used when only collection uuid and name are required.
 */
export interface Collection {
  /**
   * Collection's uuid.
   */
  uuid: string;
  /**
   * Collection's name.
   */
  name: string;
}

/**
 * Find all available Collections. On failure, an OEQ.Errors.ApiError will be thrown.
 *
 * @param requiredPrivileges Privileges required to access Collections.
 */
export const collectionListSummary = (
  requiredPrivileges?: string[]
): Promise<Collection[]> => {
  const getCollections = (resumptionToken: string) =>
    OEQ.Collection.listCollections(API_BASE_URL, {
      privilege: requiredPrivileges,
      resumption: resumptionToken,
    });
  return listEntities<OEQ.Common.BaseEntity>(getCollections).then(
    (results) => results
  );
};

/**
 * Find Collections by a list of ID.
 *
 * @param collectionUuids Collection UUIDs used to filter the list of all Collections.
 * @returns { Collection[] | undefined } An array of `Collection` instances matching the specified UUIDs or undefined if none could be found.
 */
export const findCollectionsByUuid = async (
  collectionUuids: string[]
): Promise<Collection[] | undefined> => {
  const collectionList = await collectionListSummary([
    OEQ.Acl.ACL_SEARCH_COLLECTION,
  ]);
  const filteredCollectionList = collectionList.filter((c) =>
    collectionUuids.includes(c.uuid)
  );
  return filteredCollectionList.length > 0 ? filteredCollectionList : undefined;
};
