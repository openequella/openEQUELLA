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
  Button,
  Card,
  CardActions,
  CardContent,
  CardHeader,
  CircularProgress,
  Grid,
  Hidden,
  List,
  ListItem,
  Tooltip,
  Typography,
} from "@material-ui/core";
import FavoriteBorderIcon from "@material-ui/icons/FavoriteBorder";
import FilterListIcon from "@material-ui/icons/FilterList";
import { makeStyles } from "@material-ui/core/styles";
import Share from "@material-ui/icons/Share";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { isSelectionSessionOpen } from "../../modules/LegacySelectionSessionModule";
import { languageStrings } from "../../util/langstrings";
import SearchOrderSelect, { SearchOrderSelectProps } from "./SearchOrderSelect";
import { SearchPagination, SearchPaginationProps } from "./SearchPagination";
import SearchResult from "./SearchResult";

const useStyles = makeStyles({
  transparentList: {
    opacity: 0.2,
  },
  centralSpinner: {
    top: "50%",
    position: "fixed",
  },
});

interface RefineSearchProps {
  /**
   * True when any search criteria has been set
   */
  isCriteriaSet: boolean;
  /**
   * Function fired to show Refine Search panel.
   */
  showRefinePanel: () => void;
}

/**
 * Props required by component SearchResultList.
 */
export interface SearchResultListProps {
  /**
   * Ideally a collection of `<SearchResult>` elements.
   */
  children?: React.ReactNode;
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
  /**
   * Fired when the New search button is clicked.
   */
  onClearSearchOptions: () => void;
  /**
   * Fired when the copy search button is clicked.
   */
  onCopySearchLink: () => void;
  /**
   * Fired when the save search button is clicked.
   */
  onSaveSearch: () => void;
  /**
   * Props for the Icon button that controls whether show Refine panel in small screens
   */
  refineSearchProps: RefineSearchProps;
}

const searchPageStrings = languageStrings.searchpage;

/**
 * This component is basically a Card which includes a list of search results and a few search controls.
 * The sort order select is displayed in CardHeader.
 * The search results and spinner are displayed in CardContent.
 * The pagination is displayed in CardActions.
 */
export const SearchResultList = ({
  children,
  showSpinner,
  orderSelectProps: { value, onChange: onOrderChange },
  paginationProps: {
    count,
    currentPage,
    rowsPerPage,
    onPageChange,
    onRowsPerPageChange,
  },
  refineSearchProps: { isCriteriaSet, showRefinePanel },
  onClearSearchOptions,
  onCopySearchLink,
  onSaveSearch,
}: SearchResultListProps) => {
  const classes = useStyles();
  const inSelectionSession: boolean = isSelectionSessionOpen();

  /**
   * A list that consists of search result items. Lower the list's opacity when spinner displays.
   */
  const searchResultList = (
    <List className={showSpinner ? classes.transparentList : ""}>
      {!children && !showSpinner ? (
        <ListItem key={searchPageStrings.noResultsFound} divider>
          <Typography>{searchPageStrings.noResultsFound}</Typography>
        </ListItem>
      ) : (
        children
      )}
    </List>
  );

  return (
    <Card>
      <CardHeader
        title={searchPageStrings.subtitle + ` (${count})`}
        action={
          <Grid container spacing={1} alignItems="center">
            <Grid item>
              <SearchOrderSelect value={value} onChange={onOrderChange} />
            </Grid>
            <Grid item>
              <Tooltip title={searchPageStrings.newSearchHelperText}>
                <Button variant="outlined" onClick={onClearSearchOptions}>
                  {searchPageStrings.newSearch}
                </Button>
              </Tooltip>
            </Grid>
            <Grid item>
              <TooltipIconButton
                title={searchPageStrings.favouriteSearch.title}
                onClick={onSaveSearch}
              >
                <FavoriteBorderIcon />
              </TooltipIconButton>
            </Grid>
            <Hidden mdUp>
              <Grid item>
                <TooltipIconButton
                  title={searchPageStrings.refineSearchPanel.title}
                  onClick={showRefinePanel}
                  color={isCriteriaSet ? "secondary" : "primary"}
                >
                  <FilterListIcon />
                </TooltipIconButton>
              </Grid>
            </Hidden>
            {!inSelectionSession && (
              <Grid item>
                <TooltipIconButton
                  title={searchPageStrings.shareSearchHelperText}
                  onClick={onCopySearchLink}
                >
                  <Share />
                </TooltipIconButton>
              </Grid>
            )}
          </Grid>
        }
      />
      {/*Add an inline style to make the spinner display at the Card's horizontal center.*/}
      <CardContent style={{ textAlign: "center" }}>
        {showSpinner && (
          <CircularProgress
            variant="indeterminate"
            className={children ? classes.centralSpinner : ""}
          />
        )}
        {searchResultList}
      </CardContent>
      <CardActions>
        <Grid container justify="center">
          <Grid item>
            <SearchPagination
              count={count}
              currentPage={currentPage}
              rowsPerPage={rowsPerPage}
              onPageChange={(page: number) => onPageChange(page)}
              onRowsPerPageChange={(rowsPerPage: number) =>
                onRowsPerPageChange(rowsPerPage)
              }
            />
          </Grid>
        </Grid>
      </CardActions>
    </Card>
  );
};

/**
 * Helper function for generating a collection of child `<SearchResult>` elements (for use inside
 * `<SearchResultList>`) from a collection of OEQ.Search.SearchResultItem[].
 *
 * @param items the search result items to map over
 * @param handleError function which will be called on error (e.g. comms errors)
 * @param highlights a list of highlight terms
 * @param getViewerDetails optional function to override retrieval of viewer details
 */
export const mapSearchResultItems = (
  items: OEQ.Search.SearchResultItem[],
  handleError: (error: Error) => void,
  highlights: string[],
  getViewerDetails?: (
    mimeType: string
  ) => Promise<OEQ.MimeType.MimeTypeViewerDetail>
): React.ReactNode[] =>
  items.map((item) => (
    <SearchResult
      key={`${item.uuid}/${item.version}`}
      item={item}
      handleError={handleError}
      highlights={highlights}
      getViewerDetails={getViewerDetails}
    />
  ));
