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
  Tooltip,
} from "@material-ui/core";
import * as React from "react";
import { useCallback, useState } from "react";
import SearchIcon from "@material-ui/icons/Search";
import { debounce } from "lodash";
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
  /**
   * Callback fired when the user stops typing (debounced for 500 milliseconds).
   * @param query The string to search.
   */
  onChange: (query: string) => void;
}

/**
 * Debounced searchbar component to be used in the Search Page.
 * It also includes an adornment which allows clearing the search field in a single click.
 * This component does not handle the API query itself,
 * that should be done in the parent component with the onChange callback.
 */
export default function SearchBar({ onChange }: SearchBarProps) {
  const classes = useStyles();

  const searchStrings = languageStrings.searchpage;
  const [rawSearchMode, setRawSearchMode] = useState<boolean>(false);
  const [queryString, setQuery] = React.useState<string>("");
  const strings = languageStrings.searchpage;

  const callOnChange = (query: string) => {
    //only append the wildcard if raw search is off, and the query isn't blank
    const trimmedQuery = query.trim();
    const appendWildcard = !rawSearchMode && trimmedQuery.length > 0;
    const formattedQuery = trimmedQuery + (appendWildcard ? "*" : "");
    onChange(formattedQuery);
  };

  /**
   * uses lodash to debounce the search query by half a second
   */
  const debouncedQuery = useCallback(debounce(callOnChange, 500), [
    onChange,
    rawSearchMode,
  ]);

  const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    switch (event.keyCode) {
      case ESCAPE_KEY_CODE:
        event.preventDefault();
        handleQueryChange("");
        break;
      case ENTER_KEY_CODE:
        event.preventDefault();
        debouncedQuery(queryString);
        break;
    }
  };

  const handleQueryChange = (query: string) => {
    setQuery(query);
    if (!rawSearchMode) {
      debouncedQuery(query);
    }
  };

  return (
    <Paper className={classes.root}>
      <IconButton
        onClick={() => debouncedQuery(queryString)}
        aria-label={strings.title}
      >
        <SearchIcon />
      </IconButton>
      <InputBase
        id="searchBar"
        className={classes.input}
        onKeyDown={handleKeyDown}
        onChange={(event) => {
          handleQueryChange(event.target.value);
        }}
        value={queryString}
        placeholder={rawSearchMode ? strings.pressEnterToSearch : strings.title}
      />
      <Divider className={classes.divider} orientation="vertical" />
      <Tooltip title={searchStrings.rawSearchTooltip}>
        <FormControlLabel
          label={searchStrings.rawSearch}
          control={
            <Switch
              id="rawSearch"
              onChange={(_, checked) => setRawSearchMode(checked)}
              value={rawSearchMode}
              name={searchStrings.rawSearch}
            />
          }
        />
      </Tooltip>
    </Paper>
  );
}
