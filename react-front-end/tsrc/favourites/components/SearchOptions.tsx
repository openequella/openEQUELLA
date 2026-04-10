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

import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { Box, ListItem, Typography } from "@mui/material";
import { pipe } from "fp-ts/function";
import { JSX, useState } from "react";
import * as React from "react";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { languageStrings } from "../../util/langstrings";
import * as A from "fp-ts/Array";
import { FavouriteSearchOptionsSummary } from "./FavouritesSearchHelper";
import SearchOptionsChips from "./SearchOptionsChips";
import SearchOptionsChipsWithLabel from "./SearchOptionsChipsWithLabel";
import * as O from "fp-ts/Option";

const {
  searchCriteria: searchOptionsLabel,
  showMoreSearchCriteria: showMoreSearchOptionsLabel,
  hideMoreSearchCriteria: hideMoreSearchOptionsLabel,
  searchCriteriaLabels: searchOptionsLabelsStrings,
} = languageStrings.favourites.favouritesSearch;
const {
  query: queryLabel,
  collection: collectionLabel,
  hierarchy: hierarchyLabel,
  advancedSearch: advancedSearchLabel,
  lastModifiedDateRange: lastModifiedDateRangeLabel,
  owner: ownerLabel,
  mimeTypes: mimeTypesLabel,
  classification: classificationLabel,
} = searchOptionsLabelsStrings;

export interface SearchOptionsProps {
  /**
   * The search options summary to be displayed.
   */
  searchOptionsSummary?: FavouriteSearchOptionsSummary;
}

type SearchOptionsWithLabel = [string, string | string[] | undefined][];

// Helper function to render the search options using the provided renderer function.
const renderOptions = (
  options: SearchOptionsWithLabel,
  renderer: (label: string, opts: string | string[]) => JSX.Element,
) =>
  pipe(
    options,
    A.filterMap(([label, options]) =>
      pipe(
        O.fromNullable(options),
        O.map((opts) => renderer(label, opts)),
      ),
    ),
  );

/**
 * A component that displays the search options used in the {@link FavouritesSearch}.
 */
const SearchOptions = ({ searchOptionsSummary }: SearchOptionsProps) => {
  if (searchOptionsSummary === undefined) {
    return undefined;
  }

  // Since the element of the tooltip will be moved to a new place once users click the icon,
  // the open state of the tooltip has to be manually controlled. otherwise, the tooltip will not be
  // closed properly.
  const [openSearchOptionTooltip, setOpenSearchOptionTooltip] = useState(false);

  const [showAllSearchOptions, setShowAllSearchOptions] = useState(false);

  const {
    query,
    collections,
    advancedSearch,
    hierarchy,
    classifications,
    lastModifiedDateRange,
    mimeTypes,
    owner,
  } = searchOptionsSummary;

  // Options shown by default.
  const baseOptions: SearchOptionsWithLabel = [
    [advancedSearchLabel, advancedSearch],
    [collectionLabel, collections],
    [hierarchyLabel, hierarchy],
    [queryLabel, query],
  ];

  const additionalOptions: SearchOptionsWithLabel = [
    [classificationLabel, classifications],
    [lastModifiedDateRangeLabel, lastModifiedDateRange],
    [mimeTypesLabel, mimeTypes],
    [ownerLabel, owner],
  ];

  const hasAdditionalOptions = pipe(
    additionalOptions,
    A.some(([_, v]) => v !== undefined),
  );

  // Options shown when users click the expand icon.
  const allOptions: SearchOptionsWithLabel = [
    ...baseOptions,
    ...additionalOptions,
  ];

  const baseOptionsChips = () =>
    renderOptions(baseOptions, (label, opts) => (
      <SearchOptionsChips key={label} options={opts} prefix={label} />
    ));

  const allOptionsChipsWithLabels = () =>
    renderOptions(allOptions, (label, opts) => (
      <SearchOptionsChipsWithLabel key={label} label={label} options={opts} />
    ));

  return (
    <>
      <ListItem sx={{ paddingLeft: 0 }}>
        <Typography
          sx={{
            // Make sure the label can wrap properly if there are too many chips to fit in one line.
            overflowWrap: "break-word",
          }}
        >
          {searchOptionsLabel}&nbsp;
        </Typography>

        {showAllSearchOptions ? undefined : (
          // Make chips responsive wrap to new line if necessary.
          <Box sx={{ flexWrap: "wrap" }}>{baseOptionsChips()}</Box>
        )}

        {hasAdditionalOptions && (
          <TooltipIconButton
            open={openSearchOptionTooltip}
            onClose={() => setOpenSearchOptionTooltip(false)}
            onOpen={() => setOpenSearchOptionTooltip(true)}
            title={
              showAllSearchOptions
                ? hideMoreSearchOptionsLabel
                : showMoreSearchOptionsLabel
            }
            onClick={() => {
              setOpenSearchOptionTooltip(false);
              setShowAllSearchOptions(!showAllSearchOptions);
            }}
            size="small"
          >
            {showAllSearchOptions ? <ExpandLessIcon /> : <ExpandMoreIcon />}
          </TooltipIconButton>
        )}
      </ListItem>

      {showAllSearchOptions ? allOptionsChipsWithLabels() : undefined}
    </>
  );
};

export default SearchOptions;
