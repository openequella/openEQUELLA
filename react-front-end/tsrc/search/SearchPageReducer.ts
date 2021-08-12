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
import type { GallerySearchResultItem } from "../modules/GallerySearchModule";
import type { Classification } from "../modules/SearchFacetsModule";
import type { SearchPageOptions } from "./SearchPageHelper";

/**
 * The types of SearchResultItem that we support within an `OEQ.Search.SearchResult`.
 */
export type SearchPageSearchResult =
  | {
      from: "item-search";
      content: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>;
    }
  | {
      from: "gallery-search";
      content: OEQ.Search.SearchResult<GallerySearchResultItem>;
    };

export type Action =
  | { type: "init" }
  | { type: "search"; options: SearchPageOptions; scrollToTop: boolean }
  | {
      type: "search-complete";
      result: SearchPageSearchResult;
      classifications: Classification[];
    }
  | { type: "error"; cause: Error };

export type State =
  | { status: "initialising" }
  | {
      status: "searching";
      options: SearchPageOptions;
      previousResult?: SearchPageSearchResult;
      previousClassifications?: Classification[];
      scrollToTop: boolean;
    }
  | {
      status: "success";
      result: SearchPageSearchResult;
      classifications: Classification[];
    }
  | { status: "failure"; cause: Error };

export const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "init":
      return { status: "initialising" };
    case "search":
      const prevResults =
        state.status === "success"
          ? {
              previousResult: state.result,
              previousClassifications: state.classifications,
            }
          : {};
      return {
        status: "searching",
        options: action.options,
        scrollToTop: action.scrollToTop,
        ...prevResults,
      };
    case "search-complete":
      return {
        status: "success",
        result: action.result,
        classifications: action.classifications,
      };
    case "error":
      return { status: "failure", cause: action.cause };
    default:
      throw new TypeError("Unexpected action passed to reducer!");
  }
};
