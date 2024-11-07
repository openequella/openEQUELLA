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
import {
  Badge,
  Button,
  FormControl,
  ListItem,
  MenuItem,
  Select,
} from "@mui/material";
import * as OEQ from "@openequella/rest-api-client";
import * as RS from "fp-ts/ReadonlySet";
import * as React from "react";
import { useState } from "react";
import SelectGroupDialog, {
  SelectGroupDialogProps,
} from "../../../components/securityentitydialog/SelectGroupDialog";
import SettingsListControl from "../../../components/SettingsListControl";
import SettingsListAlert from "../../../components/SettingsListAlert";
import { languageStrings } from "../../../util/langstrings";

export interface GroupWarning {
  /**
   * Show warning message if the IDs of group details fetched form server
   * can't match with the initial group IDs.
   *
   * For example:
   * suppose users select Group A and Group B for UnknownUserGroups. Later, if Group A gets deleted,
   * its ID will still be stored in the platform.
   * When the Edit page tries to get Group A and B, the server will only return Group B.
   * Consequently, a warning message will be displayed stating that Group A is missing.
   */
  warningMessageForGroups?: string[];
}

export interface UnknownUserHandlingControlProps
  extends Pick<SelectGroupDialogProps, "groupListProvider">,
    GroupWarning {
  /**
   * Initial selected option.
   */
  selection: OEQ.LtiPlatform.UnknownUserHandling;
  /**
   * The list of groups to be added to the user object If the unknown user handling is CREATE
   */
  groups?: ReadonlySet<OEQ.UserQuery.GroupDetails>;
  /**
   * The handler when option or selected groups has been changed.
   *
   * @param option new selected option.
   * @param groups New set of groups.
   */
  onChange: (
    selection: OEQ.LtiPlatform.UnknownUserHandling,
    groups: ReadonlySet<OEQ.UserQuery.GroupDetails>,
  ) => void;
}

const {
  unknownUserHandlingCreate,
  unknownUserHandlingGuest,
  unknownUserHandlingDesc,
  unknownUserHandling,
  unknownUserHandlingDeny,
  groups: selectGroupLabel,
} = languageStrings.settings.integration.lti13PlatformsSettings.createPage
  .accessControl;
const { select: selectLabel } = languageStrings.common.action;

const unknownUserHandlingOptions = [
  ["ERROR", unknownUserHandlingDeny],
  ["CREATE", unknownUserHandlingCreate],
  ["GUEST", unknownUserHandlingGuest],
];

/**
 * Used to select different operations of the system when encountering an unknown user.
 * If `CREATE` option is selected, an additional group selector will be displayed.
 */
const UnknownUserHandlingControl = ({
  selection,
  groups = RS.empty,
  onChange,
  groupListProvider,
  warningMessageForGroups,
}: UnknownUserHandlingControlProps) => {
  const [showSelectGroupDialog, setShowSelectGroupDialog] = useState(false);

  const [defaultGroups] =
    useState<ReadonlySet<OEQ.UserQuery.GroupDetails>>(groups);

  return (
    <>
      <SettingsListControl
        primaryText={unknownUserHandling}
        secondaryText={unknownUserHandlingDesc}
        control={
          <FormControl fullWidth>
            <Select
              aria-label={`${selectLabel} ${unknownUserHandling}`}
              value={selection}
              onChange={(event) => {
                onChange(
                  event.target.value as OEQ.LtiPlatform.UnknownUserHandling,
                  // if user regret and switch back to CREATE, still keep the default group.
                  event.target.value === "CREATE" ? defaultGroups : RS.empty,
                );
              }}
            >
              {unknownUserHandlingOptions.map(([key, value]) => (
                <MenuItem selected={selection === value} key={key} value={key}>
                  {value}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        }
      ></SettingsListControl>

      {selection === "CREATE" && (
        <>
          {warningMessageForGroups && (
            <SettingsListAlert
              severity="warning"
              messages={warningMessageForGroups}
            />
          )}

          <ListItem>
            <Badge badgeContent={RS.size(groups)} color="secondary">
              <Button
                variant="outlined"
                color="primary"
                aria-label={selectGroupLabel}
                onClick={() => setShowSelectGroupDialog(true)}
              >
                {selectGroupLabel}
              </Button>
            </Badge>

            <SelectGroupDialog
              value={groups}
              open={showSelectGroupDialog}
              onClose={(selectedGroups) => {
                setShowSelectGroupDialog(false);
                selectedGroups && onChange(selection, selectedGroups);
              }}
              groupListProvider={groupListProvider}
            />
          </ListItem>
        </>
      )}
    </>
  );
};

export default UnknownUserHandlingControl;
