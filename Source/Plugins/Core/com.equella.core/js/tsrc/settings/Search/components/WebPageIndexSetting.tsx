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
import { ContentIndex } from "../SearchSettingsModule";
import * as React from "react";
import { languageStrings } from "../../../util/langstrings";

export interface WebPageIndexSettingProps {
  disabled: boolean;
  value: ContentIndex;
  setValue: (indexOption: ContentIndex) => void;
}
export default function WebPageIndexSetting({
  disabled,
  value,
  setValue,
}: WebPageIndexSettingProps) {
  const contentIndexSettingsStrings =
    languageStrings.settings.searching.contentIndexSettings;
  return (
    <>
      <FormControl variant={"outlined"}>
        <Select
          SelectDisplayProps={{ id: "_contentIndex" }}
          disabled={disabled}
          onChange={(event) => setValue(event.target.value as ContentIndex)}
          variant={"outlined"}
          value={value}
          autoWidth={true}
          input={<OutlinedInput labelWidth={0} id={"_contentIndex"} />}
        >
          <MenuItem value={ContentIndex.OPTION_NONE}>
            {contentIndexSettingsStrings.option.none}
          </MenuItem>
          <MenuItem value={ContentIndex.OPTION_WEBPAGE}>
            {contentIndexSettingsStrings.option.webPage}
          </MenuItem>
          <MenuItem value={ContentIndex.OPTION_SECONDARY}>
            {contentIndexSettingsStrings.option.secondaryPage}
          </MenuItem>
        </Select>
      </FormControl>
    </>
  );
}
