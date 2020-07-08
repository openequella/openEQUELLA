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
import * as React from "react";
import { useEffect, useRef, useState } from "react";
import {
  templateDefaults,
  templateError,
  TemplateUpdateProps,
} from "../mainui/Template";
import { languageStrings } from "../util/langstrings";
import {
  Card,
  CardActions,
  CardContent,
  CardHeader,
  Grid,
  List,
  ListItem,
  TablePagination,
  Typography,
  CircularProgress,
  makeStyles,
} from "@material-ui/core";
import {
  defaultPagedSearchResult,
  defaultSearchOptions,
  searchItems,
  SearchOptions,
} from "./SearchModule";
import SearchBar from "../search/components/SearchBar";
import * as OEQ from "@openequella/rest-api-client";
import SearchResult from "./components/SearchResult";
import { generateFromError } from "../api/errors";
import {
  getSearchSettingsFromServer,
  SearchSettings,
  SortOrder,
} from "../settings/Search/SearchSettingsModule";
import SearchOrderSelect from "./components/SearchOrderSelect";

const useStyles = makeStyles({
  transparentList: {
    opacity: 0.2,
  },
  centralSpinner: {
    top: "50%",
    position: "fixed",
  },
});

const SearchPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const searchStrings = languageStrings.searchpage;
  const classes = useStyles();

  const [searchOptions, setSearchOptions] = useState<SearchOptions>(
    defaultSearchOptions
  );
  const [pagedSearchResult, setPagedSearchResult] = useState<
    OEQ.Common.PagedResult<OEQ.Search.SearchResultItem>
  >(defaultPagedSearchResult);
  const [showSpinner, setShowSpinner] = useState<boolean>(false);

  /**
   * Update the page title and retrieve Search settings.
   */
  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(searchStrings.title)(tp),
    }));

    // Show spinner before calling API to retrieve Search settings.
    setShowSpinner(true);
    getSearchSettingsFromServer().then((settings: SearchSettings) => {
      handleSortOrderChanged(settings.defaultSearchSort);
    });
  }, []);

  /**
   * Trigger a search when state values change, but skip the initial values.
   */
  const isInitialSearch = useRef(true);
  useEffect(() => {
    if (isInitialSearch.current) {
      isInitialSearch.current = false;
    } else {
      search();
    }
  }, [searchOptions]);

  const handleError = (error: Error) => {
    updateTemplate(templateError(generateFromError(error)));
  };

  /**
   * Search items with specified search criteria and show a spinner when the search is in progress.
   */
  const search = (): void => {
    setShowSpinner(true);
    searchItems(searchOptions)
      .then((items: OEQ.Common.PagedResult<OEQ.Search.SearchResultItem>) =>
        setPagedSearchResult(items)
      )
      .catch(handleError)
      .finally(() => setShowSpinner(false));
  };

  /**
   * A SearchResult that represents one of the search result items.
   */
  const searchResults = pagedSearchResult.results.map(
    (item: OEQ.Search.SearchResultItem) => (
      <SearchResult {...item} key={item.uuid} />
    )
  );

  const handleSortOrderChanged = (order: SortOrder) =>
    setSearchOptions({ ...searchOptions, sortOrder: order });

  const handleQueryChanged = (query: string) =>
    setSearchOptions({ ...searchOptions, query: query, currentPage: 0 });

  /**
   * A list that consists of search result items. Lower the list's opacity when spinner displays.
   */
  const searchResultList = (
    <List className={showSpinner ? classes.transparentList : ""}>
      {searchResults.length === 0 && !showSpinner ? (
        <ListItem key={searchStrings.noResultsFound} divider>
          <Typography>{searchStrings.noResultsFound}</Typography>
        </ListItem>
      ) : (
        searchResults
      )}
    </List>
  );

  return (
    <Grid container direction="column" spacing={2}>
      <Grid item xs={9}>
        <Card>
          <CardContent>
            <SearchBar onChange={handleQueryChanged} />
          </CardContent>
        </Card>
      </Grid>
      <Grid item xs={9}>
        <Card>
          <CardHeader
            title={searchStrings.subtitle}
            action={
              <SearchOrderSelect
                value={searchOptions.sortOrder}
                onChange={handleSortOrderChanged}
              />
            }
          />
          {/*Add an inline style to make the spinner displays at the Card's horizontal center.*/}
          <CardContent style={{ textAlign: "center" }}>
            {showSpinner && (
              <CircularProgress
                variant="indeterminate"
                className={
                  pagedSearchResult.results.length > 0
                    ? classes.centralSpinner
                    : ""
                }
              />
            )}
            {searchResultList}
          </CardContent>

          <CardActions>
            <Grid container justify="center">
              <Grid item>
                <TablePagination
                  component="div"
                  count={pagedSearchResult.available}
                  page={searchOptions.currentPage}
                  onChangePage={(_, page: number) =>
                    setSearchOptions({ ...searchOptions, currentPage: page })
                  }
                  rowsPerPageOptions={[10, 25, 50]}
                  labelRowsPerPage={searchStrings.pagination.itemsPerPage}
                  rowsPerPage={searchOptions.rowsPerPage}
                  onChangeRowsPerPage={(event) =>
                    setSearchOptions({
                      ...searchOptions,
                      currentPage: 0,
                      rowsPerPage: parseInt(event.target.value),
                    })
                  }
                />
              </Grid>
            </Grid>
          </CardActions>
        </Card>
      </Grid>
    </Grid>
  );
};

export default SearchPage;
