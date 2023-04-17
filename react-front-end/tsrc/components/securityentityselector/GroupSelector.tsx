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
import { pipe } from "fp-ts/function";
import * as RS from "fp-ts/ReadonlySet";
import * as RA from "fp-ts/ReadonlyArray";
import * as React from "react";
import { languageStrings } from "../../util/langstrings";
import GroupSearch from "../securityentitysearch/GroupSearch";
import { ordGroup } from "../../modules/GroupModule";
import GroupIcon from "@mui/icons-material/Group";
import BaseSelector from "./BaseSelector";
import SecurityEntityEntry from "./SecutiryEntityEntry";

export interface GroupSelectorProps {
  /** The currently selected Groups. */
  value: ReadonlySet<OEQ.UserQuery.GroupDetails>;
  /** Handler for when a selection is deleted. */
  onDelete: (selection: OEQ.UserQuery.GroupDetails) => void;
  /** Handler for when a Group is selected. */
  onSelect: (selections: ReadonlySet<OEQ.UserQuery.GroupDetails>) => void;
  /** Function which will provide the list of group (search function) for GroupSearch. */
  groupListProvider?: (query?: string) => Promise<OEQ.UserQuery.GroupDetails[]>;
}

/**
 * Provide a button for the user to select groups.
 * After clicking the button, the user can search and select groups in the pop-up group search dialog.
 */
const GroupSelector = ({
  value,
  onDelete,
  onSelect,
  groupListProvider,
}: GroupSelectorProps) => {
  const groupSearch = (
    selectedGroups: ReadonlySet<OEQ.UserQuery.GroupDetails>,
    setSelectedGroups: (groups: ReadonlySet<OEQ.UserQuery.GroupDetails>) => void
  ) => (
    <GroupSearch
      onChange={setSelectedGroups}
      selections={selectedGroups}
      search={groupListProvider}
      enableMultiSelection
      onSelectAll={setSelectedGroups}
      onClearAll={setSelectedGroups}
    />
  );

  const groupEntry = (g: OEQ.UserQuery.GroupDetails) => (
    <SecurityEntityEntry
      key={g.id}
      name={g.name}
      onDelete={() => onDelete(g)}
      icon={<GroupIcon color="secondary" />}
    />
  );

  return (
    <BaseSelector
      title={languageStrings.groupSearchDialog.title}
      value={pipe(value, RS.toReadonlyArray(ordGroup), RA.toArray)}
      entityDetailsToEntry={groupEntry}
      onClose={(selections) => {
        selections && onSelect(selections);
      }}
      searchComponent={groupSearch}
    />
  );
};

export default GroupSelector;
