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
import type { CustomRolesMapping } from "../../../components/CustomRoleHelper";
import SettingsList from "../../../components/SettingsList";
import SettingsListWarning from "../../../components/SettingsListWarning";
import { languageStrings } from "../../../util/langstrings";
import LtiCustomRolesMapping from "./LtiCustomRolesMapping";
import SelectRoleControl from "./SelectRoleControl";

const { roleMappings: roleMappingsStrings } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage;
const { edit: editLabel } = languageStrings.common.action;

/**
 * Warning messages for role related controls.
 *
 * Those controls will show a warning message if the IDs of role details fetched form server
 * can't match with the initial role IDs.
 *
 * For example:
 * suppose users select Role A and Role B for instructorRoles. Later, if Role A gets deleted,
 * its ID will still be stored in the platform.
 * When the Edit page tries to get Role A and B, the server will only return Role B.
 * Consequently, a warning message will be displayed stating that Role A is missing.
 */
export interface RoleMappingWarnings {
  instructorRoles?: string[];
  customRolesMapping?: string[];
  unknownRoles?: string[];
}

export interface RoleMappingsSectionProps {
  /**
   * Selected roles that should be used when the LTI Instructor role is detected Instructor Roles.
   */
  instructorRoles: ReadonlySet<OEQ.UserQuery.RoleDetails>;
  /**
   * Function to update the value of `instructorRoles`.
   */
  setInstructorRoles: (roles: ReadonlySet<OEQ.UserQuery.RoleDetails>) => void;
  /**
   * The mapping relationships between LTI roles and oEQ roles.
   */
  customRolesMapping: CustomRolesMapping;
  /**
   * Function to update the value of `customRolesMapping`.
   */
  setCustomRolesMapping: (mappings: CustomRolesMapping) => void;
  /**
   * The roles that should be used for all unmapped LTI roles.
   */
  unknownRoles: ReadonlySet<OEQ.UserQuery.RoleDetails>;
  /**
   * Function to update the value of `unknownRoles`.
   */
  setUnknownRoles: (roles: ReadonlySet<OEQ.UserQuery.RoleDetails>) => void;
  /**
   * Function used to search roles.
   */
  searchRoleProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
  /**
   * WarningMessages for each role selectors.
   */
  warningMessages?: RoleMappingWarnings;
}

/**
 * This component is used to display and edit role mappings for the platform,
 * within the LTI 1.3 platform creation page.
 */
const RoleMappingsSection = ({
  instructorRoles,
  setInstructorRoles,
  customRolesMapping,
  setCustomRolesMapping,
  unknownRoles,
  setUnknownRoles,
  searchRoleProvider,
  warningMessages,
}: RoleMappingsSectionProps) => {
  const {
    title,
    instructorRoles: instructorRolesLabel,
    instructorRolesDesc,
    unknownRoles: unknownRolesLabel,
    unknownRolesDesc,
  } = roleMappingsStrings;

  return (
    <SettingsList subHeading={title}>
      {/*Instructor Roles*/}
      <SelectRoleControl
        ariaLabel={`${editLabel} ${instructorRolesLabel}`}
        primaryText={instructorRolesLabel}
        secondaryText={instructorRolesDesc}
        value={instructorRoles}
        onChange={setInstructorRoles}
        roleListProvider={searchRoleProvider}
      />
      {warningMessages?.instructorRoles && (
        <SettingsListWarning messages={warningMessages?.instructorRoles} />
      )}

      <LtiCustomRolesMapping
        value={customRolesMapping}
        onChange={setCustomRolesMapping}
        searchRoleProvider={searchRoleProvider}
      />
      {warningMessages?.customRolesMapping && (
        <SettingsListWarning messages={warningMessages?.customRolesMapping} />
      )}

      {/*Unknown Roles*/}
      <SelectRoleControl
        ariaLabel={`${editLabel} ${unknownRolesLabel}`}
        primaryText={unknownRolesLabel}
        secondaryText={unknownRolesDesc}
        value={unknownRoles}
        onChange={setUnknownRoles}
        roleListProvider={searchRoleProvider}
      />
      {warningMessages?.unknownRoles && (
        <SettingsListWarning messages={warningMessages?.unknownRoles} />
      )}
    </SettingsList>
  );
};

export default RoleMappingsSection;
