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
import { pipe } from "fp-ts/function";
import * as RS from "fp-ts/ReadonlySet";
import * as TE from "fp-ts/TaskEither";
import * as React from "react";
import { useContext, useEffect, useState } from "react";
import SelectGroupDialog, {
  SelectGroupDialogProps,
} from "../../../../components/securityentitydialog/SelectGroupDialog";
import SettingsListControl from "../../../../components/SettingsListControl";
import { AppContext } from "../../../../mainui/App";
import { findGroupsByIds, groupIds } from "../../../../modules/GroupModule";
import { languageStrings } from "../../../../util/langstrings";
import SettingsListAlert from "../../../../components/SettingsListAlert";
import { getGroupsTask } from "../../../../components/securityentitydialog/SecurityEntityHelper";

export interface UnknownUserHandlingControlProps
  extends Pick<SelectGroupDialogProps, "searchGroupsProvider"> {
  /**
   * Initial selected option.
   */
  selection: OEQ.LtiPlatform.UnknownUserHandling;
  /**
   * The list of groups to be added to the user object If the unknown user handling is CREATE
   */
  groups?: ReadonlySet<OEQ.Common.UuidString>;
  /**
   * The handler when option or selected groups has been changed.
   *
   * @param option new selected option.
   * @param groups New set of groups.
   */
  onChange: (
    selection: OEQ.LtiPlatform.UnknownUserHandling,
    groups: ReadonlySet<OEQ.Common.UuidString>,
  ) => void;
  /**
   * Function to get all groups details by ids.
   */
  findGroupsByIdsProvider?: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.GroupDetails[]>;
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
  searchGroupsProvider,
  findGroupsByIdsProvider = findGroupsByIds,
}: UnknownUserHandlingControlProps) => {
  const { appErrorHandler } = useContext(AppContext);

  const [showSelectGroupDialog, setShowSelectGroupDialog] = useState(false);
  const [defaultGroups] = useState<ReadonlySet<OEQ.Common.UuidString>>(groups);
  // Show warning message if the IDs of group details fetched form server
  // can't match with the initial group IDs.
  //
  // For example:
  // suppose users select Group A and Group B for UnknownUserGroups. Later, if Group A gets deleted,
  // its ID will still be stored in the platform.
  // When the Edit page tries to get Group A and B, the server will only return Group B.
  // Consequently, a warning message will be displayed stating that Group A is missing.
  const [warningMessageForGroups, setWarningMessageForGroups] =
    useState<string[]>();
  const [groupDetails, setGroupDetails] =
    useState<ReadonlySet<OEQ.UserQuery.GroupDetails>>();

  useEffect(() => {
    pipe(
      getGroupsTask(groups, findGroupsByIdsProvider),
      TE.match(appErrorHandler, (result) => {
        setGroupDetails(result.entities);
        setWarningMessageForGroups(result.warning);
      }),
    )();
  }, [appErrorHandler, findGroupsByIdsProvider, groups]);

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
              value={groupDetails}
              open={showSelectGroupDialog}
              onClose={(selectedGroups) => {
                setShowSelectGroupDialog(false);
                selectedGroups && onChange(selection, groupIds(selectedGroups));
              }}
              searchGroupsProvider={searchGroupsProvider}
            />
          </ListItem>
        </>
      )}
    </>
  );
};

export default UnknownUserHandlingControl;
