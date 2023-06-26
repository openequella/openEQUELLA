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
import EditIcon from "@mui/icons-material/Edit";
import { Badge } from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as React from "react";
import SettingsListControl from "../../../components/SettingsListControl";
import { TooltipIconButton } from "../../../components/TooltipIconButton";
import { languageStrings } from "../../../util/langstrings";
import SelectCustomRoleDialog from "./SelectCustomRoleDialog";

const { customRolesDesc, customRoles: customRolesTitle } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .roleMappings;
const { edit: editLabel } = languageStrings.common.action;

export interface CustomRolesMappingControlProps {
  /** Initial roles mapping value . */
  value: Map<string, Set<OEQ.UserQuery.RoleDetails>>;
  /** Handler for when roles mapping is updated. */
  onChange: (maps: Map<string, Set<OEQ.UserQuery.RoleDetails>>) => void;
  /** Function which will provide the list of Role (search function) for RoleSelector. */
  roleListProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
}

const SelectButton = ({
  badge,
  onClick,
}: {
  badge: React.ReactNode;
  onClick: () => void;
}): JSX.Element => {
  const title = `${editLabel} ${customRolesTitle}`;

  return (
    <Badge badgeContent={badge} color="secondary">
      <TooltipIconButton
        color="primary"
        title={title}
        aria-label={title}
        onClick={onClick}
      >
        <EditIcon fontSize="large"></EditIcon>
      </TooltipIconButton>
    </Badge>
  );
};

const CustomRolesMappingControl = ({
  value,
  onChange,
  roleListProvider,
}: CustomRolesMappingControlProps) => {
  const [showDialog, setShowDialog] = React.useState(false);

  return (
    <SettingsListControl
      primaryText={customRolesTitle}
      secondaryText={customRolesDesc}
      control={
        <>
          <SelectButton
            badge={value.size}
            onClick={() => setShowDialog(true)}
          />
          <SelectCustomRoleDialog
            open={showDialog}
            value={value}
            onClose={(result) => {
              setShowDialog(false);
              result && onChange(result);
            }}
            roleListProvider={roleListProvider}
          />
        </>
      }
    />
  );
};

export default CustomRolesMappingControl;
