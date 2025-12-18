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
    }
  | {
      from: "favourite-search";
      content: OEQ.Search.SearchResult<OEQ.Favourite.FavouriteSearch>;
    };

export type Action =
  | { type: "init"; options: SearchPageOptions }
  | {
      type: "search";
      // A unique token to identify this search request.
      requestToken: string;
      options: SearchPageOptions;
      updateClassifications: boolean;
      callback?: () => void;
    }
  | {
      type: "search-complete";
      // A unique token to identify which search request triggered this action.
      requestToken: string;
      result: SearchPageSearchResult;
      classifications: Classification[];
    }
  | { type: "error"; cause: Error };

export type State =
  | {
      status: "initialising";
      options: SearchPageOptions;
    }
  | {
      status: "searching";
      requestToken: string;
      options: SearchPageOptions;
      previousResult?: SearchPageSearchResult;
      previousClassifications?: Classification[];
      updateClassifications: boolean;
      callback?: () => void;
    }
  | {
      status: "success";
      options: SearchPageOptions;
      result: SearchPageSearchResult;
      classifications: Classification[];
    }
  | {
      status: "failure";
      options: SearchPageOptions;
      cause: Error;
    };

export const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "init":
      return { status: "initialising", options: action.options };
    case "search":
      return pipe(
        state.status === "success"
          ? {
              previousResult: state.result,
              previousClassifications: state.classifications,
            }
          : {},
        (prevResults) => ({
          status: "searching",
          requestToken: action.requestToken,
          options: action.options,
          callback: action.callback,
          updateClassifications: action.updateClassifications,
          ...prevResults,
        }),
      );
    case "search-complete":
      // Explicitly define the valid state transition using positive logic.
      // The requestToken in state represents the latest search request.
      // If token from action is different, ignore the stale action.
      if (
        state.status === "searching" &&
        state.requestToken === action.requestToken
      ) {
        return {
          status: "success",
          options: state.options,
          result: action.result,
          classifications: action.classifications,
        };
      }

      // Fallback for stale requests or invalid states.
      console.debug(
        `Ignoring stale search-complete action (token: ${action.requestToken}).`,
      );
      return state;
    case "error":
      return {
        status: "failure",
        options: state.options,
        cause: action.cause,
      };
    default:
      return absurd(action);
  }
};
