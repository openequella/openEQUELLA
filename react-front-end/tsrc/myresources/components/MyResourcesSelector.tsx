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
import { MenuItem, Select } from "@mui/material";
import { pipe } from "fp-ts/function";
import { useContext } from "react";
import * as React from "react";
import { AppContext } from "../../mainui/App";
import { guestUser } from "../../modules/UserModule";
import { languageStrings } from "../../util/langstrings";
import { OrdAsIs } from "../../util/Ord";
import type { MyResourcesType } from "../MyResourcesPageHelper";
import * as M from "fp-ts/Map";

export interface MyResourcesSelectorProps {
  /**
   * Initially selected My resources type.
   */
  value: MyResourcesType;
  /**
   * Handler for selecting a different resource type.
   */
  onChange: (resourceType: MyResourcesType) => void;
}

const { published, drafts, scrapbook, modqueue, archive, all } =
  languageStrings.myResources.resourceType;

const defaultOptions = new Map<MyResourcesType, string>([
  ["Published", published],
  ["Drafts", drafts],
  ["Scrapbook", scrapbook],
  ["Moderation queue", modqueue],
  ["Archive", archive],
  ["All resources", all],
]);

/**
 * This component provides a Dropdown to allow selecting a single My resources type. The option of Scrapbook
 * may not be available, depending on whether the user has access to Scrapbook.
 */
export const MyResourcesSelector = ({
  value,
  onChange,
}: MyResourcesSelectorProps) => {
  const { scrapbookEnabled } = useContext(AppContext).currentUser ?? guestUser;

  const options: React.JSX.Element[] = pipe(
    defaultOptions,
    // When access to Scrapbook is not enabled, drop the option of Scrapbook.
    M.filter((value) => scrapbookEnabled || value !== scrapbook),
    M.mapWithIndex((key, value) => (
      <MenuItem key={key} value={key}>
        {value}
      </MenuItem>
    )),
    M.values<React.JSX.Element>(OrdAsIs),
  );

  return (
    <Select
      fullWidth
      value={value}
      onChange={({ target: { value } }) => {
        onChange(value as MyResourcesType);
      }}
      variant="outlined"
    >
      {options}
    </Select>
  );
};
