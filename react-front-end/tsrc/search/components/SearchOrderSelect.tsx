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
import { InputLabel, MenuItem, Select } from "@mui/material";
import { pipe } from "fp-ts/function";
import * as E from "../../util/Either.extended";
import * as React from "react";
import * as OEQ from "@openequella/rest-api-client";
import { defaultSortingOptions } from "../SearchPageHelper";

export type SortOrderOptions = Map<OEQ.Search.SortOrder, string>;

/**
 * Type of props passed to SearchOrderSelect.
 */
export interface SearchOrderSelectProps {
  /**
   * The selected order. Being undefined means no option is selected.
   */
  value?: OEQ.Search.SortOrder;
  /**
   * Fired when a different sort order is selected.
   * @param sortOrder The new order.
   */
  onChange: (sortOrder: OEQ.Search.SortOrder) => void;
  /**
   * If specified, will override the standard set of sort options.
   */
  customSortingOptions?: SortOrderOptions;
}

export const SearchOrderSelect = ({
  value,
  onChange,
  customSortingOptions,
}: SearchOrderSelectProps) => {
  /**
   * Provide a data source for search sorting control.
   */
  const sortOptions: SortOrderOptions =
    customSortingOptions ??
    new Map<OEQ.Search.SortOrder, string>(defaultSortingOptions);

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
          pipe(
            event.target.value,
            OEQ.Codec.Search.SortOrderCodec.decode,
            E.getOrThrow,
            onChange,
          )
        }
        variant="standard"
      >
        {Array.from(sortOptions).map(([value, text]) => (
          <MenuItem key={value} value={value}>
            {text}
          </MenuItem>
        ))}
      </Select>
    </>
  );
};

export default SearchOrderSelect;
