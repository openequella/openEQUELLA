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
import { useCallback, useEffect, useReducer } from "react";
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

interface State {
  status: "init" | "queryUpdated" | "waiting";
  query: string;
}

type Action =
  | { type: "clearQuery" }
  | { type: "updateQuery"; query: string }
  | { type: "waitForNewQuery"; query: string };

const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "clearQuery":
      return { status: "init", query: "" };
    case "updateQuery":
      return { status: "queryUpdated", query: action.query };
    case "waitForNewQuery":
      return { status: "waiting", query: action.query };
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

  const search = useCallback(
    (query: string) => {
      dispatch({
        type: "updateQuery",
        query,
      });
    },
    [dispatch]
  );

  // The state query should be consistent with prop query. But there are two situations where they
  // are different.
  // One is when a new search has been performed to clear SearchPageOptions. In this case, we dispatch
  // the action of "clearQuery".
  // The other is when the page is rendered with previously selected search options where a query is
  // included. We dispatch the action of "waitForNewQuery" to update the state without triggering
  // an extra search.
  useEffect(() => {
    if (!query && state.query) {
      dispatch({
        type: "clearQuery",
      });
    } else if (query && !state.query) {
      dispatch({
        type: "waitForNewQuery",
        query,
      });
    }
  }, [query, state.query]);

  useEffect(() => {
    if (state.status === "waiting") {
      // Most likely called because of a change in onQueryChange so no action required
      return;
    } else if (state.status === "queryUpdated") {
      onQueryChange(state.query);
      dispatch({
        type: "waitForNewQuery",
        query: state.query,
      });
    }
  }, [state, dispatch, onQueryChange]);

  const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Escape" && state.query) {
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
        value={state.query}
        placeholder={searchStrings.searchBarPlaceholder}
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
