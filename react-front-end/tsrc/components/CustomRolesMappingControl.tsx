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
import * as React from "react";
import type { CustomRolesMapping } from "./CustomRoleHelper";
import SettingsListControl from "./SettingsListControl";
import { TooltipIconButton } from "./TooltipIconButton";
import { languageStrings } from "../util/langstrings";
import SelectCustomRoleDialog, {
  SelectCustomRoleDialogProps,
} from "./SelectCustomRoleDialog";

const { customRolesDesc, customRoles: customRolesTitle } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .roleMappings;
const { edit: editLabel } = languageStrings.common.action;

export interface CustomRolesMappingControlProps
  extends Omit<SelectCustomRoleDialogProps, "open" | "onClose"> {
  /** Custom title for the settings control. */
  title?: string;
  /** Custom description for the settings control. */
  description?: string;
  /** Handler for when roles mapping is updated. */
  onChange: (maps: CustomRolesMapping) => void;
}

export const CustomRolesMappingControl = ({
  title,
  description,
  initialRoleMappings,
  onChange,
  searchRoleProvider,
  defaultCustomRole,
  customRoleSelector,
  strings,
}: CustomRolesMappingControlProps) => {
  const [showDialog, setShowDialog] = React.useState(false);

  const SelectButton = ({
    title,
    badge,
    onClick,
  }: {
    title: string;
    badge: React.ReactNode;
    onClick: () => void;
  }): React.JSX.Element => (
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

  return (
    <SettingsListControl
      primaryText={title ?? customRolesTitle}
      secondaryText={description ?? customRolesDesc}
      control={
        <>
          <SelectButton
            title={`${editLabel} ${title ?? customRolesTitle}`}
            badge={initialRoleMappings.size}
            onClick={() => setShowDialog(true)}
          />
          <SelectCustomRoleDialog
            open={showDialog}
            initialRoleMappings={initialRoleMappings}
            onClose={(result) => {
              setShowDialog(false);
              result && onChange(result);
            }}
            searchRoleProvider={searchRoleProvider}
            defaultCustomRole={defaultCustomRole}
            customRoleSelector={customRoleSelector}
            strings={strings}
          />
        </>
      }
    />
  );
};

export default CustomRolesMappingControl;
