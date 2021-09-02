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
import { TextField } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";

export interface AdvancedSearchSelectorProps {
  /**
   * All available Advanced searches.
   */
  advancedSearches: OEQ.Common.BaseEntitySummary[];
  /**
   * Fires when a different Advanced search is selected.
   * @param collections Selected collections.
   */
  onSelectionChange: (
    advancedSearch: OEQ.Common.BaseEntitySummary | null
  ) => void;
  /**
   * Initially selected Advanced search.
   */
  value?: OEQ.Common.BaseEntitySummary;
}

const { title } = languageStrings.searchpage.advancedSearchSelector;

/**
 * Component used to select an Advanced search. Only single selection is supported.
 */
export const AdvancedSearchSelector = ({
  advancedSearches,
  onSelectionChange,
  value,
}: AdvancedSearchSelectorProps) => (
  <Autocomplete
    value={value}
    options={advancedSearches}
    getOptionLabel={({ name }: OEQ.Common.BaseEntitySummary) => name}
    onChange={(_, value: OEQ.Common.BaseEntitySummary | null) =>
      onSelectionChange(value)
    }
    renderInput={(params) => (
      <TextField
        {...params}
        variant="outlined"
        label={title}
        placeholder={title}
      />
    )}
  />
);
