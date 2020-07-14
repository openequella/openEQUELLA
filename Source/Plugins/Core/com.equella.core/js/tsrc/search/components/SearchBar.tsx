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
import { IconButton, InputAdornment, TextField } from "@material-ui/core";
import * as React from "react";
import { useCallback } from "react";
import SearchIcon from "@material-ui/icons/Search";
import { Close } from "@material-ui/icons";
import { debounce } from "lodash";
import { languageStrings } from "../../util/langstrings";

const ENTER_KEY_CODE = 13;
const ESCAPE_KEY_CODE = 27;

interface SearchBarProps {
  /**
   * Callback fired when the user stops typing (debounced for 500 milliseconds).
   * @param query The string to search.
   */
  onChange: (query: string) => void;
  /**
   * Flag for raw search mode.
   * If false, search is a debounced type-ahead style simple search with a * wildcard added automatically.
   * If true, search occurs only on Enter press and is an explicit search which supports Lucene syntax.
   */
  rawSearchMode: boolean;
}

/**
 * Debounced searchbar component to be used in the Search Page.
 * It also includes an adornment which allows clearing the search field in a single click.
 * This component does not handle the API query itself,
 * that should be done in the parent component with the onChange callback.
 */
export default function SearchBar({ onChange, rawSearchMode }: SearchBarProps) {
  const [query, setQuery] = React.useState<string>("");
  const strings = languageStrings.searchpage;
  const callOnChange = (query: string) =>
    onChange(query + (rawSearchMode ? "" : "*"));
  /**
   * uses lodash to debounce the search query by half a second
   */
  const debouncedQuery = useCallback(debounce(callOnChange, 500), [onChange]);

  const handleKeyDown = (event: React.KeyboardEvent<HTMLDivElement>) => {
    switch (event.keyCode) {
      case ESCAPE_KEY_CODE:
        event.preventDefault();
        handleQueryChange("");
        break;
      case ENTER_KEY_CODE:
        event.preventDefault();
        debouncedQuery(query);
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
    <TextField
      id="searchBar"
      helperText={rawSearchMode ? strings.pressEnterToSearch : " "}
      onKeyDown={handleKeyDown}
      InputProps={{
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon fontSize="small" />
          </InputAdornment>
        ),
        endAdornment: query.length > 0 && (
          <IconButton onClick={() => handleQueryChange("")} size="small">
            <Close />
          </IconButton>
        ),
      }}
      fullWidth
      onChange={(event) => {
        handleQueryChange(event.target.value);
      }}
      variant="standard"
      value={query}
    />
  );
}
