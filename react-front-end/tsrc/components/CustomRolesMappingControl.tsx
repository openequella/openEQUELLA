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
import { pipe } from "fp-ts/function";
import { useContext, useEffect, useState } from "react";
import * as React from "react";
import { AppContext } from "../mainui/App";
import { findRolesByIds } from "../modules/RoleModule";
import { generateCustomRoles } from "./securityentitydialog/SecurityEntityHelper";
import * as TE from "../util/TaskEither.extended";
import {
  CustomRolesDetailsMappings,
  CustomRolesMappings,
  transformCustomRoleMapping,
} from "./CustomRoleHelper";
import SettingsListAlert from "./SettingsListAlert";
import SettingsListControl from "./SettingsListControl";
import { TooltipIconButton } from "./TooltipIconButton";
import { languageStrings } from "../util/langstrings";
import SelectCustomRoleDialog, {
  SelectCustomRoleDialogProps,
} from "./SelectCustomRoleDialog";
import * as OEQ from "@openequella/rest-api-client";

const { title: customRolesTitle, desc: customRolesDesc } =
  languageStrings.customRolesMappingControl;
const { edit: editLabel } = languageStrings.common.action;

export interface CustomRolesMappingControlProps
  extends Omit<
    SelectCustomRoleDialogProps,
    "open" | "onClose" | "initialMappings"
  > {
  /** Initial custom roles mappings value with IDs. */
  initialMappings: CustomRolesMappings;
  /** Custom title for the settings control. */
  title?: string;
  /** Custom description for the settings control. */
  description?: string;
  /** Handler for when roles mapping is updated. */
  onChange: (maps: CustomRolesMappings) => void;
  /**
   * Functions to find role details by their IDs.
   */
  findRolesByIdsProvider?: (
    ids: ReadonlySet<string>,
  ) => Promise<OEQ.UserQuery.RoleDetails[]>;
}

export const CustomRolesMappingControl = ({
  title,
  description,
  initialMappings,
  onChange,
  searchRolesProvider,
  findRolesByIdsProvider = findRolesByIds,
  defaultCustomRoleId,
  customRoleSelector,
  strings,
}: CustomRolesMappingControlProps) => {
  const { appErrorHandler } = useContext(AppContext);

  const [showDialog, setShowDialog] = React.useState(false);

  // Role details for the initial mappings.
  const [rolesMappings, setRolesMappings] =
    React.useState<CustomRolesDetailsMappings>();
  // Warning messages if any oEQ role details can't be found in the server.
  const [warningMessages, setWarningMessages] = useState<string[]>();

  // Fetch role details from server.
  useEffect(() => {
    pipe(
      generateCustomRoles(initialMappings, findRolesByIdsProvider),
      TE.match(appErrorHandler, (result) => {
        setRolesMappings(result.mappings);
        setWarningMessages(result.warnings);
      }),
    )();
  }, [appErrorHandler, findRolesByIdsProvider, initialMappings]);

  const buttonTitle = `${editLabel} ${title ?? customRolesTitle}`;

  return (
    <>
      <SettingsListControl
        primaryText={title ?? customRolesTitle}
        secondaryText={description ?? customRolesDesc}
        control={
          <>
            <Badge badgeContent={initialMappings.size} color="secondary">
              <TooltipIconButton
                color="primary"
                title={buttonTitle}
                aria-label={buttonTitle}
                onClick={() => setShowDialog(true)}
              >
                <EditIcon fontSize="large"></EditIcon>
              </TooltipIconButton>
            </Badge>

            <SelectCustomRoleDialog
              open={showDialog}
              initialMappings={rolesMappings}
              onClose={(result) => {
                setShowDialog(false);
                result && pipe(result, transformCustomRoleMapping, onChange);
              }}
              searchRolesProvider={searchRolesProvider}
              defaultCustomRoleId={defaultCustomRoleId}
              customRoleSelector={customRoleSelector}
              strings={strings}
            />
          </>
        }
      />
      {warningMessages && (
        <SettingsListAlert severity="warning" messages={warningMessages} />
      )}
    </>
  );
};

export default CustomRolesMappingControl;
