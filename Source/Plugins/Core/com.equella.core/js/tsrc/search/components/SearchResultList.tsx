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
import * as OEQ from "@openequella/rest-api-client";
import {
  Card,
  CardActions,
  CardContent,
  CardHeader,
  CircularProgress,
  Grid,
  List,
  ListItem,
  Typography,
} from "@material-ui/core";
import SearchOrderSelect, { SearchOrderSelectProps } from "./SearchOrderSelect";
import { languageStrings } from "../../util/langstrings";
import SearchResult from "./SearchResult";
import { makeStyles } from "@material-ui/core/styles";
import { SearchPagination, SearchPaginationProps } from "./SearchPagination";

const useStyles = makeStyles({
  transparentList: {
    opacity: 0.2,
  },
  centralSpinner: {
    top: "50%",
    position: "fixed",
  },
});

/**
 * Props required by component SearchResultList.
 */
interface SearchResultListProps {
  /**
   * Items of a search result.
   */
  searchResultItems: OEQ.Search.SearchResultItem[];
  /**
   * True if showing the spinner is required.
   */
  showSpinner: boolean;
  /**
   * Props required by component SearchOrderSelect.
   */
  orderSelectProps: SearchOrderSelectProps;
  /**
   * Props required by component SearchPagination.
   */
  paginationProps: SearchPaginationProps;
}

/**
 * This component is basically a Card which includes a list of search results and a few search controls.
 * The sort order select is displayed in CardHeader.
 * The search results and spinner are displayed in CardContent.
 * The pagination is displayed in CardActions.
 */
export const SearchResultList = ({
  searchResultItems,
  showSpinner,
  orderSelectProps,
  paginationProps,
}: SearchResultListProps) => {
  const searchPageStrings = languageStrings.searchpage;
  const classes = useStyles();
  /**
   * A SearchResult that represents one of the search result items.
   */
  const searchResults = searchResultItems.map(
    (item: OEQ.Search.SearchResultItem) => (
      <SearchResult {...item} key={item.uuid} />
    )
  );

  /**
   * A list that consists of search result items. Lower the list's opacity when spinner displays.
   */
  const searchResultList = (
    <List className={showSpinner ? classes.transparentList : ""}>
      {searchResults.length === 0 && !showSpinner ? (
        <ListItem key={searchPageStrings.noResultsFound} divider>
          <Typography>{searchPageStrings.noResultsFound}</Typography>
        </ListItem>
      ) : (
        searchResults
      )}
    </List>
  );

  return (
    <Card>
      <CardHeader
        title={searchPageStrings.subtitle}
        action={
          <SearchOrderSelect
            value={orderSelectProps.value}
            onChange={orderSelectProps.onChange}
          />
        }
      />
      {/*Add an inline style to make the spinner display at the Card's horizontal center.*/}
      <CardContent style={{ textAlign: "center" }}>
        {showSpinner && (
          <CircularProgress
            variant="indeterminate"
            className={
              searchResultItems.length > 0 ? classes.centralSpinner : ""
            }
          />
        )}
        {searchResultList}
      </CardContent>
      <CardActions>
        <Grid container justify="center">
          <Grid item>
            <SearchPagination
              count={paginationProps.count}
              currentPage={paginationProps.currentPage}
              rowsPerPage={paginationProps.rowsPerPage}
              onPageChange={(page: number) =>
                paginationProps.onPageChange(page)
              }
              onRowsPerPageChange={(rowsPerPage: number) =>
                paginationProps.onRowsPerPageChange(rowsPerPage)
              }
            />
          </Grid>
        </Grid>
      </CardActions>
    </Card>
  );
};
