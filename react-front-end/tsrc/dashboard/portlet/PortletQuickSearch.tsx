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
import {
  Alert,
  Button,
  CircularProgress,
  debounce,
  Grid,
  TextField,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { not } from "fp-ts/Predicate";
import * as S from "fp-ts/string";
import * as T from "fp-ts/Task";
import * as TO from "fp-ts/TaskOption";
import * as React from "react";
import { useHistory } from "react-router";
import { routes } from "../../mainui/routes";
import { defaultSearchOptions, searchItems } from "../../modules/SearchModule";
import { getSearchSettingsFromServer } from "../../modules/SearchSettingsModule";
import { languageStrings } from "../../util/langstrings";
import { simpleMatch } from "../../util/match";
import { DraggablePortlet } from "../components/DraggablePortlet";
import type { PortletBasicProps } from "./PortletHelper";
import { PortletSearchResultList } from "../components/PortletSearchResultList";

const strings = {
  ...languageStrings.dashboard.portlets.quickSearch,
  actionShowAll: languageStrings.common.action.showAll,
};

const defaultSortOrder: OEQ.Search.SortOrder = "rank";

type SearchState =
  | { state: "initial" }
  | { state: "searching" }
  | {
      state: "success";
      results: OEQ.Search.SearchResultItem[];
    }
  | { state: "no results" };

export interface PortletQuickSearchProps extends PortletBasicProps {
  /** Optional search provider - primarily for testing. */
  searchProvider?: typeof searchItems;
  /** Optional provider for fetching search settings - primarily for testing. */
  searchSettingsProvider?: typeof getSearchSettingsFromServer;
}

/**
 * Portlet component that provides a quick search UI.
 */
export const PortletQuickSearch = ({
  cfg,
  position,
  searchProvider = searchItems,
  searchSettingsProvider = getSearchSettingsFromServer,
}: PortletQuickSearchProps) => {
  const history = useHistory();

  const [loadingState, setLoadingState] = React.useState<
    "loading" | "loaded" | "failed"
  >("loading");
  const [sortOrder, setSortOrder] =
    React.useState<OEQ.Search.SortOrder>(defaultSortOrder);
  const [query, setQuery] = React.useState<string>("");
  const [result, setResult] = React.useState<SearchState>({ state: "initial" });

  const search = React.useCallback(
    (query: string) => {
      setResult({ state: "searching" });
      return searchProvider({
        ...defaultSearchOptions,
        query,
        sortOrder,
        rowsPerPage: 5,
      });
    },
    [searchProvider, sortOrder],
  );

  const debouncedSearch = React.useMemo(
    () =>
      debounce((query: string) => {
        const processSearchResult = (
          res: OEQ.Search.SearchResult<OEQ.Search.SearchResultItem>,
        ): SearchState =>
          pipe(
            res.results,
            O.fromPredicate(A.isEmpty),
            O.match<OEQ.Search.SearchResultItem[], SearchState>(
              () => ({ state: "success", results: res.results }),
              () => ({ state: "no results" }),
            ),
          );

        pipe(
          TO.tryCatch(() => search(query)),
          TO.match(
            () => ({ state: "no results" }) as SearchState,
            processSearchResult,
          ),
          T.map(setResult),
        )();
      }, 500),
    [search],
  );

  React.useEffect(() => {
    pipe(
      TO.tryCatch(searchSettingsProvider),
      TO.match(
        () => setLoadingState("failed"),
        ({ defaultSearchSort: serverSortOrder }) => {
          setSortOrder(serverSortOrder ?? defaultSortOrder);
          setLoadingState("loaded");
        },
      ),
    )();
  }, [searchSettingsProvider]);

  React.useEffect(() => {
    pipe(
      query,
      O.fromPredicate(not(S.isEmpty)),
      O.match(
        () => setResult({ state: "initial" }),
        (q) => debouncedSearch(q),
      ),
    );

    return () => {
      // Cancel debounce on unmount or when debouncedSearch changes
      debouncedSearch.clear();
    };
  }, [query, debouncedSearch]);

  const handleQueryChange = (e: React.ChangeEvent<HTMLInputElement>) =>
    setQuery(e.target.value);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) =>
    pipe(
      e.key,
      simpleMatch({
        Enter: goToSearchPage,
        Escape: () => setQuery(""),
        _: () => {
          // No action for other keys
        },
      }),
    );

  const goToSearchPage = () =>
    history.push(routes.SearchPage.quickSearch(query));

  const searchResults = () => {
    switch (result.state) {
      case "initial":
        return null;
      case "searching":
        return (
          <div>
            <CircularProgress
              variant="indeterminate"
              aria-label={strings.searching}
            />
          </div>
        );
      case "no results":
        return <Alert severity="info">{strings.noResults}</Alert>;
      case "success":
        return <PortletSearchResultList results={result.results} />;
    }
  };

  const portletContent = pipe(
    loadingState,
    simpleMatch({
      failed: () => (
        <Alert severity="error">{strings.failedToInitialise}</Alert>
      ),
      loaded: () => (
        <Grid container direction="column" spacing={2}>
          <Grid>
            <TextField
              label={strings.queryField}
              value={query}
              onChange={handleQueryChange}
              onKeyDown={handleKeyDown}
              variant="outlined"
              size="small"
              fullWidth
            />
          </Grid>
          <Grid>{searchResults()}</Grid>
          <Grid display="flex" justifyContent="center">
            <Button variant="outlined" onClick={goToSearchPage}>
              {strings.actionShowAll}
            </Button>
          </Grid>
        </Grid>
      ),
      // In theory this state shouldn't be seen because we show a loading state in the PortletItem
      _: () => <div>Loading...</div>,
    }),
  );

  return (
    <DraggablePortlet
      portlet={cfg}
      isLoading={loadingState === "loading"}
      position={position}
    >
      {portletContent}
    </DraggablePortlet>
  );
};
