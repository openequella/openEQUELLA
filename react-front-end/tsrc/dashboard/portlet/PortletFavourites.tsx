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
import { Alert, Button, Grid, Tab, Tabs } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as T from "fp-ts/Task";
import * as TO from "fp-ts/TaskOption";
import * as React from "react";
import { useContext } from "react";
import { Link } from "react-router-dom";
import { AppContext } from "../../mainui/App";
import { routes } from "../../mainui/routes";
import { PortletPosition } from "../../modules/DashboardModule";
import {
  FavouritesType,
  searchFavouriteItems,
  searchFavouriteSearches,
} from "../../modules/FavouriteModule";
import { SearchOptions } from "../../modules/SearchModule";
import { languageStrings } from "../../util/langstrings";
import { simpleMatch } from "../../util/match";
import { DraggablePortlet } from "../components/DraggablePortlet";
import { FavouriteItemsTab } from "./FavouriteItemsTab";

const strings = {
  ...languageStrings.dashboard.portlets.favourites,
  actionShowAll: languageStrings.common.action.showAll,
};

type ResourcesProvider = (
  user: OEQ.LegacyContent.CurrentUserDetails,
) => Promise<OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>>;
type SearchesProvider = () => Promise<
  OEQ.Search.SearchResult<OEQ.Favourite.FavouriteSearch>
>;

export interface PortletFavouritesProps {
  /** The portlet configuration */
  cfg: OEQ.Dashboard.BasicPortlet;
  /** The actual position of the portlet in the page which is used for drag and drop operations. */
  position: PortletPosition;
  /** Optional provider for favourite resources - primarily for testing. */
  favouriteResourcesProvider?: ResourcesProvider;
  /** Optional provider for favourite searches - primarily for testing. */
  favouriteSearchesProvider?: SearchesProvider;
}

const favouriteSearchOptions: SearchOptions = {
  currentPage: 0,
  // The requirement is that we show a max of 5 items/searches in the portlet.
  rowsPerPage: 5,
  sortOrder: "added_at",
  rawMode: true,
};

const defaultResourcesProvider: ResourcesProvider = (user) =>
  searchFavouriteItems(favouriteSearchOptions, user);
const defaultSearchesProvider: SearchesProvider = () =>
  searchFavouriteSearches(favouriteSearchOptions);

/**
 * Portlet component that displays the user's favourite resources and searches.
 */
export const PortletFavourites = ({
  cfg,
  position,
  favouriteResourcesProvider = defaultResourcesProvider,
  favouriteSearchesProvider = defaultSearchesProvider,
}: PortletFavouritesProps): React.JSX.Element => {
  const { currentUser } = useContext(AppContext);

  const [isLoading, setIsLoading] = React.useState(true);
  const [activeTab, setActiveTab] = React.useState(0);
  const [favouriteResources, setFavouriteResources] = React.useState<
    OEQ.Search.SearchResultItem[]
  >([]);
  const [favouriteSearches, setFavouriteSearches] = React.useState<
    OEQ.Favourite.FavouriteSearch[]
  >([]);

  React.useEffect(() => {
    const fetchResourcesTask = pipe(
      currentUser,
      TO.fromNullable,
      TO.chain((user: OEQ.LegacyContent.CurrentUserDetails) =>
        TO.tryCatch(() => favouriteResourcesProvider(user)),
      ),
      TO.match(
        () => [],
        (items) => items.results,
      ),
      T.map(setFavouriteResources),
    );

    const fetchSearchesTask = pipe(
      TO.tryCatch(() => favouriteSearchesProvider()),
      TO.match(
        () => [],
        (searches) => searches.results,
      ),
      T.map(setFavouriteSearches),
    );

    pipe(
      [fetchResourcesTask, fetchSearchesTask],
      A.sequence(T.ApplicativePar),
      T.tapIO(() => () => setIsLoading(false)),
    )();
  }, [currentUser, favouriteResourcesProvider, favouriteSearchesProvider]);

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) =>
    setActiveTab(newValue);

  const tabContent = pipe(
    activeTab,
    simpleMatch({
      0: () => (
        <FavouriteItemsTab items={favouriteResources} type="resources" />
      ),
      1: () => <FavouriteItemsTab items={favouriteSearches} type="searches" />,
      _: () => <Alert severity="error">Unknown tab state!</Alert>,
    }),
  );

  const showAllPathForTab = pipe(
    activeTab,
    simpleMatch<FavouritesType>({
      0: () => "resources",
      1: () => "searches",
      _: () => "resources",
    }),
    routes.Favourites.to,
  );

  return (
    <DraggablePortlet portlet={cfg} isLoading={isLoading} position={position}>
      <Grid container direction="column" spacing={2}>
        <Grid>
          <Tabs value={activeTab} onChange={handleTabChange}>
            <Tab label={strings.resourcesTabName} />
            <Tab label={strings.searchesTabName} />
          </Tabs>
          {tabContent}
        </Grid>
        <Grid display="flex" justifyContent="center">
          <Button variant="outlined" component={Link} to={showAllPathForTab}>
            {strings.actionShowAll}
          </Button>
        </Grid>
      </Grid>
    </DraggablePortlet>
  );
};
