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
import { FormControl, ListItemText, MenuItem, Select } from "@mui/material";
import * as React from "react";
import {
  CustomRole,
  CustomRolesMapping,
} from "../../../../components/CustomRoleHelper";
import {
  defaultSelectedRoleUrn,
  getRoleNameByUrn,
  ltiRoles,
} from "../../../../modules/Lti13PlatformsModule";
import { languageStrings } from "../../../../util/langstrings";
import CustomRolesMappingControl from "../../../../components/CustomRolesMappingControl";
import * as OEQ from "@openequella/rest-api-client";

const {
  customRoleDialogTitle,
  customRoleSelectLtiRoleLabel,
  customRoleTableLtiRoleColumn,
} =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .roleMappings;

export const defaultLtiRole = {
  role: defaultSelectedRoleUrn,
  name: getRoleNameByUrn(defaultSelectedRoleUrn),
};

const selectLtiRole = (
  onChange: (value: CustomRole) => void,
  value?: CustomRole,
) => (
  <FormControl fullWidth>
    <Select
      value={value?.role ?? defaultLtiRole.role}
      onChange={(event) => {
        const claimValue = event.target.value;
        onChange({
          role: claimValue,
          name: getRoleNameByUrn(claimValue),
        });
      }}
      MenuProps={{
        PaperProps: {
          style: {
            maxHeight: 280,
          },
        },
      }}
      aria-label={customRoleSelectLtiRoleLabel}
      variant="outlined"
    >
      {ltiRoles.map(({ name, urn }) => (
        <MenuItem key={urn} value={urn}>
          <ListItemText primary={name} secondary={urn} />
        </MenuItem>
      ))}
    </Select>
  </FormControl>
);

export interface LtiCustomRolesMappingProps {
  /** The custom roles mappings. */
  value: CustomRolesMapping;
  /** Handler for when roles mapping is updated. */
  onChange: (maps: CustomRolesMapping) => void;
  /** Function which will provide the list of Role (search function). */
  searchRoleProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
}

const LtiCustomRolesMapping = ({
  value,
  onChange,
  searchRoleProvider,
}: LtiCustomRolesMappingProps) => (
  <CustomRolesMappingControl
    initialRoleMappings={value}
    onChange={onChange}
    searchRoleProvider={searchRoleProvider}
    strings={{
      title: customRoleDialogTitle,
      customRoleLabel: customRoleSelectLtiRoleLabel,
      customRoleColumnName: customRoleTableLtiRoleColumn,
    }}
    customRoleSelector={selectLtiRole}
    defaultCustomRole={defaultLtiRole}
  />
);

export default LtiCustomRolesMapping;
