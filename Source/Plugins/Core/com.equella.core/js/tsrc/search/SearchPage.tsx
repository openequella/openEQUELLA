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
import { defaultPagedSearchResult, searchItems } from "./SearchModule";
import * as OEQ from "@openequella/rest-api-client";
import SearchResult from "./components/SearchResult";
import { generateFromError } from "../api/errors";
import {
  getSearchSettingsFromServer,
  SearchSettings,
  SortOrder,
} from "../settings/Search/SearchSettingsModule";
import { makeStyles } from "@material-ui/core/styles";

const useStyles = makeStyles({
  cardAction: {
    display: "flex",
    justifyContent: "center",
  },
});

const SearchPage = ({ updateTemplate }: TemplateUpdateProps) => {
  const classes = useStyles();
  const searchStrings = languageStrings.searchpage;

  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [sortOrder, setSortOrder] = useState<SortOrder>(SortOrder.RANK);
  const [pagedSearchResult, setPagedSearchResult] = useState<
    OEQ.Common.PagedResult<OEQ.Search.SearchResultItem>
  >(defaultPagedSearchResult);

  /**
   * Construct a standard search criteria.
   */
  const standardParams: OEQ.Search.SearchParams = {
    start: currentPage * rowsPerPage,
    length: rowsPerPage,
    status: [OEQ.Common.ItemStatus.LIVE, OEQ.Common.ItemStatus.REVIEW],
    order: sortOrder,
  };
  /**
   * Update the page title and retrieve Search settings.
   */
  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(searchStrings.title)(tp),
    }));

    getSearchSettingsFromServer().then((settings: SearchSettings) => {
      setSortOrder(settings.defaultSearchSort);
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
      search(standardParams);
    }
  }, [rowsPerPage, currentPage, sortOrder]);

  const handleError = (error: Error) => {
    updateTemplate(templateError(generateFromError(error)));
  };

  /**
   * Search items with specified search criteria.
   * @param params Search criteria
   */
  const search = (params?: OEQ.Search.SearchParams): void => {
    searchItems(params)
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

          <CardActions className={classes.cardAction}>
            <TablePagination
              component="div"
              count={pagedSearchResult.available}
              page={currentPage}
              onChangePage={(_, page: number) => setCurrentPage(page)}
              rowsPerPageOptions={[10, 25, 50]}
              labelRowsPerPage={searchStrings.pagination.itemsPerPage}
              rowsPerPage={rowsPerPage}
              onChangeRowsPerPage={(event) => {
                setRowsPerPage(parseInt(event.target.value));
                setCurrentPage(0);
              }}
            />
          </CardActions>
        </Card>
      </Grid>
    </Grid>
  );
};

export default SearchPage;
