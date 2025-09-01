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
import FavoriteIcon from "@mui/icons-material/Favorite";
import { ReactNode } from "react";
import * as React from "react";
import { TooltipIconButton } from "../components/TooltipIconButton";
import { RenderFunc } from "../myresources/MyResourcesPageHelper";
import GallerySearchResult from "../search/components/GallerySearchResult";
import SearchResult, {
  defaultActionButtonProps,
} from "../search/components/SearchResult";
import type { SearchPageSearchResult } from "../search/SearchPageReducer";
import { languageStrings } from "../util/langstrings";
const { favouriteItem: favouriteItemStrings } = languageStrings.searchpage;

/**
 * Build a renderer function for favourite items.
 * The returned function renders a SearchResult for each item and injects
 * a "Remove from favourites" action button which invokes the provided callback.
 *
 * @param onRemoveFavouriteItem - Callback invoked when the remove action is clicked.
 * @returns A RenderFunc that takes an item and highlight terms and returns a React node.
 */
const buildFavouriteItemRenderer =
  (onRemoveFavouriteItem: (bookmarkId: number) => void): RenderFunc =>
  (item: OEQ.Search.SearchResultItem, highlight: string[]) => {
    const { uuid, version, bookmarkId } = item;
    return (
      <SearchResult
        key={`${uuid}/${version}`}
        item={item}
        highlights={highlight}
        customActionButtons={[
          <TooltipIconButton
            title={favouriteItemStrings.remove}
            onClick={() => bookmarkId && onRemoveFavouriteItem(bookmarkId)}
            size="small"
          >
            <FavoriteIcon />
          </TooltipIconButton>,
        ]}
        actionButtonConfig={{
          ...defaultActionButtonProps,
          showAddToFavourite: false,
        }}
      />
    );
  };

/**
 * Render the favourites item search results using either gallery or list layout.
 *
 * @param searchResult - The search result to render.
 * @param onRemoveFromFavourites - Handler called when a user clicks 'Remove from favourites' button.
 * @returns React nodes representing the rendered search results.
 */
export const renderFavouriteItemsResult = (
  searchResult: SearchPageSearchResult,
  onRemoveFromFavourites: (bookmarkId: number) => void,
): ReactNode => {
  const renderFavouriteItem = buildFavouriteItemRenderer(
    onRemoveFromFavourites,
  );
  return searchResult.from === "gallery-search" ? (
    <GallerySearchResult items={searchResult.content.results} />
  ) : (
    searchResult.content.results.map((item) =>
      renderFavouriteItem(item, searchResult.content.highlight),
    )
  );
};
