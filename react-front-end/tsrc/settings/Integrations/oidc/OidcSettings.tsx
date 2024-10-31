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
import { Card, CardContent, Divider, Grid } from "@mui/material";
import { RoleDetails } from "@openequella/rest-api-client/dist/UserQuery";
import { useEffect, useState } from "react";
import * as React from "react";
import { CustomRolesMapping } from "../../../components/CustomRoleHelper";
import CustomRolesMappingControl from "../../../components/CustomRolesMappingControl";
import GeneralDetailsSection, {
  textFiledComponent,
} from "../../../components/GeneralDetailsSection";
import SettingPageTemplate from "../../../components/SettingPageTemplate";
import SettingsList from "../../../components/SettingsList";
import SettingsListControl from "../../../components/SettingsListControl";
import SettingsListWarning from "../../../components/SettingsListWarning";
import { routes } from "../../../mainui/routes";
import {
  templateDefaults,
  TemplateUpdateProps,
} from "../../../mainui/Template";
import { languageStrings } from "../../../util/langstrings";
import * as OEQ from "@openequella/rest-api-client";
import SelectRoleControl from "../lti13/components/SelectRoleControl";
import {
  defaultIdpGeneralDetails,
  generalDetailsRenderOptions,
} from "./OidcSettingsHelper";

const {
  name,
  oeqDetails: { title: oeqDetailsTitle },
  generalDetails: { title: generalDetailsTitle },
  roleMappings: {
    title: roleMappingsTitle,
    defaultRole: defaultRoleTitle,
    defaultRoleDesc,
    roleClaim: roleClaimTitle,
    roleClaimDesc,
    customRoleDialog: customRoleDialogStrings,
  },
} = languageStrings.settings.integration.oidc;
const { edit: editLabel } = languageStrings.common.action;

export interface OidcSettingsProps extends TemplateUpdateProps {
  /**
   * Function used to search oEQ roles.
   */
  searchRoleProvider?: (query?: string) => Promise<OEQ.UserQuery.RoleDetails[]>;
}

/**
 * Show warning message for role selector related controls,
 * if the IDs of role details fetched form server doesn't match with the initial group/groups IDs.
 */
interface RoleWarningMessages {
  defaultRoles?: string[];
  customRoles?: string[];
}

const OidcSettings = ({
  updateTemplate,
  searchRoleProvider,
}: OidcSettingsProps) => {
  useEffect(() => {
    updateTemplate((tp) => ({
      ...templateDefaults(name)(tp),
      backRoute: routes.Settings.to,
    }));
  }, [updateTemplate]);

  const handleOnSave = () => {};

  // Given that the save button on the creation page of OEQ has always been available in present UI,
  // set the disableSaveButton `false` here.
  const [saveButtonDisabled] = useState(false);
  const [showSnackBar, setShowSnackBar] = useState(false);
  const [preventNavigation] = useState(true);

  const [idpDetails, setIdpDetails] = useState<OEQ.Oidc.IdentityProvider>(
    defaultIdpGeneralDetails,
  );
  const [showValidationErrors] = useState(false);

  // Warning messages if the oeq roles can't be found in the server.
  const [
    { defaultRoles: defaultRolesWarnings, customRoles: customRolesWarnings },
  ] = useState<RoleWarningMessages>({
    defaultRoles: undefined,
    customRoles: undefined,
  });
  const [defaultRoles, setDefaultRoles] = useState<ReadonlySet<RoleDetails>>(
    new Set(),
  );
  const [customRoles, setCustomRoles] = useState<CustomRolesMapping>(new Map());

  // Update the corresponding value in generalDetailsRenderOptions based on the provided key.
  const onIdpGeneralDetailsChange = (key: string, newValue: unknown) =>
    setIdpDetails({
      ...idpDetails,
      [key]: newValue,
    });

  return (
    <SettingPageTemplate
      onSave={handleOnSave}
      preventNavigation={preventNavigation}
      saveButtonDisabled={saveButtonDisabled}
      snackBarOnClose={() => setShowSnackBar(false)}
      snackbarOpen={showSnackBar}
    >
      <Card>
        <CardContent>
          <Grid>
            <GeneralDetailsSection
              title={generalDetailsTitle}
              fields={generalDetailsRenderOptions(
                idpDetails,
                onIdpGeneralDetailsChange,
                showValidationErrors,
              )}
            />
          </Grid>

          <Divider variant="middle" />

          <Grid mt={2}>
            <SettingsList subHeading={roleMappingsTitle}>
              <SelectRoleControl
                ariaLabel={`${editLabel} ${defaultRoleTitle}`}
                primaryText={defaultRoleTitle}
                secondaryText={defaultRoleDesc}
                value={defaultRoles}
                onChange={setDefaultRoles}
                roleListProvider={searchRoleProvider}
              />
              {defaultRolesWarnings && (
                <SettingsListWarning messages={defaultRolesWarnings} />
              )}

              <SettingsListControl
                primaryText={roleClaimTitle}
                secondaryText={roleClaimDesc}
                control={textFiledComponent(
                  roleClaimTitle,
                  idpDetails.roleConfig?.roleClaim,
                  false,
                  true,
                  (value) =>
                    setIdpDetails({
                      ...idpDetails,
                      roleConfig: {
                        roleClaim: value,
                        customRoles:
                          idpDetails.roleConfig?.customRoles ?? new Map(),
                      },
                    }),
                  showValidationErrors,
                )}
              />

              {idpDetails.roleConfig?.roleClaim && (
                <>
                  <CustomRolesMappingControl
                    initialRoleMappings={customRoles}
                    onChange={setCustomRoles}
                    searchRoleProvider={searchRoleProvider}
                    strings={customRoleDialogStrings}
                  />
                  {customRolesWarnings && (
                    <SettingsListWarning messages={customRolesWarnings} />
                  )}
                </>
              )}
            </SettingsList>
          </Grid>
        </CardContent>
      </Card>

      <Card>
        <CardContent>{oeqDetailsTitle}</CardContent>
      </Card>
    </SettingPageTemplate>
  );
};

export default OidcSettings;
