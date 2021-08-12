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
  FormControl,
  MenuItem,
  OutlinedInput,
  Select,
} from "@material-ui/core";
import * as React from "react";
import { languageStrings } from "../../../util/langstrings";
import { makeStyles } from "@material-ui/core/styles";
import * as OEQ from "@openequella/rest-api-client";

export interface DefaultSortOrderSettingProps {
  disabled: boolean;
  value?: OEQ.SearchSettings.SortOrder;
  setValue: (order: OEQ.SearchSettings.SortOrder) => void;
}
const useStyles = makeStyles({
  select: {
    width: "200px",
  },
});
export default function DefaultSortOrderSetting({
  disabled,
  value,
  setValue,
}: DefaultSortOrderSettingProps) {
  const searchPageSettingsStrings =
    languageStrings.settings.searching.searchPageSettings;
  const classes = useStyles();

  const validateSortOrder = (value: unknown): OEQ.SearchSettings.SortOrder =>
    OEQ.SearchSettings.SortOrderRunTypes.check(value);

  return (
    <FormControl variant="outlined">
      <Select
        SelectDisplayProps={{ id: "_sortOrder" }}
        disabled={disabled}
        onChange={(event) => setValue(validateSortOrder(event.target.value))}
        variant="outlined"
        value={value}
        className={classes.select}
        input={<OutlinedInput labelWidth={0} id="_sortOrder" />}
      >
        <MenuItem value="RANK">{searchPageSettingsStrings.relevance}</MenuItem>
        <MenuItem value={validateSortOrder("DATEMODIFIED")}>
          {searchPageSettingsStrings.lastModified}
        </MenuItem>
        <MenuItem value={validateSortOrder("DATECREATED")}>
          {searchPageSettingsStrings.dateCreated}
        </MenuItem>
        <MenuItem value={validateSortOrder("NAME")}>
          {searchPageSettingsStrings.title}
        </MenuItem>
        <MenuItem value={validateSortOrder("RATING")}>
          {searchPageSettingsStrings.userRating}
        </MenuItem>
      </Select>
    </FormControl>
  );
}
