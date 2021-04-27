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
  Divider,
  FormControlLabel,
  IconButton,
  InputBase,
  Paper,
  Switch,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import SearchIcon from "@material-ui/icons/Search";
import * as React from "react";
import { useCallback, useEffect, useReducer, useState } from "react";
import { languageStrings } from "../../util/langstrings";

const useStyles = makeStyles({
  root: {
    display: "flex",
    alignItems: "center",
  },
  input: {
    flex: "auto",
  },
  divider: {
    height: 28,
    margin: "4px 12px",
  },
});

export interface SearchBarProps {
  /** Current value for the search field. */
  query: string;

  /** Current value for the wildcard mode toggle. */
  wildcardMode: boolean;

  /**
   * Callback fired when the user stops typing (debounced for 500 milliseconds).
   * @param query The string to search.
   */
  onQueryChange: (query: string) => void;

  /**
   * Callback fired when the user changes the wildcardMode.
   * @param wildcardMode the new value for Wildcard Mode
   */
  onWildcardModeChange: (wildcardMode: boolean) => void;

  /** Called when search button clicked. */
  doSearch: () => void;
}

const searchStrings = languageStrings.searchpage;

type State =
  | { status: "init"; query: string }
  | { status: "searching"; query: string }
  | { status: "done" };

type Action =
  | { type: "newSearch"; initialQuery: string }
  | { type: "debouncedSearch"; debouncedQuery: string }
  | { type: "waitForNewQuery" };

const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "newSearch":
      return { status: "init", query: action.initialQuery };
    case "debouncedSearch":
      return { status: "searching", query: action.debouncedQuery };
    case "waitForNewQuery":
      return { status: "done" };
    default:
      throw new TypeError("Unexpected action passed to reducer!");
  }
};

/**
 * Provide a input field to update the query of search criteria.
 * This component does not handle the API query itself,
 * that should be done in the parent component in response to the
 * onXyzChange callbacks and the doSearch.
 */
export default function SearchBar({
  query,
  wildcardMode,
  onQueryChange,
  onWildcardModeChange,
  doSearch,
}: SearchBarProps) {
  const classes = useStyles();
  const [state, dispatch] = useReducer(reducer, { status: "init", query });
  const [currentQuery, setCurrentQuery] = useState<string>(query);

  const search = useCallback(
    (query: string) => {
      dispatch({
        type: "debouncedSearch",
        debouncedQuery: query,
      });
    },
    [dispatch]
  );

  useEffect(() => {
    // When props query becomes falsy, it means a new search has been performed to clear SearchPageOptions.
    // So we dispatch the action of "newSearch".
    if (!query) {
      dispatch({
        type: "newSearch",
        initialQuery: query,
      });
    }
  }, [query]);

  useEffect(() => {
    if (state.status === "done") {
      return;
    }
    setCurrentQuery(state.query);
    if (state.status === "searching") {
      onQueryChange(state.query);
      dispatch({
        type: "waitForNewQuery",
      });
    }
  }, [state, dispatch, onQueryChange]);

  const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Escape" && currentQuery) {
      // iff there is a current query, clear it out and trigger a search
      search("");
    }
  };

  const handleOnChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    search(event.target.value);
  };

  return (
    <Paper className={classes.root}>
      <IconButton onClick={doSearch} aria-label={searchStrings.title}>
        <SearchIcon />
      </IconButton>
      <InputBase
        id="searchBar"
        className={classes.input}
        onKeyDown={handleKeyDown}
        onChange={handleOnChange}
        value={currentQuery}
        placeholder={
          wildcardMode
            ? searchStrings.wildcardSearchEnabledPlaceholder
            : searchStrings.wildcardSearchDisabledPlaceholder
        }
      />
      <Divider className={classes.divider} orientation="vertical" />
      <FormControlLabel
        style={{ opacity: 0.6 }}
        label={searchStrings.wildcardSearch}
        control={
          <Switch
            id="wildcardSearch"
            onChange={(_, checked) => onWildcardModeChange(checked)}
            value={wildcardMode}
            checked={wildcardMode}
            name={searchStrings.wildcardSearch}
            size="small"
          />
        }
      />
    </Paper>
  );
}
