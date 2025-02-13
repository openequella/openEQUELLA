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
import GroupIcon from "@mui/icons-material/Group";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import { ordGroup } from "../../modules/GroupModule";
import { languageStrings } from "../../util/langstrings";
import GroupSearch from "../securityentitysearch/GroupSearch";
import SecurityEntityEntry from "./SecurityEntityEntry";
import SelectEntityDialog from "./SelectEntityDialog";

export interface SelectGroupDialogProps {
  /** Open the dialog when true. */
  open: boolean;
  /**
   * The currently selected Groups.
   * Undefined means the component should wait for the group details to be provided.
   */
  value?: ReadonlySet<OEQ.UserQuery.GroupDetails>;
  /** Handler for when dialog is closed. */
  onClose: (selections?: ReadonlySet<OEQ.UserQuery.GroupDetails>) => void;
  /** Function which will provide the list of group (search function) for GroupSearch. */
  searchGroupsProvider?: (
    query?: string,
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
}

/**
 * Provide a button for the user to select groups.
 * After clicking the button, the user can search and select groups in the pop-up group search dialog.
 */
const SelectGroupDialog = ({
  open,
  value,
  onClose,
  searchGroupsProvider,
}: SelectGroupDialogProps) => {
  const groupSearch = (
    onAdd: (group: OEQ.UserQuery.GroupDetails) => void,
    onSelectAll: (entities: ReadonlySet<OEQ.UserQuery.GroupDetails>) => void,
  ) => (
    <GroupSearch
      mode={{
        type: "one_click",
        onAdd: onAdd,
      }}
      search={searchGroupsProvider}
      onSelectAll={onSelectAll}
    />
  );

  const groupEntry = (g: OEQ.UserQuery.GroupDetails, onDelete: () => void) => (
    <SecurityEntityEntry
      key={g.id}
      name={g.name}
      onDelete={onDelete}
      icon={<GroupIcon color="secondary" />}
    />
  );

  return (
    <SelectEntityDialog
      open={open}
      title={languageStrings.groupSearchDialog.title}
      value={value}
      itemOrd={ordGroup}
      entityDetailsToEntry={groupEntry}
      searchComponent={groupSearch}
      onConfirm={onClose}
      onCancel={onClose}
      addEntityMessage={languageStrings.selectGroupDialog.addGroups}
    />
  );
};

export default SelectGroupDialog;
