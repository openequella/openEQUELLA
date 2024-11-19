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
import * as RS from "fp-ts/ReadonlySet";
import * as React from "react";
import { useState } from "react";
import SelectRoleDialog from "../../../../components/securityentitydialog/SelectRoleDialog";
import SettingsListControl from "../../../../components/SettingsListControl";
import { TooltipIconButton } from "../../../../components/TooltipIconButton";
import { languageStrings } from "../../../../util/langstrings";

const { selectRole: selectRoleLabel } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .roleMappings;

export interface SelectRoleControlProps {
  /**
   * Aria label for the edit icon
   */
  ariaLabel?: string;
  /**
   * Text to appear on the top line of the row.
   */
  primaryText: string;
  /**
   * Text to appear on the bottom line(s) of the row.
   */
  secondaryText?: string;
  /**
   * The initial list of selected roles
   */
  value?: ReadonlySet<OEQ.UserQuery.RoleDetails>;
  /**
   * The handler when option or selected roles has been changed.
   *
   * @param roles New set of roles.
   */
  onChange: (roles: ReadonlySet<OEQ.UserQuery.RoleDetails>) => void;
  /** Function which will provide the list of Role (search function) for RoleSelector. */
  roleListProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
}

/**
 * SettingsListControl with an edit icon to let users search for and select roles.
 */
const SelectRoleControl = ({
  primaryText,
  secondaryText,
  value = RS.empty,
  onChange,
  roleListProvider,
  ariaLabel = selectRoleLabel,
}: SelectRoleControlProps) => {
  const [showSelectRoleDialog, setShowSelectRoleDialog] = useState(false);

  return (
    <>
      <SettingsListControl
        primaryText={primaryText}
        secondaryText={secondaryText}
        control={
          <Badge badgeContent={value.size} color="secondary">
            <TooltipIconButton
              color="primary"
              title={ariaLabel}
              aria-label={ariaLabel}
              onClick={() => setShowSelectRoleDialog(true)}
            >
              <EditIcon fontSize="large"></EditIcon>
            </TooltipIconButton>
          </Badge>
        }
      />
      <SelectRoleDialog
        value={value}
        open={showSelectRoleDialog}
        onClose={(selectedRoles) => {
          setShowSelectRoleDialog(false);
          if (selectedRoles) {
            onChange(selectedRoles);
          }
        }}
        roleListProvider={roleListProvider}
      />
    </>
  );
};

export default SelectRoleControl;
