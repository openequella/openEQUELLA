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
import { InputLabel, MenuItem, Select } from "@material-ui/core";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";
import * as OEQ from "@openequella/rest-api-client";

/**
 * Type of props passed to SearchOrderSelect.
 */
export interface SearchOrderSelectProps {
  /**
   * The selected order. Being undefined means no option is selected.
   */
  value?: OEQ.SearchSettings.SortOrder;
  /**
   * Fired when a different sort order is selected.
   * @param sortOrder The new order.
   */
  onChange: (sortOrder: OEQ.SearchSettings.SortOrder) => void;
}

export const SearchOrderSelect = ({
  value,
  onChange,
}: SearchOrderSelectProps) => {
  const {
    relevance,
    lastModified,
    dateCreated,
    title,
    userRating,
  } = languageStrings.settings.searching.searchPageSettings;

  /**
   * Provide a data source for search sorting control.
   */
  const sortingOptionStrings = new Map<OEQ.SearchSettings.SortOrder, string>([
    ["RANK", relevance],
    ["DATEMODIFIED", lastModified],
    ["DATECREATED", dateCreated],
    ["NAME", title],
    ["RATING", userRating],
  ]);

  const baseId = "sort-order-select";
  const labelId = baseId + "-label";

  return (
    <>
      <InputLabel id={labelId} style={{ display: "none" }}>
        Search Order
      </InputLabel>
      <Select
        id={baseId}
        labelId={labelId}
        // If sortOrder is undefined, pass an empty string to select nothing.
        value={value ?? ""}
        onChange={(event) =>
          onChange(
            OEQ.SearchSettings.SortOrderRunTypes.check(event.target.value)
          )
        }
      >
        {Array.from(sortingOptionStrings).map(([value, text]) => (
          <MenuItem key={value} value={value}>
            {text}
          </MenuItem>
        ))}
      </Select>
    </>
  );
};

export default SearchOrderSelect;
