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
import { absurd, pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { Location } from "history";
import { nonDeletedStatuses } from "../modules/SearchModule";
import { simpleMatch } from "../util/match";

export type MyResourcesType =
  | "Published"
  | "Drafts"
  | "Scrapbook"
  | "Moderation queue"
  | "Archive"
  | "All resources";

/**
 * Return a list of Item status that match the given MyResources type.
 *
 * @param resourceType MyResources type that may represent one or multiple Item statuses.
 */
export const myResourcesTypeToItemStatus = (
  resourceType: MyResourcesType
): OEQ.Common.ItemStatus[] => {
  switch (resourceType) {
    case "Published":
      return ["LIVE", "REVIEW"];
    case "Drafts":
      return ["DRAFT"];
    case "Scrapbook":
      return ["PERSONAL"];
    case "Moderation queue":
      return ["MODERATING", "REJECTED", "REVIEW"];
    case "Archive":
      return ["ARCHIVED"];
    case "All resources":
      return nonDeletedStatuses;
    default:
      return absurd(resourceType);
  }
};

export const getMyResourcesTypeFromLegacyQueryParam = (
  location: Location
): MyResourcesType | undefined =>
  pipe(
    location.search,
    O.fromNullable,
    O.chain((search) =>
      O.fromNullable(new URLSearchParams(search).get("type"))
    ),
    O.map(
      simpleMatch<MyResourcesType>({
        published: () => "Published",
        draft: () => "Drafts",
        scrapbook: () => "Scrapbook",
        modqueue: () => "Moderation queue",
        archived: () => "Archive",
        all: () => "All resources",
        _: (resourcesType) => {
          throw new TypeError(
            `Unknown Legacy My resources type [${resourcesType}]`
          );
        },
      })
    ),
    O.toUndefined
  );

/**
 * Return the default sort order for My resources page.
 * todo: Return "Submitted" for Moderation queue when working on OEQ-1343.
 */
export const defaultSortOrder = (_: MyResourcesType): OEQ.Search.SortOrder =>
  "datemodified";
