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
import SearchIcon from "@mui/icons-material/Search";
import TuneIcon from "@mui/icons-material/Tune";
import {
  Divider,
  FormControlLabel,
  IconButton,
  InputBase,
  Paper,
  Switch,
} from "@mui/material";
import { styled } from "@mui/material/styles";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import { not } from "fp-ts/Predicate";
import * as S from "fp-ts/string";
import * as React from "react";
import { forwardRef, useCallback, useEffect, useState } from "react";
import { TooltipIconButton } from "../../components/TooltipIconButton";
import { languageStrings } from "../../util/langstrings";

const PREFIX = "SearchBar";

const classes = {
  root: `${PREFIX}-root`,
  input: `${PREFIX}-input`,
  divider: `${PREFIX}-divider`,
};

const StyledPaper = styled(Paper)({
  [`&.${classes.root}`]: {
    display: "flex",
    alignItems: "center",
  },
  [`& .${classes.input}`]: {
    flex: "auto",
  },
  [`& .${classes.divider}`]: {
    height: 28,
    margin: "4px 12px",
  },
});

export interface SearchBarProps {
  /** Query supplied by the parent (typically `searchPageOptions.query`) */
  query: string;

  /**
   * Callback invoked with the current input value on every change.
   *
   * This component forwards changes immediately; it does NOT perform debounce.  The parent
   * (`SearchPageBody`) wraps its handler with a 500ms debounce so the actual search is throttled.
   */
  onQueryChange: (query: string) => void;

  /** Called when search button clicked. */
  doSearch: () => void;

  /**
   * Properties to control the display of the Advanced Search filter button.
   * If `undefined` the button is not displayed.
   */
  advancedSearchFilter?: {
    /** Called when the filter button is clicked */
    onClick: () => void;

    /** If true the button wil be highlighted by the Secondary colour. */
    accent: boolean;
  };

  /**
   * Properties to control the display and behavior of the wildcard toggle switch.
   * If `undefined` the toggle is not displayed.
   */
  wildcardSearch?: {
    /** Current value for the wildcard mode toggle. */
    wildcardMode: boolean;

    /**
     * Callback fired when the user changes the wildcardMode.
     * @param wildcardMode the new value for Wildcard Mode
     */
    onWildcardModeChange: (wildcardMode: boolean) => void;
  };
}

const searchStrings = languageStrings.searchpage;

/**
 * Derives the active search query state.
 * Preserves in-progress local typing to prevent stale debounced results from overwriting it.
 *
 * @param localQuery The current local query value.
 * @param propQuery The query supplied via props.
 * @return The derived query string.
 */
const deriveNextQuery = (localQuery: string, propQuery: string): string => {
  if (S.isEmpty(propQuery)) return S.empty;

  return pipe(
    localQuery,
    O.fromPredicate(not(S.isEmpty)),
    O.getOrElse(() => propQuery),
  );
};

/**
 * Provide a input field to update the query of search criteria.
 * This component does not handle the API query itself,
 * that should be done in the parent component in response to the
 * onXyzChange callbacks and the doSearch.
 */
const SearchBar = forwardRef(
  (
    {
      query,
      onQueryChange,
      doSearch,
      advancedSearchFilter,
      wildcardSearch,
    }: SearchBarProps,
    ref: React.ForwardedRef<HTMLDivElement>,
  ) => {
    const [localQuery, setLocalQuery] = useState(query);

    const search = useCallback(
      (query: string) => {
        setLocalQuery(query);
        onQueryChange(query);
      },
      [onQueryChange],
    );

    useEffect(() => {
      setLocalQuery((currentQuery) => deriveNextQuery(currentQuery, query));
    }, [query]);

    const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
      if (event.key === "Escape" && localQuery) {
        // if there is a current query, clear it out and trigger a search
        search("");
      }
    };

    const handleOnChange = (event: React.ChangeEvent<HTMLInputElement>) => {
      search(event.target.value);
    };

    return (
      <StyledPaper ref={ref} className={classes.root}>
        <IconButton
          onClick={doSearch}
          aria-label={searchStrings.title}
          size="large"
        >
          <SearchIcon />
        </IconButton>
        <InputBase
          id="searchBar"
          className={classes.input}
          onKeyDown={handleKeyDown}
          onChange={handleOnChange}
          value={localQuery}
          placeholder={searchStrings.searchBarPlaceholder}
          inputProps={{
            "aria-label": searchStrings.title,
          }}
        />
        {advancedSearchFilter && (
          <TooltipIconButton
            title={searchStrings.showAdvancedSearchFilter}
            onClick={advancedSearchFilter.onClick}
          >
            <TuneIcon
              color={advancedSearchFilter.accent ? "secondary" : "inherit"}
            />
          </TooltipIconButton>
        )}
        {wildcardSearch && (
          <>
            <Divider className={classes.divider} orientation="vertical" />
            <FormControlLabel
              style={{ opacity: 0.6 }}
              label={searchStrings.wildcardSearch}
              control={
                <Switch
                  id="wildcardSearch"
                  onChange={(event) =>
                    wildcardSearch.onWildcardModeChange(event.target.checked)
                  }
                  value={wildcardSearch.wildcardMode}
                  checked={wildcardSearch.wildcardMode}
                  name={searchStrings.wildcardSearch}
                  size="small"
                  color="secondary"
                />
              }
            />
          </>
        )}
      </StyledPaper>
    );
  },
);

export default SearchBar;
