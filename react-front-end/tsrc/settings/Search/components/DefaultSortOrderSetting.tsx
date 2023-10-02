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
import { FormControl, MenuItem, OutlinedInput, Select } from "@mui/material";
import { styled } from "@mui/material/styles";
import { pipe } from "fp-ts/function";
import * as E from "../../../util/Either.extended";
import * as React from "react";
import { languageStrings } from "../../../util/langstrings";
import * as OEQ from "@openequella/rest-api-client";

const PREFIX = "DefaultSortOrderSetting";

const classes = {
  select: `${PREFIX}-select`,
};

const StyledFormControl = styled(FormControl)({
  [`& .${classes.select}`]: {
    width: "200px",
  },
});

export interface DefaultSortOrderSettingProps {
  disabled: boolean;
  value?: OEQ.Search.SortOrder;
  setValue: (order: OEQ.Search.SortOrder) => void;
}
export default function DefaultSortOrderSetting({
  disabled,
  value,
  setValue,
}: DefaultSortOrderSettingProps) {
  const { relevance, lastModified, dateCreated, title, userRating } =
    languageStrings.settings.searching.searchPageSettings;

  const options: [OEQ.Search.SortOrder, string][] = [
    ["rank", relevance],
    ["datemodified", lastModified],
    ["datecreated", dateCreated],
    ["name", title],
    ["rating", userRating],
  ];

  return (
    <StyledFormControl variant="outlined">
      <Select
        SelectDisplayProps={{ id: "_sortOrder" }}
        disabled={disabled}
        onChange={(event) =>
          pipe(
            event.target.value,
            OEQ.Codec.Search.SortOrderCodec.decode,
            E.getOrThrow,
            setValue,
          )
        }
        variant="outlined"
        value={value}
        className={classes.select}
        input={<OutlinedInput id="_sortOrder" />}
      >
        {options.map(([value, label]) => (
          <MenuItem key={value} value={value}>
            {label}
          </MenuItem>
        ))}
      </Select>
    </StyledFormControl>
  );
}
