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
import { Checkbox, Chip, TextField } from "@material-ui/core";
import CheckBoxIcon from "@material-ui/icons/CheckBox";
import CheckBoxOutlineBlankIcon from "@material-ui/icons/CheckBoxOutlineBlank";
import { Autocomplete, AutocompleteGetTagProps } from "@material-ui/lab";
import { useEffect, useState } from "react";
import * as React from "react";
import { MimeTypeFilter } from "../../modules/SearchFilterSettingsModule";
import { languageStrings } from "../../util/langstrings";

export interface MimeTypeFilterSelectorProps {
  /**
   * MIME type filters that have been selected.
   */
  value?: MimeTypeFilter[];
  /**
   * Function to provide configured MIME type filters.
   */
  mimeTypeFilterProvider: () => Promise<MimeTypeFilter[]>;
  /**
   * Function fired on selecting different MIME type filters.
   * @param filters A list of currently selected MIME type filters.
   */
  onChange: (filters: MimeTypeFilter[]) => void;
}

const { helperText } = languageStrings.searchpage.mimeTypeFilterSelector;
/**
 * This component displays a list of configured MIME type filters which can be selected to
 * filter search results by Mime Types. It supports filtering options by typing keywords.
 */
export const MimeTypeFilterSelector = ({
  value,
  mimeTypeFilterProvider,
  onChange,
}: MimeTypeFilterSelectorProps) => {
  const [mimeTypeFilters, setMimeTypeFilters] = useState<MimeTypeFilter[]>([]);

  useEffect(() => {
    mimeTypeFilterProvider().then((filters) => setMimeTypeFilters(filters));
  }, [mimeTypeFilterProvider]);

  return (
    <Autocomplete
      multiple
      renderTags={(
        filters: MimeTypeFilter[],
        getTagProps: AutocompleteGetTagProps
      ) =>
        filters.map((filter, index) => (
          <Chip
            key={filter.id}
            label={filter.name}
            {...getTagProps({ index })}
          />
        ))
      }
      onChange={(_, value: MimeTypeFilter[]) => {
        onChange(value);
      }}
      value={value ?? []}
      options={mimeTypeFilters}
      disableCloseOnSelect
      getOptionLabel={(filter) => filter.name}
      getOptionSelected={(filter, selected) => filter.id === selected.id}
      renderOption={(filter, { selected }) => (
        <>
          <Checkbox
            icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
            checkedIcon={<CheckBoxIcon fontSize="small" />}
            checked={selected}
          />
          {filter.name}
        </>
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
};
