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
import * as NEA from "fp-ts/NonEmptyArray";
import * as O from "fp-ts/Option";
import { API_BASE_URL } from "../AppConfig";
import { routes } from "../mainui/routes";

/**
 * Represents a sub-category for a 'My Resources' type, specifically for 'Moderation queue'.
 */
interface MyResourcesSubCategory extends OEQ.MyResource.MyResourcesSubCategory {
  /** Contains the route for UI navigation. */
  to: string;
}

/**
 * Holds the details for an individual category of 'My Resources' (e.g. Draft, Published, Archived, etc.).
 */
export interface MyResourcesCategory
  extends Omit<OEQ.MyResource.MyResourcesCategory, "links" | "subSearches"> {
  /** Contains the route for UI navigation. */
  to: string;
  /**
   * An optional list of sub-categories, each also including a `to` route.
   * This is primarily for the 'Moderation queue'.
   */
  subCategories?: MyResourcesSubCategory[];
}

/**
 * Transforms the list of My Resources categories from the API into the data structure required by the UI.
 * This involves filtering out unwanted types (like 'all' or 'scrapbook' if disabled), adding navigation links (`to`) and excluding `links` attribute from main type .
 *
 * @param isScrapbookEnabled Whether the Scrapbook feature is enabled for the current user.
 */
const transformMyResourcesCategories =
  (isScrapbookEnabled: boolean) =>
  (types: OEQ.MyResource.MyResourcesCategory[]): MyResourcesCategory[] => {
    // Filter out the `all` category as it is used with `Show All` button, and 'scrapbook' category if it is not enabled for current user.
    const filterMyResourcesCategories = (
      t: OEQ.MyResource.MyResourcesCategory,
    ): boolean =>
      t.id !== "all" && (isScrapbookEnabled || t.id !== "scrapbook");

    const mapSubSearches = (
      parentName: OEQ.MyResource.MyResourcesCategoryName,
      subSearches?: OEQ.MyResource.MyResourcesSubCategory[],
    ) =>
      pipe(
        subSearches,
        O.fromPredicate(
          (
            ss,
          ): ss is NEA.NonEmptyArray<OEQ.MyResource.MyResourcesSubCategory> =>
            parentName === "Moderation queue" && !!ss && A.isNonEmpty(ss),
        ),
        O.map(
          NEA.map((ss: OEQ.MyResource.MyResourcesSubCategory) => ({
            ...ss,
            to: routes.MyResources.to(
              parentName,
              ss.id.toUpperCase() as OEQ.Common.ItemStatus,
            ),
          })),
        ),
        O.toUndefined,
      );

    // Converts a raw API search type into `MyResourcesCategory` by adding routing links and removing raw API links.
    const toMyResourcesCategory = ({
      links,
      subSearches,
      ...rest
    }: OEQ.MyResource.MyResourcesCategory): MyResourcesCategory => ({
      ...rest,
      to: routes.MyResources.to(rest.name),
      ...pipe(
        mapSubSearches(rest.name, subSearches),
        O.fromNullable,
        O.match(
          () => ({}),
          (subCategories) => ({ subCategories }),
        ),
      ),
    });

    return pipe(
      types,
      A.filter(filterMyResourcesCategories),
      A.map(toMyResourcesCategory),
    );
  };

/**
 * Retrieves the details of My Resources categories available to the current user.
 *
 * @param isScrapbookEnabled Whether the Scrapbook feature is enabled for the current user, used to filter the results.
 */
export const getMyResourceCategories = (
  isScrapbookEnabled: boolean,
): Promise<MyResourcesCategory[]> =>
  OEQ.MyResource.getMyResourceCategories(API_BASE_URL).then(
    transformMyResourcesCategories(isScrapbookEnabled),
  );
