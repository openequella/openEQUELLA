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
import type { CustomRolesMappings } from "../../../../components/CustomRoleHelper";
import SettingsList from "../../../../components/SettingsList";
import { languageStrings } from "../../../../util/langstrings";
import LtiCustomRolesMapping from "./LtiCustomRolesMapping";
import SelectRoleControl from "../../../../components/SelectRoleControl";

const { roleMappings: roleMappingsStrings } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage;
const { edit: editLabel } = languageStrings.common.action;

export interface RoleMappingsSectionProps {
  /**
   * Selected roles that should be used when the LTI Instructor role is detected Instructor Roles.
   */
  instructorRoles: ReadonlySet<OEQ.Common.UuidString>;
  /**
   * Function to update the value of `instructorRoles`.
   */
  setInstructorRoles: (roles: ReadonlySet<OEQ.Common.UuidString>) => void;
  /**
   * The mapping relationships between LTI roles and oEQ roles.
   */
  customRolesMapping: CustomRolesMappings;
  /**
   * Function to update the value of `customRolesMapping`.
   */
  setCustomRolesMapping: (mappings: CustomRolesMappings) => void;
  /**
   * The roles that should be used for all unmapped LTI roles.
   */
  unknownRoles: ReadonlySet<OEQ.Common.UuidString>;
  /**
   * Function to update the value of `unknownRoles`.
   */
  setUnknownRoles: (roles: ReadonlySet<OEQ.Common.UuidString>) => void;
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
      />

      <LtiCustomRolesMapping
        value={customRolesMapping}
        onChange={setCustomRolesMapping}
      />

      {/*Unknown Roles*/}
      <SelectRoleControl
        ariaLabel={`${editLabel} ${unknownRolesLabel}`}
        primaryText={unknownRolesLabel}
        secondaryText={unknownRolesDesc}
        value={unknownRoles}
        onChange={setUnknownRoles}
      />
    </SettingsList>
  );
};

export default RoleMappingsSection;
