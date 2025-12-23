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
import { API_BASE_URL } from "../AppConfig";
import { routes } from "../mainui/routes";

/**
 * A sub-search for 'My resources'.
 * It excludes `links` and includes a `to` property for navigation.
 */
interface MyResourceModeratingSubSearch
  extends OEQ.SearchMyResource.MyResourceModeratingSubSearch {
  to: string;
}

/**
 * A 'My resources' type structure.
 * It excludes `links` and includes a `to` property for navigation and its sub-searches also have the `to` property.
 */
export interface MyResourcesTypeData
  extends Omit<
    OEQ.SearchMyResource.MyResourceSearchType,
    "links" | "subSearches"
  > {
  to: string;
  subSearches?: MyResourceModeratingSubSearch[];
}

/**
 * Transforms the list of My Resources search types from the API into the data structure required by the UI.
 * This involves filtering out unwanted types (like 'all' or 'scrapbook' if disabled), adding navigation links (`to`) and excluding `links` attribute from main type .
 *
 * @param isScrapbookEnabled Whether the Scrapbook feature is enabled for the current user.
 */
export const transformMyResourcesTypes =
  (isScrapbookEnabled: boolean) =>
  (
    types: OEQ.SearchMyResource.MyResourceSearchType[],
  ): MyResourcesTypeData[] => {
    // Filter out the `all` type as it is used with `Show All` button, and 'scrapbook' if it is not enabled for current user.
    const filterMyResourcesTypes = (
      t: OEQ.SearchMyResource.MyResourceSearchType,
    ): boolean =>
      t.id !== "all" && (isScrapbookEnabled || t.id !== "scrapbook");

    // Converts a raw API search type into `MyResourcesTypeData` by adding routing links (`to`) for both the main type and any sub-searches.
    const toMyResourcesTypeData = ({
      links,
      subSearches,
      ...rest
    }: OEQ.SearchMyResource.MyResourceSearchType): MyResourcesTypeData => ({
      ...rest,
      to: routes.MyResources.to(rest.id),
      subSearches:
        rest.id === "modqueue" && subSearches
          ? subSearches.map((s) => ({
              ...s,
              to: routes.MyResources.to(rest.id, s.id),
            }))
          : undefined,
    });

    return pipe(
      types,
      A.filter(filterMyResourcesTypes),
      A.map(toMyResourcesTypeData),
    );
  };

/**
 * Fetches the list of 'My resources' search types from the API and returns the transforms data.
 *
 * @param isScrapbookEnabled Whether the Scrapbook feature is enabled for the current user, used to filter the results.
 * @returns A promise resolving to a list of transformed 'My resources' search types.
 */
export const getMyResourceSearchTypes = (
  isScrapbookEnabled: boolean,
): Promise<MyResourcesTypeData[]> =>
  OEQ.SearchMyResource.getMyResourceSearchTypes(API_BASE_URL).then(
    transformMyResourcesTypes(isScrapbookEnabled),
  );
