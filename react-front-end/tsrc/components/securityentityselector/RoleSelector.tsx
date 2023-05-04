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
import { ordRole } from "../../modules/RoleModule";
import RoleSearch from "../securityentitysearch/RoleSearch";
import BaseSelector from "./BaseSelector";
import SecurityEntityEntry from "./SecutiryEntityEntry";

export interface RoleSelectorProps {
  /** The currently selected Roles. */
  value: ReadonlySet<OEQ.UserQuery.RoleDetails>;
  /** Handler for when a selection is deleted. */
  onDelete: (selection: OEQ.UserQuery.RoleDetails) => void;
  /** Handler for when a Role is selected. */
  onSelect: (selections: ReadonlySet<OEQ.UserQuery.RoleDetails>) => void;
  /** Function which will provide the list of Role (search function) for RoleSearch. */
  roleListProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
}

/**
 * Provide a button for the user to select roles.
 * After clicking the button, the user can search and select roles in the pop-up role search dialog.
 */
const RoleSelector = ({
  value,
  onDelete,
  onSelect,
  roleListProvider,
}: RoleSelectorProps) => {
  const roleSearch = (
    selectedRoles: ReadonlySet<OEQ.UserQuery.RoleDetails>,
    setSelectedRoles: (roles: ReadonlySet<OEQ.UserQuery.RoleDetails>) => void
  ) => (
    <RoleSearch
      onChange={setSelectedRoles}
      selections={selectedRoles}
      search={roleListProvider}
      enableMultiSelection
      onSelectAll={setSelectedRoles}
      onClearAll={setSelectedRoles}
    />
  );

  const roleEntry = (r: OEQ.UserQuery.RoleDetails) => (
    <SecurityEntityEntry
      key={r.id}
      name={r.name}
      onDelete={() => onDelete(r)}
    />
  );

  return (
    <BaseSelector
      title={languageStrings.roleSearchDialog.title}
      value={pipe(value, RS.toReadonlyArray(ordRole), RA.toArray)}
      entityDetailsToEntry={roleEntry}
      onClose={(selections) => {
        selections && onSelect(selections);
      }}
      searchComponent={roleSearch}
    />
  );
};

export default RoleSelector;
