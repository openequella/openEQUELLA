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
  Card,
  CardContent,
  IconButton,
  InputAdornment,
  TextField,
} from "@material-ui/core";
import * as React from "react";
import { useCallback } from "react";
import SearchIcon from "@material-ui/icons/Search";
import { Close } from "@material-ui/icons";
import { debounce } from "lodash";

const ESCAPE_KEY_CODE = 27;
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
  const [query, setQuery] = React.useState<string>("");

  /**
   * uses lodash to debounce the search query by half a second
   */
  const delayedQuery = useCallback(
    debounce((query: string) => onChange(query), 500),
    [onChange]
  );

  const handleQueryChange = (query: string) => {
    setQuery(query);
    delayedQuery(query);
  };

  return (
    <Card>
      <CardContent>
        <TextField
          onKeyDown={(event) => {
            if (event.keyCode == ESCAPE_KEY_CODE) {
              event.preventDefault();
              handleQueryChange("");
            }
          }}
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
      </CardContent>
    </Card>
  );
}
