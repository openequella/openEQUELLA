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
import { TextField } from "@mui/material";
import { Autocomplete } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as A from "fp-ts/Array";
import { pipe } from "fp-ts/function";
import * as ORD from "fp-ts/Ord";
import * as S from "fp-ts/string";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";

export interface AdvancedSearchSelectorProps {
  /**
   * All available Advanced searches.
   */
  advancedSearches: OEQ.Common.BaseEntitySummary[];
  /**
   * Function fired when the selection is changed.
   *
   * @param selection Currently selected Advanced search.
   */
  onSelectionChange: (selection: OEQ.Common.BaseEntitySummary | null) => void;
  /**
   * Initially selected Advanced search.
   */
  value?: OEQ.Common.BaseEntitySummary;
}

const { label } = languageStrings.searchpage.advancedSearchSelector;

/**
 * Component used to select an Advanced search. Only single selection is supported.
 */
export const AdvancedSearchSelector = ({
  advancedSearches,
  onSelectionChange,
  value,
}: AdvancedSearchSelectorProps) => (
  <Autocomplete
    value={value ?? null}
    options={pipe(
      advancedSearches,
      A.sort(
        ORD.contramap<string, OEQ.Common.BaseEntitySummary>(({ name }) => name)(
          S.Ord
        )
      )
    )}
    isOptionEqualToValue={(option, selected) => selected.uuid === option.uuid}
    getOptionLabel={({ name }: OEQ.Common.BaseEntitySummary) => name}
    onChange={(_, value: OEQ.Common.BaseEntitySummary | null) =>
      onSelectionChange(value)
    }
    renderOption={(props, { name }) => <li {...props}>{name}</li>}
    renderInput={(params) => (
      <TextField
        {...params}
        variant="outlined"
        label={label}
        placeholder={label}
      />
    )}
  />
);
