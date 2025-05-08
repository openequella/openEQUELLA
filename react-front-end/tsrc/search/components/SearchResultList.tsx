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
import FavoriteBorderIcon from "@mui/icons-material/FavoriteBorder";
import FilterListIcon from "@mui/icons-material/FilterList";
import Share from "@mui/icons-material/Share";
import {
  Button,
  Card,
  CardActions,
  CardContent,
  CardHeader,
  CircularProgress,
  Grid,
  List,
  ListItem,
  Tooltip,
  Typography,
  useMediaQuery,
} from "@mui/material";
import type { Theme } from "@mui/material/styles";
import { styled } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import clsx from "clsx";
import * as React from "react";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { isSelectionSessionOpen } from "../../modules/LegacySelectionSessionModule";
import { languageStrings } from "../../util/langstrings";
import {
  ExportSearchResultLink,
  ExportSearchResultLinkProps,
} from "./ExportSearchResultLink";
import SearchOrderSelect, { SearchOrderSelectProps } from "./SearchOrderSelect";
import { SearchPagination, SearchPaginationProps } from "./SearchPagination";
import SearchResult from "./SearchResult";

const PREFIX = "SearchResultList";

const classes = {
  transparentList: `${PREFIX}-transparentList`,
  centralSpinner: `${PREFIX}-centralSpinner`,
  textCentered: `${PREFIX}-textCentered`,
};

const StyledCard = styled(Card)({
  [`& .${classes.transparentList}`]: {
    opacity: 0.2,
  },
  [`& .${classes.centralSpinner}`]: {
    top: "50%",
    position: "fixed",
  },
  [`& .${classes.textCentered}`]: {
    textAlign: "center",
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
   * Title of the search result list. The default value is the `searchpage.subtitle` from languageStrings.
   */
  title?: string;
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
   * True to enable the Share search button.
   */
  useShareSearchButton?: boolean;
  /**
   * Props for the Icon button that controls whether show Refine panel in small screens
   */
  refineSearchProps: RefineSearchProps;
  /**
   * Props required by ExportSearchResultLink.
   */
  exportProps: {
    isExportPermitted: boolean;
    linkRef: React.RefObject<HTMLAnchorElement>;
    exportLinkProps: ExportSearchResultLinkProps;
  };
  /**
   * Additional components to be displayed in the CardHeader.
   */
  additionalHeaders?: React.JSX.Element[];
}

const searchPageStrings = languageStrings.searchpage;

/**
 * This component is basically a Card which includes a list of search results and a few search controls.
 * The sort order select is displayed in CardHeader.
 * The search results and spinner are displayed in CardContent.
 * The pagination is displayed in CardActions.
 */
export const SearchResultList = ({
  title,
  children,
  showSpinner,
  orderSelectProps,
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
  exportProps: { isExportPermitted, linkRef, exportLinkProps },
  useShareSearchButton = true,
  additionalHeaders,
}: SearchResultListProps) => {
  const inSelectionSession: boolean = isSelectionSessionOpen();
  const isMdDown = useMediaQuery<Theme>((theme) =>
    theme.breakpoints.down("md"),
  );

  /**
   * A list that consists of search result items. Lower the list's opacity when spinner displays.
   */
  const searchResultList = (
    <List
      className={showSpinner ? classes.transparentList : ""}
      data-testid="search-result-list"
    >
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
    <StyledCard>
      <CardHeader
        title={(title ?? searchPageStrings.subtitle) + ` (${count})`}
        action={
          <Grid container spacing={1} alignItems="center">
            <Grid>
              <SearchOrderSelect {...orderSelectProps} />
            </Grid>
            <Grid>
              <Tooltip title={searchPageStrings.newSearchHelperText}>
                <Button
                  variant="outlined"
                  onClick={onClearSearchOptions}
                  color="inherit"
                >
                  {searchPageStrings.newSearch}
                </Button>
              </Tooltip>
            </Grid>
            <Grid>
              <TooltipIconButton
                title={searchPageStrings.favouriteSearch.title}
                onClick={onSaveSearch}
              >
                <FavoriteBorderIcon />
              </TooltipIconButton>
            </Grid>
            {isExportPermitted && (
              <Grid>
                <ExportSearchResultLink {...exportLinkProps} ref={linkRef} />
              </Grid>
            )}
            {isMdDown && (
              <Grid>
                <TooltipIconButton
                  title={searchPageStrings.refineSearchPanel.title}
                  onClick={showRefinePanel}
                  color={isCriteriaSet ? "secondary" : "primary"}
                >
                  <FilterListIcon />
                </TooltipIconButton>
              </Grid>
            )}
            {!inSelectionSession && useShareSearchButton && (
              <Grid>
                <TooltipIconButton
                  title={searchPageStrings.shareSearchHelperText}
                  onClick={onCopySearchLink}
                >
                  <Share />
                </TooltipIconButton>
              </Grid>
            )}
            {additionalHeaders?.map((header) => (
              <Grid key={header.key}>{header}</Grid>
            ))}
          </Grid>
        }
      />
      <CardContent className={clsx(showSpinner && classes.textCentered)}>
        {showSpinner && (
          <CircularProgress
            aria-label={searchPageStrings.loading}
            variant="indeterminate"
            className={clsx(children && classes.centralSpinner)}
          />
        )}
        {searchResultList}
      </CardContent>
      <CardActions>
        <Grid container justifyContent="center" size="grow">
          <Grid>
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
    </StyledCard>
  );
};

/**
 * Helper function for generating a collection of child `<SearchResult>` elements (for use inside
 * `<SearchResultList>`) from a collection of OEQ.Search.SearchResultItem[].
 *
 * @param items the search result items to map over
 * @param highlights a list of highlight terms
 * @param getViewerDetails optional function to override retrieval of viewer details
 */
export const mapSearchResultItems = (
  items: OEQ.Search.SearchResultItem[],
  highlights: string[],
  getViewerDetails?: (
    mimeType: string,
  ) => Promise<OEQ.MimeType.MimeTypeViewerDetail>,
): React.ReactNode[] =>
  items.map((item) => (
    <SearchResult
      key={`${item.uuid}/${item.version}`}
      item={item}
      highlights={highlights}
      getViewerDetails={getViewerDetails}
    />
  ));
