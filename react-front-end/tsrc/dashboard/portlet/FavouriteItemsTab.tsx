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
import { Article, Search } from "@mui/icons-material";
import { List, ListItem, ListItemIcon, ListItemText } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import { absurd } from "fp-ts/function";
import * as React from "react";
import { Link } from "react-router-dom";
import { sprintf } from "sprintf-js";
import { routes } from "../../mainui/routes";
import { languageStrings } from "../../util/langstrings";
import { PortletSearchResultNoneFound } from "../components/PortletSearchResultNoneFound";

const { favourites: strings } = languageStrings.dashboard.portlets;

/** Props for `FavouriteItemsTab` indicating whether to show resources or searches, along with the items to show. */
type FavouriteItemsTabProps =
  | {
      type: "resources";
      items: OEQ.Search.SearchResultItem[];
    }
  | {
      type: "searches";
      items: OEQ.Favourite.FavouriteSearch[];
    };

/** Tab displaying either favourite resources or favourite searches. For use with `PortletFavourites`. */
export const FavouriteItemsTab = ({
  items,
  type,
}: FavouriteItemsTabProps): React.JSX.Element => {
  if (items.length === 0) {
    const tabName =
      type === "resources" ? strings.resourcesTabName : strings.searchesTabName;
    const noneFound = sprintf(strings.noneFound, tabName);

    return <PortletSearchResultNoneFound noneFoundMessage={noneFound} />;
  }

  const icon = () => (type === "resources" ? <Article /> : <Search />);

  const buildItem = (icon: React.JSX.Element, text: string, url: string) => (
    <ListItem key={text} component={Link} to={url}>
      <ListItemIcon>{icon}</ListItemIcon>
      <ListItemText primary={text} />
    </ListItem>
  );

  const listItem = (
    item: OEQ.Search.SearchResultItem | OEQ.Favourite.FavouriteSearch,
  ): React.JSX.Element => {
    switch (type) {
      case "resources": {
        const resource = item as OEQ.Search.SearchResultItem;
        return buildItem(
          icon(),
          resource.name ?? resource.uuid,
          routes.ViewItem.to(resource.uuid, resource.version),
        );
      }
      case "searches": {
        const search = item as OEQ.Favourite.FavouriteSearch;
        return buildItem(icon(), search.name, search.url);
      }
      default:
        return absurd(type);
    }
  };

  return <List>{items.map(listItem)}</List>;
};
