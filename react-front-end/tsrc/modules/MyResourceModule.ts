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
import { flow, pipe } from "fp-ts/function";
import * as NEA from "fp-ts/NonEmptyArray";
import * as O from "fp-ts/Option";
import { API_BASE_URL, getBaseUrl } from "../AppConfig";
import { NEW_MY_RESOURCES_PATH, routes } from "../mainui/routes";
import { PARAM_MYRESOURCES_TYPE } from "../myresources/MyResourcesPageHelper";
import { generateQueryStringFromSearchPageOptions } from "../search/SearchPageHelper";
import { omit } from "../util/objects";

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

/** Predicate to filter out the 'all' category or 'scrapbook' if it is disabled. */
const shouldIncludeCategory =
  (isScrapbookEnabled: boolean) =>
  (category: OEQ.MyResource.MyResourcesCategory): boolean =>
    category.id !== "all" &&
    (isScrapbookEnabled || category.id !== "scrapbook");

/** Maps API sub-searches to UI sub-categories containing route links. */
const buildSubCategoryRoutes = (
  parentName: OEQ.MyResource.MyResourcesCategoryName,
  subCategories?: OEQ.MyResource.MyResourcesSubCategory[],
): MyResourcesSubCategory[] | undefined =>
  pipe(
    O.fromNullable(subCategories),
    O.chain(NEA.fromArray),
    O.map(
      NEA.map((subCategory) => ({
        ...subCategory,
        to: routes.MyResources.to(
          parentName,
          subCategory.id.toUpperCase() as OEQ.Common.ItemStatus,
        ),
      })),
    ),
    O.toUndefined,
  );

/** Converts a raw API MyResources category into the UI model by adding routing links. and excluding 'links'. */
const addRoutingInfo = (
  category: OEQ.MyResource.MyResourcesCategory,
): MyResourcesCategory => {
  const baseCategory = {
    ...category,
    to: routes.MyResources.to(category.name),
  };

  const subCategories = buildSubCategoryRoutes(
    category.name,
    category.subSearches,
  );

  const withSubCategories = subCategories
    ? { ...baseCategory, subCategories }
    : baseCategory;

  return omit(withSubCategories, ["links", "subSearches"]);
};

/**
 * Transforms the list of My Resources categories from the API into the data structure required by the UI.
 *
 * @param isScrapbookEnabled Whether the Scrapbook feature is enabled for the current user.
 */
const transformMyResourcesCategories = (isScrapbookEnabled: boolean) =>
  flow(
    A.filter(shouldIncludeCategory(isScrapbookEnabled)),
    A.map(addRoutingInfo),
  );

/**
 * Builds a URL value object for the My Resources page.
 * Encapsulates search parameter serialization to maintain consistency with the search API.
 */
export const buildMyResourceUrl = (
  myResourcesType: OEQ.MyResource.MyResourcesCategoryName,
  status?: OEQ.Common.ItemStatus,
): URL => {
  const url = new URL(NEW_MY_RESOURCES_PATH.slice(1), getBaseUrl());

  // Set the primary resource type
  url.searchParams.set(PARAM_MYRESOURCES_TYPE, myResourcesType);

  // If status is provided, merge the serialized search options into our URL
  if (status) {
    const searchOptionsQuery = generateQueryStringFromSearchPageOptions({
      status: [status],
    });
    const searchParams = new URLSearchParams(searchOptionsQuery);
    searchParams.forEach((value, key) => url.searchParams.set(key, value));
  }

  return url;
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
