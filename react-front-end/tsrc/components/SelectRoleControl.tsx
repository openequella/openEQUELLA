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
import { pipe } from "fp-ts/function";
import * as RS from "fp-ts/ReadonlySet";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useContext, useEffect, useState } from "react";
import { AppContext } from "../mainui/App";
import { findRolesByIds, roleIds } from "../modules/RoleModule";
import { getRolesTask } from "./securityentitydialog/SecurityEntityHelper";
import SelectRoleDialog, {
  SelectRoleDialogProps,
} from "./securityentitydialog/SelectRoleDialog";
import SettingsListAlert from "./SettingsListAlert";
import SettingsListControl from "./SettingsListControl";
import { TooltipIconButton } from "./TooltipIconButton";
import { languageStrings } from "../util/langstrings";

const { selectRole: selectRoleLabel } =
  languageStrings.settings.integration.lti13PlatformsSettings.createPage
    .roleMappings;

export interface SelectRoleControlProps
  extends Pick<SelectRoleDialogProps, "searchRolesProvider"> {
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
   * The initial list of selected roles IDs.
   */
  value?: ReadonlySet<OEQ.Common.UuidString>;
  /**
   * The handler when option or selected roles has been changed.
   *
   * @param roles New set of roles.
   */
  onChange: (roles: ReadonlySet<OEQ.Common.UuidString>) => void;
  /**
   * Function to get all entity details by their ids.
   */
  findRolesByIdsProvider?: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.RoleDetails[]>;
}

/**
 * SettingsListControl with an edit icon to let users search for and select roles.
 */
const SelectRoleControl = ({
  primaryText,
  secondaryText,
  value = RS.empty,
  onChange,
  ariaLabel = selectRoleLabel,
  findRolesByIdsProvider = findRolesByIds,
  searchRolesProvider,
}: SelectRoleControlProps) => {
  const { appErrorHandler } = useContext(AppContext);

  const [roles, setRoles] = useState<ReadonlySet<OEQ.UserQuery.RoleDetails>>();
  // Show warning messages if the IDs of role details fetched form server
  // doesn't match with the initial group/groups IDs.
  //
  // For example:
  //   suppose users select Role A and Role B for UnknownRoles. Later, if Role A gets deleted,
  //   its ID will still be stored in the platform.
  //   When the Edit page tries to get Role A and B, the server will only return Role B.
  //   Consequently, a warning message will be displayed stating that Role A is missing.
  const [warningMessages, setWarningMessages] = useState<string[]>();

  const [showSelectRoleDialog, setShowSelectRoleDialog] = useState(false);

  useEffect(() => {
    pipe(
      getRolesTask(value, findRolesByIdsProvider),
      TE.match(appErrorHandler, (result) => {
        setRoles(result.entities);
        setWarningMessages(result.warning);
      }),
    )();
  }, [appErrorHandler, findRolesByIdsProvider, value]);

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
      {warningMessages && (
        <SettingsListAlert severity="warning" messages={warningMessages} />
      )}
      <SelectRoleDialog
        value={roles}
        open={showSelectRoleDialog}
        onClose={(selectedRoles) => {
          setShowSelectRoleDialog(false);
          if (selectedRoles) {
            pipe(selectedRoles, roleIds, onChange);
          }
        }}
        searchRolesProvider={searchRolesProvider}
      />
    </>
  );
};

export default SelectRoleControl;
