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
  debounce,
  Divider,
  FormControlLabel,
  IconButton,
  InputBase,
  Paper,
  Switch,
  Tooltip,
} from "@material-ui/core";
import * as React from "react";
import { useCallback, useState } from "react";
import SearchIcon from "@material-ui/icons/Search";
import { languageStrings } from "../../util/langstrings";
import { makeStyles } from "@material-ui/core/styles";

const ENTER_KEY_CODE = 13;
const ESCAPE_KEY_CODE = 27;

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

interface SearchBarProps {
  /** Current value for the search field. */
  query: string;

  /** Current value for the rawMode toggle. */
  rawMode: boolean;

  /**
   * Callback fired when the user stops typing (debounced for 500 milliseconds).
   * @param query The string to search.
   */
  onQueryChange: (query: string) => void;

  /**
   * Callback fired when the user changes the rawMode.
   * @param rawMode the new value for Raw Mode
   */
  onRawModeChange: (rawMode: boolean) => void;

  /** Called when search button clicked. */
  doSearch: () => void;
}

/**
 * Debounced searchbar component to be used in the Search Page.
 * This component does not handle the API query itself,
 * that should be done in the parent component in response to the
 * onXyzChange callbacks and the doSearch.
 */
export default function SearchBar({
  query,
  rawMode,
  onQueryChange,
  onRawModeChange,
  doSearch,
}: SearchBarProps) {
  const classes = useStyles();
  const searchStrings = languageStrings.searchpage;

  const [currentQuery, setCurrentQuery] = useState<string>(query);

  const debouncedOnQueryChange = useCallback(debounce(onQueryChange, 500), [
    doSearch,
  ]);

  const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    switch (event.keyCode) {
      case ESCAPE_KEY_CODE:
        if (currentQuery) {
          // iff there is a current query, clear it out and trigger a search
          setCurrentQuery("");
          onQueryChange("");
        }
        break;
      case ENTER_KEY_CODE:
        debouncedOnQueryChange(currentQuery);
        break;
    }
  };

  const handleOnChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const updatedQuery = event.target.value;
    setCurrentQuery(updatedQuery);
    if (!rawMode) {
      debouncedOnQueryChange(updatedQuery);
    }
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
          rawMode ? searchStrings.pressEnterToSearch : searchStrings.title
        }
      />
      <Divider className={classes.divider} orientation="vertical" />
      <Tooltip title={searchStrings.rawSearchTooltip}>
        <FormControlLabel
          label={searchStrings.rawSearch}
          control={
            <Switch
              id="rawSearch"
              onChange={(_, checked) => onRawModeChange(checked)}
              value={rawMode}
              checked={rawMode}
              name={searchStrings.rawSearch}
            />
          }
        />
      </Tooltip>
    </Paper>
  );
}
