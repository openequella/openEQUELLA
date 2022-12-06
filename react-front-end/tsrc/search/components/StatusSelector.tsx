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
import CheckBoxIcon from "@mui/icons-material/CheckBox";
import CheckBoxOutlineBlankIcon from "@mui/icons-material/CheckBoxOutlineBlank";
import {
  Autocomplete,
  Button,
  ButtonGroup,
  Checkbox,
  TextField,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import { isEqual } from "lodash";
import * as React from "react";
import { liveStatuses, nonLiveStatuses } from "../../modules/SearchModule";
import { languageStrings } from "../../util/langstrings";

const { title, live, all } = languageStrings.searchpage.statusSelector;

interface AdvancedModeProps {
  options: OEQ.Common.ItemStatus[];
}

export interface StatusSelectorProps {
  /**
   * A list of the currently selected statuses.
   *
   * In normal mode, this list is then used to determine one of two
   * possible sets: live OR all.
   *
   * In advanced mode, this list determines what statuses have been selected.
   */
  value?: OEQ.Common.ItemStatus[];
  /**
   * Handler to call when the selection is modified. The resulting value being what would be passed
   * back as `value` for future renderings.
   *
   * @param value a list representing the new selection.
   */
  onChange: (value: OEQ.Common.ItemStatus[]) => void;
  /**
   * Use the component in advanced mode with a list of Item statuses.
   */
  advancedMode?: AdvancedModeProps;
}

interface AdvancedSelectorProps extends StatusSelectorProps {
  /**
   * Override advancedMode to make it mandatory.
   */
  advancedMode: AdvancedModeProps;
}

const NormalSelector = ({
  value = liveStatuses,
  onChange,
}: StatusSelectorProps) => {
  // iff it contains only those specified in the live status list then we consider
  // the selection to be the 'live' option, otherwise we will go with 'all'
  const isLive = (statusList: OEQ.Common.ItemStatus[]): boolean =>
    isEqual(statusList, liveStatuses);
  const variant = (determiner: () => boolean) =>
    determiner() ? "contained" : "outlined";

  return (
    <ButtonGroup color="secondary">
      <Button
        variant={variant(() => isLive(value))}
        onClick={() => onChange(liveStatuses)}
      >
        {live}
      </Button>
      <Button
        variant={variant(() => !isLive(value))}
        onClick={() => onChange(liveStatuses.concat(nonLiveStatuses))}
      >
        {all}
      </Button>
    </ButtonGroup>
  );
};

const AdvancedSelector = ({
  value,
  advancedMode: { options },
  onChange,
}: AdvancedSelectorProps) => (
  <Autocomplete
    fullWidth
    multiple
    disableCloseOnSelect
    value={value ?? []}
    onChange={(_, selected) => {
      onChange(selected);
    }}
    options={options}
    limitTags={2}
    getOptionLabel={(status) => status}
    isOptionEqualToValue={(status, selected) => selected === status}
    renderOption={(props, status, { selected }) => (
      <li {...props}>
        <Checkbox
          icon={<CheckBoxOutlineBlankIcon fontSize="small" />}
          checkedIcon={<CheckBoxIcon fontSize="small" />}
          checked={selected}
        />
        {status}
      </li>
    )}
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

/**
 * This component provides two modes for selections of Item status.
 *
 * In normal mode, it displays a button toggle to provide a simple means of either seeing items
 * which are 'live' or otherwise all. To do this, it basically considers that if the provided `value`
 * contains only those known as live then then status is 'live', otherwise it's all. Very simplistic.
 *
 * In advanced mode, it displays a Drop-down to allow users to select individual statuses. It also supports
 * customising Drop-down options and multiple selections.
 */
const StatusSelector = (props: StatusSelectorProps) =>
  props.advancedMode ? (
    <AdvancedSelector {...props} advancedMode={props.advancedMode} />
  ) : (
    <NormalSelector {...props} />
  );

export default StatusSelector;
