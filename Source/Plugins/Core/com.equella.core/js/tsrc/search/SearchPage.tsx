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
  Grid,
  IconButton,
  List,
  ListSubheader,
  TablePagination,
  TextField,
} from "@material-ui/core";
import SearchIcon from "@material-ui/icons/Search";
import {
  defaultPagedSearchResult,
  defaultSearchOptions,
  searchItems,
  SearchOptions,
} from "./SearchModule";
import * as OEQ from "@openequella/rest-api-client";
import SearchResult from "./components/SearchResult";
import { generateFromError } from "../api/errors";
import {
  getSearchSettingsFromServer,
  SearchSettings,
} from "../settings/Search/SearchSettingsModule";

const SearchPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const searchStrings = languageStrings.searchpage;

  const [searchOptions, setSearchOptions] = useState<SearchOptions>(
    defaultSearchOptions
  );
  const [pagedSearchResult, setPagedSearchResult] = useState<
    OEQ.Common.PagedResult<OEQ.Search.SearchResultItem>
  >(defaultPagedSearchResult);

  /**
   * Update the page title and retrieve Search settings.
   */
  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(searchStrings.title)(tp),
    }));

    getSearchSettingsFromServer().then((settings: SearchSettings) => {
      setSearchOptions({
        ...searchOptions,
        sortOrder: settings.defaultSearchSort,
      });
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
   * Search items with specified search criteria.
   */
  const search = (): void => {
    searchItems(searchOptions)
      .then((items: OEQ.Common.PagedResult<OEQ.Search.SearchResultItem>) =>
        setPagedSearchResult(items)
      )
      .catch((error: Error) => handleError(error));
  };

  /**
   * A SearchResult that represents one of the search result items.
   */
  const searchResults = pagedSearchResult.results.map(
    (item: OEQ.Search.SearchResultItem) => (
      <SearchResult {...item} key={item.uuid} />
    )
  );

  /**
   * A list that consists of search result items.
   */
  const searchResultList = (
    <List subheader={<ListSubheader>{searchStrings.subtitle}</ListSubheader>}>
      {searchResults}
    </List>
  );

  return (
    <Grid container direction="column" spacing={2}>
      <Grid item xs={9}>
        <Card>
          <CardContent>
            <IconButton>
              <SearchIcon fontSize="large" />
            </IconButton>
            <TextField />
          </CardContent>
        </Card>
      </Grid>

      <Grid item xs={9}>
        <Card>
          <CardContent>{searchResultList}</CardContent>

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
