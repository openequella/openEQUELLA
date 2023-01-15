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
import * as OEQ from "@openequella/rest-api-client";
import {
  Checkbox,
  TextField,
  AutocompleteRenderGetTagProps,
} from "@mui/material";
import CheckBoxIcon from "@mui/icons-material/CheckBox";
import CheckBoxOutlineBlankIcon from "@mui/icons-material/CheckBoxOutlineBlank";
import { Autocomplete } from "@mui/material";
import * as React from "react";
import { TooltipChip } from "../../components/TooltipChip";
import { languageStrings } from "../../util/langstrings";

export interface MimeTypeFilterSelectorProps {
  /**
   * MIME type filters that have been selected.
   */
  value?: OEQ.SearchFilterSettings.MimeTypeFilter[];
  /**
   * Function fired on selecting different MIME type filters.
   * @param filters A list of currently selected MIME type filters.
   */
  onChange: (filters: OEQ.SearchFilterSettings.MimeTypeFilter[]) => void;
  /**
   * All configured MIME type filters.
   */
  filters: OEQ.SearchFilterSettings.MimeTypeFilter[];
}

const { helperText } = languageStrings.searchpage.mimeTypeFilterSelector;
/**
 * This component displays a list of configured MIME type filters which can be selected to
 * filter search results by MIME Types. It supports filtering options by typing keywords.
 */
export const MimeTypeFilterSelector = ({
  value,
  onChange,
  filters,
}: MimeTypeFilterSelectorProps) => (
  <Autocomplete
    multiple
    renderTags={(
      filters: OEQ.SearchFilterSettings.MimeTypeFilter[],
      getTagProps: AutocompleteRenderGetTagProps
    ) =>
      filters.map((filter, index) => (
        <TooltipChip
          key={filter.id}
          title={filter.name}
          maxWidth={200}
          tagProps={getTagProps({ index })}
        />
      ))
    }
    onChange={(_, value: OEQ.SearchFilterSettings.MimeTypeFilter[]) => {
      onChange(value);
    }}
    value={value ?? []}
    options={filters}
    disableCloseOnSelect
    getOptionLabel={(filter) => filter.name}
    isOptionEqualToValue={(filter, selected) => filter.id === selected.id}
    renderOption={(props, filter, { selected }) => (
      <li {...props}>
        <Checkbox
          icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
          checkedIcon={<CheckBoxIcon fontSize="small" />}
          checked={selected}
        />
        {filter.name}
      </li>
    )}
    renderInput={(params) => (
      <TextField
        {...params}
        variant="outlined"
        label={helperText}
        placeholder={helperText}
      />
    )}
  />
);
