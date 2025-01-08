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
import * as React from "react";
import { findRolesByIds, ordRole } from "../../modules/RoleModule";
import { languageStrings } from "../../util/langstrings";
import RoleSearch from "../securityentitysearch/RoleSearch";
import SecurityEntityEntry from "./SecurityEntityEntry";
import SelectEntityDialog from "./SelectEntityDialog";

export interface SelectRoleDialogProps {
  /** Open the dialog when true. */
  open: boolean;
  /** The currently selected Roles. */
  value: ReadonlySet<OEQ.Common.UuidString>;
  /** Handler for when dialog is closed. */
  onClose: (selections?: ReadonlySet<OEQ.Common.UuidString>) => void;
  /** Function which will provide the list of Role (search function) for RoleSearch. */
  searchRolesProvider?: (
    query?: string,
  ) => Promise<OEQ.UserQuery.RoleDetails[]>;
  /**
   * Function to get all roles details by ids.
   */
  findRolesByIdsProvider?: (
    ids: ReadonlySet<string>,
  ) => Promise<ReadonlyArray<OEQ.UserQuery.RoleDetails>>;
}

/**
 * Provide a button for the user to select roles.
 * After clicking the button, the user can search and select roles in the pop-up role search dialog.
 */
const SelectRoleDialog = ({
  open,
  value,
  onClose,
  searchRolesProvider,
  findRolesByIdsProvider = findRolesByIds,
}: SelectRoleDialogProps) => {
  const roleSearch = (
    onAdd: (roles: OEQ.UserQuery.RoleDetails) => void,
    onSelectAll: (entities: ReadonlySet<OEQ.UserQuery.RoleDetails>) => void,
  ) => (
    <RoleSearch
      mode={{
        type: "one_click",
        onAdd: onAdd,
      }}
      search={searchRolesProvider}
      onSelectAll={onSelectAll}
    />
  );

  const roleEntry = (r: OEQ.UserQuery.RoleDetails, onDelete: () => void) => (
    <SecurityEntityEntry key={r.id} name={r.name} onDelete={onDelete} />
  );

  return (
    <SelectEntityDialog
      open={open}
      title={languageStrings.roleSearchDialog.title}
      value={value}
      itemOrd={ordRole}
      entityDetailsToEntry={roleEntry}
      searchComponent={roleSearch}
      onConfirm={onClose}
      onCancel={onClose}
      addEntityMessage={languageStrings.selectRoleDialog.addRoles}
      findEntitiesByIds={findRolesByIdsProvider}
    />
  );
};

export default SelectRoleDialog;
